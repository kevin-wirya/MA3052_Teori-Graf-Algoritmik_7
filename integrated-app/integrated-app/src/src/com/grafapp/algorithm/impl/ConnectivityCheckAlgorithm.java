package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

/**
 * Connectivity Check — memeriksa apakah graf terhubung (connected).
 */
public class ConnectivityCheckAlgorithm implements GraphAlgorithm {

    @Override public String getName() { return "Connectivity Check"; }
    @Override public String getCategory() { return "Connectivity"; }

    @Override
    public String getDescription() {
        return "Memeriksa apakah graf terhubung (connected) menggunakan DFS. "
             + "Memulai dari node pertama dan memeriksa apakah semua node dapat dikunjungi.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Collections.emptyList();
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        if (graph.getNodeCount() == 0) {
            return new AlgorithmResult(steps, "Graf kosong.");
        }

        int startNode = graph.getNodeIds().iterator().next();
        steps.add(AlgorithmStep.markStart(startNode,
            "Memulai pengecekan konektivitas dari node " + startNode));
        dfs(graph, startNode, visited, steps);

        boolean connected = visited.size() == graph.getNodeCount();

        String summary;
        if (connected) {
            summary = "Graf TERHUBUNG (Connected). Semua " + graph.getNodeCount()
                    + " node dapat dijangkau dari node " + startNode + ".";
        } else {
            Set<Integer> unreached = new LinkedHashSet<>(graph.getNodeIds());
            unreached.removeAll(visited);
            summary = "Graf TIDAK TERHUBUNG (Disconnected). "
                    + "Dijangkau: " + visited.size() + "/" + graph.getNodeCount()
                    + " node.\nNode tidak terjangkau: " + unreached;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("connected", connected);
        data.put("visitedCount", visited.size());
        return new AlgorithmResult(steps, summary, data);
    }

    private void dfs(Graph graph, int node, Set<Integer> visited,
                     List<AlgorithmStep> steps) {
        visited.add(node);
        steps.add(AlgorithmStep.visitNode(node, "Mengunjungi node " + node));

        for (int neighbor : graph.getNeighbors(node)) {
            if (!visited.contains(neighbor)) {
                steps.add(AlgorithmStep.traverseEdge(node, neighbor, ""));
                dfs(graph, neighbor, visited, steps);
            }
        }
        steps.add(AlgorithmStep.finishNode(node, "Selesai node " + node));
    }
}
