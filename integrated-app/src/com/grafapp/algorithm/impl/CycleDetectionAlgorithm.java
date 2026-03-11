package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

/**
 * Cycle Detection — mendeteksi SEMUA siklus dalam graf.
 */
public class CycleDetectionAlgorithm implements GraphAlgorithm {

    private static final int WHITE = 0, GRAY = 1, BLACK = 2;

    @Override public String getName() { return "Cycle Detection"; }
    @Override public String getCategory() { return "Properties"; }

    @Override
    public String getDescription() {
        return "Mendeteksi semua siklus dalam graf. "
             + "Setiap siklus akan ditampilkan dan bisa di-highlight.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Collections.emptyList();
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();

        if (graph.isDirected()) {
            return detectDirected(graph, steps);
        } else {
            return detectUndirected(graph, steps);
        }
    }

    private AlgorithmResult detectUndirected(Graph graph, List<AlgorithmStep> steps) {
        Map<Integer, Integer> color = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        List<List<Integer>> allCycles = new ArrayList<>();

        List<Integer> sortedIds = new ArrayList<>(graph.getNodeIds());
        Collections.sort(sortedIds);
        for (int id : sortedIds) color.put(id, WHITE);

        for (int start : sortedIds) {
            if (color.get(start) == WHITE) {
                parent.put(start, -1);
                dfsUndirected(graph, start, color, parent, steps, allCycles);
            }
        }

        return buildResult(allCycles, steps, false);
    }

    private void dfsUndirected(Graph graph, int node, Map<Integer, Integer> color,
                                Map<Integer, Integer> parent,
                                List<AlgorithmStep> steps, List<List<Integer>> allCycles) {
        color.put(node, GRAY);
        steps.add(AlgorithmStep.visitNode(node, "Mengunjungi node " + node));

        for (int neighbor : graph.getNeighbors(node)) {
            if (color.get(neighbor) == WHITE) {
                parent.put(neighbor, node);
                steps.add(AlgorithmStep.traverseEdge(node, neighbor,
                    "Menelusuri edge " + node + " \u2192 " + neighbor));
                dfsUndirected(graph, neighbor, color, parent, steps, allCycles);
            } else if (color.get(neighbor) == GRAY && neighbor != parent.getOrDefault(node, -1)) {
                // Back edge ke ancestor GRAY → siklus!
                steps.add(AlgorithmStep.traverseEdge(node, neighbor,
                    "Back edge: " + node + " \u2192 " + neighbor + " \u2014 Siklus!"));

                List<Integer> cycle = reconstructCycle(node, neighbor, parent);
                if (cycle != null && !isDuplicate(allCycles, cycle)) {
                    allCycles.add(cycle);
                }
            }
        }

        color.put(node, BLACK);
        steps.add(AlgorithmStep.finishNode(node, "Selesai node " + node));
    }

    private AlgorithmResult detectDirected(Graph graph, List<AlgorithmStep> steps) {
        Map<Integer, Integer> color = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        List<List<Integer>> allCycles = new ArrayList<>();

        List<Integer> sortedIds = new ArrayList<>(graph.getNodeIds());
        Collections.sort(sortedIds);
        for (int id : sortedIds) color.put(id, WHITE);

        for (int start : sortedIds) {
            if (color.get(start) == WHITE) {
                parent.put(start, -1);
                dfsDirected(graph, start, color, parent, steps, allCycles);
            }
        }

        return buildResult(allCycles, steps, true);
    }

    private void dfsDirected(Graph graph, int node, Map<Integer, Integer> color,
                              Map<Integer, Integer> parent,
                              List<AlgorithmStep> steps, List<List<Integer>> allCycles) {
        color.put(node, GRAY);
        steps.add(AlgorithmStep.visitNode(node, "Mengunjungi node " + node + " (gray)"));

        for (int neighbor : graph.getNeighbors(node)) {
            if (color.get(neighbor) == WHITE) {
                parent.put(neighbor, node);
                steps.add(AlgorithmStep.traverseEdge(node, neighbor,
                    "Menelusuri edge " + node + " \u2192 " + neighbor));
                dfsDirected(graph, neighbor, color, parent, steps, allCycles);
            } else if (color.get(neighbor) == GRAY) {
                steps.add(AlgorithmStep.traverseEdge(node, neighbor,
                    "Back edge: " + node + " \u2192 " + neighbor + " \u2014 Siklus!"));

                List<Integer> cycle = reconstructCycle(node, neighbor, parent);
                if (cycle != null && !isDuplicate(allCycles, cycle)) {
                    allCycles.add(cycle);
                }
            }
        }

        color.put(node, BLACK);
        steps.add(AlgorithmStep.finishNode(node, "Selesai node " + node));
    }

    // Rekonstruksi siklus dari node kembali ke ancestor via parent chain
    private List<Integer> reconstructCycle(int fromNode, int toAncestor, Map<Integer, Integer> parent) {
        List<Integer> cycle = new ArrayList<>();
        cycle.add(toAncestor);
        int cur = fromNode;
        int limit = 1000;
        while (cur != toAncestor && limit-- > 0) {
            cycle.add(cur);
            Integer p = parent.get(cur);
            if (p == null || p == -1) return null; 
            cur = p;
        }
        if (cur != toAncestor) return null;
        return cycle;
    }

    private AlgorithmResult buildResult(List<List<Integer>> allCycles, List<AlgorithmStep> steps, boolean directed) {
        Map<String, Object> data = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        if (!allCycles.isEmpty()) {
            // Highlight first cycle
            List<Integer> firstCycle = allCycles.get(0);
            for (int node : firstCycle) {
                steps.add(AlgorithmStep.markPathNode(node, ""));
            }
            for (int i = 0; i < firstCycle.size(); i++) {
                int u = firstCycle.get(i);
                int v = firstCycle.get((i + 1) % firstCycle.size());
                steps.add(AlgorithmStep.markPathEdge(u, v, ""));
            }
            steps.add(AlgorithmStep.log("Ditemukan " + allCycles.size() + " siklus."));

            sb.append("Ditemukan ").append(allCycles.size()).append(" siklus!\n");
            for (int i = 0; i < allCycles.size(); i++) {
                sb.append("Cycle ").append(i + 1).append(": ").append(formatCycle(allCycles.get(i)));
                if (i < allCycles.size() - 1) sb.append("\n");
            }
            data.put("hasCycle", true);
            data.put("allCycles", allCycles);
        } else {
            String msg = directed ? "Graf bersifat DAG (Directed Acyclic Graph)."
                                  : "Graf bersifat acyclic, bisa jadi tree atau forest.";
            steps.add(AlgorithmStep.log("Tidak ada siklus. " + msg));
            sb.append("TIDAK ADA SIKLUS\n").append(msg);
            data.put("hasCycle", false);
            data.put("allCycles", Collections.emptyList());
        }

        return new AlgorithmResult(steps, sb.toString(), data);
    }

    private boolean isDuplicate(List<List<Integer>> existing, List<Integer> newCycle) {
        Set<Integer> newSet = new HashSet<>(newCycle);
        for (List<Integer> c : existing) {
            if (new HashSet<>(c).equals(newSet)) return true;
        }
        return false;
    }

    private String formatCycle(List<Integer> cycle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cycle.size(); i++) {
            if (i > 0) sb.append(" \u2192 ");
            sb.append(cycle.get(i));
        }
        sb.append(" \u2192 ").append(cycle.get(0));
        return sb.toString();
    }
}
