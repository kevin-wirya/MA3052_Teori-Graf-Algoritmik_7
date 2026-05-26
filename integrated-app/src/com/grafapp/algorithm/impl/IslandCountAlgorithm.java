package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

public class IslandCountAlgorithm implements GraphAlgorithm {

    @Override public String getName() { return "Island Count"; }
    @Override public String getCategory() { return "Connectivity"; }

    @Override
    public String getDescription() {
        return "Menghitung jumlah pulau (connected components) dalam graf. "
             + "DFS traversal digunakan untuk menemukan semua komponen terhubung, "
             + "di mana setiap pulau ditandai dengan warna berbeda.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Collections.emptyList();
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        List<List<Integer>> islands = new ArrayList<>();

        List<Integer> sortedIds = new ArrayList<>(graph.getNodeIds());
        Collections.sort(sortedIds);

        steps.add(AlgorithmStep.log(
            "Memulai pencarian pulau pada graf dengan " + graph.getNodeCount() + " node..."));

        for (int nodeId : sortedIds) {
            if (!visited.contains(nodeId)) {
                List<Integer> island = new ArrayList<>();
                int islandNum = islands.size() + 1;
                steps.add(AlgorithmStep.log(
                    "Menemukan pulau #" + islandNum + " mulai dari node " + nodeId));
                dfs(graph, nodeId, visited, island, steps);
                islands.add(island);
                steps.add(AlgorithmStep.markComponent(islandNum - 1, island,
                    "Pulau #" + islandNum + ": " + island + " (ukuran: " + island.size() + " node)"));
            }
        }

        int count = islands.size();
        StringBuilder sb = new StringBuilder();
        sb.append("Jumlah Pulau: ").append(count);
        if (count == 0) {
            sb.append("\nGraf kosong, tidak ada pulau.");
        } else if (count == 1) {
            sb.append("\nGraf terdiri dari 1 pulau (graf terhubung).");
        } else {
            sb.append("\nGraf terdiri dari ").append(count).append(" pulau terpisah.");
        }
        for (int i = 0; i < islands.size(); i++) {
            sb.append("\n  Pulau ").append(i + 1).append(": ").append(islands.get(i));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("islandCount", count);
        data.put("islands", islands);
        return new AlgorithmResult(steps, sb.toString(), data);
    }

    private void dfs(Graph graph, int node, Set<Integer> visited,
                     List<Integer> island, List<AlgorithmStep> steps) {
        visited.add(node);
        island.add(node);
        steps.add(AlgorithmStep.visitNode(node, "Mengunjungi node " + node));

        for (int neighbor : graph.getNeighbors(node)) {
            if (!visited.contains(neighbor)) {
                steps.add(AlgorithmStep.traverseEdge(node, neighbor, ""));
                dfs(graph, neighbor, visited, island, steps);
            }
        }
        steps.add(AlgorithmStep.finishNode(node, ""));
    }
}
