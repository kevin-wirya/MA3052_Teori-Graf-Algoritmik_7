package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import com.grafapp.model.GraphEdge;
import java.util.*;

/**
 * Minimum Spanning Tree / Forest menggunakan algoritma Prim.
 */
public class PrimMinimumSpanningTreeAlgorithm implements GraphAlgorithm {

    private static final double EPS = 1e-9;

    @Override public String getName() { return "Minimum Spanning Tree (Prim)"; }
    @Override public String getCategory() { return "Spanning Tree"; }

    @Override
    public String getDescription() {
        return "Membangun pohon pembangun minimal menggunakan algoritma Prim. "
             + "Jika graf tidak terhubung, hasilnya berupa minimum spanning forest.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Arrays.asList(
            new ParameterInfo("startNode", "Start Node", ParameterInfo.Type.NODE_SELECT, 0, true)
        );
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();

        if (graph == null || graph.getNodeCount() == 0) {
            steps.add(AlgorithmStep.log("Graf kosong — tidak ada MST yang dapat dibangun."));
            data.put("found", false);
            data.put("mstEdges", Collections.emptyList());
            data.put("mstWeight", 0.0);
            data.put("connected", false);
            return new AlgorithmResult(steps, "Graf kosong, MST tidak terdefinisi.", data);
        }

        if (graph.isDirected()) {
            steps.add(AlgorithmStep.log("Graf directed tidak didukung untuk MST Prim."));
            data.put("found", false);
            data.put("mstEdges", Collections.emptyList());
            data.put("mstWeight", 0.0);
            data.put("connected", false);
            return new AlgorithmResult(steps,
                "Minimum spanning tree hanya didefinisikan untuk graf undirected.", data);
        }

        List<Integer> nodeIds = new ArrayList<>(graph.getNodeIds());
        Collections.sort(nodeIds);

        Object startParam = parameters.get("startNode");
        int requestedStart = startParam instanceof Number
            ? ((Number) startParam).intValue()
            : nodeIds.get(0).intValue();
        int firstStart = nodeIds.contains(requestedStart) ? requestedStart : nodeIds.get(0).intValue();

        Set<Integer> visited = new HashSet<>();
        PriorityQueue<CandidateEdge> pq = new PriorityQueue<>(Comparator
            .comparingDouble((CandidateEdge e) -> e.weight)
            .thenComparingInt(e -> Math.min(e.from, e.to))
            .thenComparingInt(e -> Math.max(e.from, e.to))
            .thenComparingInt(e -> e.from)
            .thenComparingInt(e -> e.to));

        List<List<Integer>> mstEdges = new ArrayList<>();
        double totalWeight = 0.0;

        for (int pass = 0; pass < nodeIds.size(); pass++) {
            int startNode = (pass == 0) ? firstStart : nodeIds.get(pass).intValue();
            if (visited.contains(startNode)) continue;

            steps.add(AlgorithmStep.markStart(startNode,
                "Memulai Prim dari node " + startNode));
            visitNode(graph, startNode, visited, pq, steps);

            while (!pq.isEmpty()) {
                CandidateEdge candidate = pq.poll();
                if (visited.contains(candidate.to)) {
                    continue;
                }

                String weightLabel = formatWeight(candidate.weight);
                steps.add(AlgorithmStep.traverseEdge(candidate.from, candidate.to,
                    "Memeriksa kandidat edge " + candidate.from + " \u2192 " + candidate.to
                    + " (w = " + weightLabel + ")"));

                mstEdges.add(Arrays.asList(candidate.from, candidate.to));
                totalWeight += candidate.weight;
                steps.add(AlgorithmStep.markTreeEdge(candidate.from, candidate.to,
                    "Edge dipilih ke MST: " + candidate.from + " - " + candidate.to
                    + " (w = " + weightLabel + ")"));
                steps.add(AlgorithmStep.log("Edge diterima, total bobot sementara = "
                    + formatWeight(totalWeight)));

                visitNode(graph, candidate.to, visited, pq, steps);
            }
        }

        boolean connected = visited.size() == nodeIds.size();
        if (mstEdges.isEmpty()) {
            steps.add(AlgorithmStep.log("Tidak ada edge yang dipilih ke MST."));
        }

        String summary;
        if (connected) {
            summary = "MST ditemukan dengan total bobot = " + formatWeight(totalWeight)
                + " (" + mstEdges.size() + " edge).";
        } else {
            summary = "Minimum spanning forest ditemukan dengan total bobot = "
                + formatWeight(totalWeight) + " (" + mstEdges.size() + " edge).";
        }

        data.put("found", true);
        data.put("mstEdges", mstEdges);
        data.put("mstWeight", totalWeight);
        data.put("connected", connected);
        data.put("componentCount", countComponents(graph));

        return new AlgorithmResult(steps, summary, data);
    }

    private void visitNode(Graph graph, int node, Set<Integer> visited,
                           PriorityQueue<CandidateEdge> pq, List<AlgorithmStep> steps) {
        if (!visited.add(node)) return;

        steps.add(AlgorithmStep.visitNode(node, "Mengunjungi node " + node));

        for (int neighbor : graph.getNeighbors(node)) {
            if (visited.contains(neighbor)) continue;

            GraphEdge edge = graph.getEdge(node, neighbor);
            if (edge == null) continue;

            pq.offer(new CandidateEdge(node, neighbor, edge.getWeight()));
            steps.add(AlgorithmStep.traverseEdge(node, neighbor,
                "Menambahkan kandidat edge " + node + " \u2192 " + neighbor
                + " (w = " + formatWeight(edge.getWeight()) + ")"));
        }

        steps.add(AlgorithmStep.finishNode(node, "Selesai node " + node));
    }

    private int countComponents(Graph graph) {
        Set<Integer> visited = new HashSet<>();
        int components = 0;
        List<Integer> sortedIds = new ArrayList<>(graph.getNodeIds());
        Collections.sort(sortedIds);

        for (int node : sortedIds) {
            if (visited.add(node)) {
                components++;
                Deque<Integer> stack = new ArrayDeque<>();
                stack.push(node);
                while (!stack.isEmpty()) {
                    int current = stack.pop();
                    for (int neighbor : graph.getNeighbors(current)) {
                        if (visited.add(neighbor)) {
                            stack.push(neighbor);
                        }
                    }
                }
            }
        }
        return components;
    }

    private String formatWeight(double value) {
        if (Math.abs(value - Math.rint(value)) < EPS) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private static final class CandidateEdge {
        private final int from;
        private final int to;
        private final double weight;

        private CandidateEdge(int from, int to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }
}