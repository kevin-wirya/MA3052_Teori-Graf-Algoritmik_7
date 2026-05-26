package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

/**
 * Depth-First Search — traversal graf secara mendalam menggunakan rekursi.
 */
public class DFSAlgorithm implements GraphAlgorithm {

    @Override public String getName() { return "Depth-First Search (DFS)"; }
    @Override public String getCategory() { return "Traversal"; }

    @Override
    public String getDescription() {
        return "Menelusuri graf secara mendalam (depth-first) menggunakan rekursi/stack. "
             + "Mengunjungi setiap cabang sejauh mungkin sebelum backtrack.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Arrays.asList(
            new ParameterInfo("startNode", "Start Node", ParameterInfo.Type.NODE_SELECT, 0, true)
        );
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        int startNode = ((Number) parameters.get("startNode")).intValue();
        List<AlgorithmStep> steps = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        List<Integer> order = new ArrayList<>();

        steps.add(AlgorithmStep.markStart(startNode, "Memulai DFS dari node " + startNode));
        dfs(graph, startNode, visited, steps, order, -1);

        String summary = "DFS selesai. Urutan: " + order + " (" + order.size() + " node)";
        Map<String, Object> data = new HashMap<>();
        data.put("traversalOrder", order);
        return new AlgorithmResult(steps, summary, data);
    }

    private void dfs(Graph graph, int node, Set<Integer> visited,
                     List<AlgorithmStep> steps, List<Integer> order, int parent) {
        visited.add(node);
        order.add(node);

        // Edge diwarnai DULU, baru node
        if (parent != -1) {
            steps.add(AlgorithmStep.traverseEdge(parent, node,
                "Menelusuri edge " + parent + " \u2192 " + node));
        }
        steps.add(AlgorithmStep.visitNode(node, "Mengunjungi node " + node));

        for (int neighbor : graph.getNeighbors(node)) {
            if (!visited.contains(neighbor)) {
                dfs(graph, neighbor, visited, steps, order, node);
            }
        }

        steps.add(AlgorithmStep.finishNode(node, "Selesai node " + node));
        if (parent != -1) {
            steps.add(AlgorithmStep.finishEdge(parent, node, ""));
        }
    }
}
