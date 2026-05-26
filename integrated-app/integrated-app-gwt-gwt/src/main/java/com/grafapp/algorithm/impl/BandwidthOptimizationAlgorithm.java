package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.AlgorithmResult;
import com.grafapp.algorithm.AlgorithmStep;
import com.grafapp.algorithm.GraphAlgorithm;
import com.grafapp.algorithm.ParameterInfo;
import com.grafapp.model.Graph;
import com.grafapp.model.GraphEdge;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Heuristic untuk Graph Bandwidth Problem.
 * Menggunakan Cuthill-McKee dan local swap untuk menurunkan bandwidth.
 */
public class BandwidthOptimizationAlgorithm implements GraphAlgorithm {

    @Override
    public String getName() {
        return "Graph Bandwidth (Heuristic)";
    }

    @Override
    public String getCategory() {
        return "Optimization";
    }

    @Override
    public String getDescription() {
        return "Mengoptimasi penomoran node agar bandwidth graf mengecil. "
            + "Menggunakan Cuthill-McKee dan perbaikan local swap.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Collections.emptyList();
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();

        if (graph == null || graph.getNodeCount() == 0) {
            return new AlgorithmResult(steps, "Graf kosong, bandwidth tidak dapat dihitung.", data);
        }

        List<Integer> baseOrder = new ArrayList<>(graph.getNodeIds());
        Map<Integer, List<Integer>> adj = buildAdjacency(graph);

        List<Integer> cmOrder = cuthillMcKeeOrder(adj, baseOrder);
        List<Integer> rcmOrder = new ArrayList<>(cmOrder);
        Collections.reverse(rcmOrder);

        int baselineBandwidth = computeBandwidth(baseOrder, graph);
        int maxPasses = Math.max(1, Math.min(30, baseOrder.size()));

        OrderCandidate best = evaluateCandidate("Original", baseOrder, graph, maxPasses);
        OrderCandidate cmBest = evaluateCandidate("Cuthill-McKee", cmOrder, graph, maxPasses);
        OrderCandidate rcmBest = evaluateCandidate("Reverse Cuthill-McKee", rcmOrder, graph, maxPasses);

        best = minBandwidth(best, cmBest);
        best = minBandwidth(best, rcmBest);

        data.put("bandwidthOrderBefore", baseOrder);
        data.put("bandwidthOrderAfter", best.order);
        data.put("bandwidthBefore", baselineBandwidth);
        data.put("bandwidthAfter", best.bandwidth);
        data.put("bandwidthMethod", best.method);

        String summary = "Bandwidth sebelum: " + baselineBandwidth
            + ", sesudah: " + best.bandwidth
            + " (" + best.method + ").";

        steps.add(AlgorithmStep.log(summary));
        steps.add(AlgorithmStep.log("Order awal: " + formatOrder(baseOrder)));
        steps.add(AlgorithmStep.log("Order hasil: " + formatOrder(best.order)));

        return new AlgorithmResult(steps, summary, data);
    }

    private Map<Integer, List<Integer>> buildAdjacency(Graph graph) {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (int id : graph.getNodeIds()) {
            adj.put(id, new ArrayList<>());
        }
        for (GraphEdge edge : graph.getEdges()) {
            int u = edge.getSource();
            int v = edge.getTarget();
            adj.get(u).add(v);
            adj.get(v).add(u);
        }
        return adj;
    }

    private List<Integer> cuthillMcKeeOrder(Map<Integer, List<Integer>> adj, List<Integer> nodeIds) {
        Map<Integer, Integer> degree = new HashMap<>();
        for (int id : nodeIds) {
            degree.put(id, adj.getOrDefault(id, Collections.emptyList()).size());
        }

        List<Integer> sorted = new ArrayList<>(nodeIds);
        sorted.sort((a, b) -> compareByDegree(a, b, degree));

        Set<Integer> visited = new HashSet<>();
        List<Integer> order = new ArrayList<>();
        Queue<Integer> queue = new ArrayDeque<>();

        for (int start : sorted) {
            if (visited.contains(start)) {
                continue;
            }
            visited.add(start);
            queue.add(start);

            while (!queue.isEmpty()) {
                int u = queue.poll();
                order.add(u);

                List<Integer> neighbors = new ArrayList<>(adj.getOrDefault(u, Collections.emptyList()));
                neighbors.sort((a, b) -> compareByDegree(a, b, degree));

                for (int v : neighbors) {
                    if (visited.add(v)) {
                        queue.add(v);
                    }
                }
            }
        }

        return order;
    }

    private int compareByDegree(int a, int b, Map<Integer, Integer> degree) {
        int da = degree.getOrDefault(a, 0);
        int db = degree.getOrDefault(b, 0);
        if (da != db) {
            return Integer.compare(da, db);
        }
        return Integer.compare(a, b);
    }

    private int computeBandwidth(List<Integer> order, Graph graph) {
        Map<Integer, Integer> pos = new HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            pos.put(order.get(i), i);
        }

        int max = 0;
        for (GraphEdge edge : graph.getEdges()) {
            Integer p1 = pos.get(edge.getSource());
            Integer p2 = pos.get(edge.getTarget());
            if (p1 == null || p2 == null) {
                continue;
            }
            int diff = Math.abs(p1 - p2);
            if (diff > max) {
                max = diff;
            }
        }
        return max;
    }

    private List<Integer> improveByAdjacentSwaps(List<Integer> order, Graph graph, int maxPasses) {
        List<Integer> best = new ArrayList<>(order);
        int bestBandwidth = computeBandwidth(best, graph);

        for (int pass = 0; pass < maxPasses; pass++) {
            boolean improved = false;
            for (int i = 0; i < best.size() - 1; i++) {
                Collections.swap(best, i, i + 1);
                int bw = computeBandwidth(best, graph);
                if (bw < bestBandwidth) {
                    bestBandwidth = bw;
                    improved = true;
                } else {
                    Collections.swap(best, i, i + 1);
                }
            }
            if (!improved) {
                break;
            }
        }
        return best;
    }

    private OrderCandidate evaluateCandidate(String method, List<Integer> order, Graph graph, int maxPasses) {
        List<Integer> improved = improveByAdjacentSwaps(order, graph, maxPasses);
        int bw = computeBandwidth(improved, graph);
        return new OrderCandidate(method + " + local swap", improved, bw);
    }

    private OrderCandidate minBandwidth(OrderCandidate a, OrderCandidate b) {
        if (b.bandwidth < a.bandwidth) {
            return b;
        }
        return a;
    }

    private String formatOrder(List<Integer> order) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < order.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(order.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    private static final class OrderCandidate {
        private final String method;
        private final List<Integer> order;
        private final int bandwidth;

        private OrderCandidate(String method, List<Integer> order, int bandwidth) {
            this.method = method;
            this.order = order;
            this.bandwidth = bandwidth;
        }
    }
}
