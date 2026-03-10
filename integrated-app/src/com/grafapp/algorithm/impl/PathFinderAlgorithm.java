package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

/**
 * Path Finder — mencari lintasan dari node A ke node B menggunakan DFS + backtracking.
 */
public class PathFinderAlgorithm implements GraphAlgorithm {

    @Override public String getName() { return "Path Finder (A \u2192 B)"; }
    @Override public String getCategory() { return "Path Finding"; }

    @Override
    public String getDescription() {
        return "Mencari lintasan dari node A ke node B menggunakan DFS dengan backtracking. "
             + "Menampilkan proses pencarian dan lintasan yang ditemukan.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Arrays.asList(
            new ParameterInfo("startNode", "Start Node (A)",
                ParameterInfo.Type.NODE_SELECT, 0, true),
            new ParameterInfo("endNode", "End Node (B)",
                ParameterInfo.Type.NODE_SELECT, 0, true)
        );
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        int startNode = ((Number) parameters.get("startNode")).intValue();
        int endNode = ((Number) parameters.get("endNode")).intValue();

        List<AlgorithmStep> steps = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        List<Integer> path = new ArrayList<>();

        steps.add(AlgorithmStep.markStart(startNode, "Node awal: " + startNode));
        steps.add(AlgorithmStep.markEnd(endNode, "Node tujuan: " + endNode));

        boolean found = dfs(graph, startNode, endNode, visited, path, steps);

        Map<String, Object> data = new HashMap<>();
        data.put("found", found);
        String summary;

        if (found) {
            data.put("path", new ArrayList<>(path));
            // Highlight final path
            for (int i = 0; i < path.size(); i++) {
                steps.add(AlgorithmStep.markPathNode(path.get(i), ""));
                if (i > 0) {
                    steps.add(AlgorithmStep.markPathEdge(path.get(i - 1), path.get(i), ""));
                }
            }
            summary = "Lintasan DITEMUKAN: " + path;
        } else {
            summary = "TIDAK ADA lintasan dari " + startNode + " ke " + endNode;
        }

        return new AlgorithmResult(steps, summary, data);
    }

    private boolean dfs(Graph graph, int current, int target, Set<Integer> visited,
                        List<Integer> path, List<AlgorithmStep> steps) {
        visited.add(current);
        path.add(current);
        steps.add(AlgorithmStep.visitNode(current, "Mengunjungi node " + current));

        if (current == target) {
            steps.add(AlgorithmStep.log("Node tujuan " + target + " ditemukan!"));
            return true;
        }

        for (int neighbor : graph.getNeighbors(current)) {
            if (!visited.contains(neighbor)) {
                steps.add(AlgorithmStep.traverseEdge(current, neighbor,
                    "Mencoba " + current + " \u2192 " + neighbor));
                if (dfs(graph, neighbor, target, visited, path, steps)) {
                    return true;
                }
                steps.add(AlgorithmStep.log("Backtrack dari " + neighbor));
            }
        }

        path.remove(path.size() - 1);
        steps.add(AlgorithmStep.finishNode(current, "Backtrack dari node " + current));
        return false;
    }
}
