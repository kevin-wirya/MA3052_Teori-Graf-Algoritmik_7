package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

/**
 * Connected Components — menemukan semua komponen terhubung dalam graf.
 */
public class ConnectedComponentsAlgorithm implements GraphAlgorithm {

    @Override public String getName() { return "Connected Components"; }
    @Override public String getCategory() { return "Connectivity"; }

    @Override
    public String getDescription() {
        return "Menemukan semua komponen terhubung dalam graf menggunakan DFS. "
             + "Setiap komponen ditandai dengan warna berbeda.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Collections.emptyList();
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        List<List<Integer>> components = new ArrayList<>();
        int compId = 0;

        List<Integer> sortedIds = new ArrayList<>(graph.getNodeIds());
        Collections.sort(sortedIds);

        for (int nodeId : sortedIds) {
            if (!visited.contains(nodeId)) {
                List<Integer> component = new ArrayList<>();
                steps.add(AlgorithmStep.log(
                    "Menemukan komponen baru #" + (compId + 1) + " mulai dari node " + nodeId));
                dfs(graph, nodeId, visited, component, steps);
                components.add(component);
                steps.add(AlgorithmStep.markComponent(compId, component,
                    "Komponen #" + (compId + 1) + ": " + component + " (ukuran: " + component.size() + ")"));
                compId++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Ditemukan ").append(components.size()).append(" komponen terhubung.");
        for (int i = 0; i < components.size(); i++) {
            sb.append("\n  Komponen ").append(i + 1).append(": ").append(components.get(i));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("componentCount", components.size());
        data.put("components", components);
        return new AlgorithmResult(steps, sb.toString(), data);
    }

    private void dfs(Graph graph, int node, Set<Integer> visited,
                     List<Integer> component, List<AlgorithmStep> steps) {
        visited.add(node);
        component.add(node);
        steps.add(AlgorithmStep.visitNode(node, "Mengunjungi node " + node));

        for (int neighbor : graph.getNeighbors(node)) {
            if (!visited.contains(neighbor)) {
                steps.add(AlgorithmStep.traverseEdge(node, neighbor, ""));
                dfs(graph, neighbor, visited, component, steps);
            }
        }
        steps.add(AlgorithmStep.finishNode(node, ""));
    }
}
