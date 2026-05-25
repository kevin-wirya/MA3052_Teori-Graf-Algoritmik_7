package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

/**
 * Largest Component — menemukan komponen terhubung terbesar dalam graf.
 */
public class LargestComponentAlgorithm implements GraphAlgorithm {

    @Override public String getName() { return "Largest Component"; }
    @Override public String getCategory() { return "Connectivity"; }

    @Override
    public String getDescription() {
        return "Menemukan komponen terhubung yang memiliki jumlah node terbesar "
             + "dalam graf menggunakan DFS.";
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
                    "Menelusuri komponen #" + (compId + 1) + " mulai dari node " + nodeId));
                dfs(graph, nodeId, visited, component, steps);
                components.add(component);
                steps.add(AlgorithmStep.markComponent(compId, component,
                    "Komponen #" + (compId + 1) + ": " + component + " (ukuran: " + component.size() + ")"));
                compId++;
            }
        }

        // Cari komponen terbesar
        int largestIdx = 0;
        for (int i = 1; i < components.size(); i++) {
            if (components.get(i).size() > components.get(largestIdx).size()) {
                largestIdx = i;
            }
        }

        List<Integer> largest = components.get(largestIdx);

        // Highlight komponen terbesar dengan warna PATH
        for (int nodeId : largest) {
            steps.add(AlgorithmStep.markPathNode(nodeId, ""));
        }
        steps.add(AlgorithmStep.log("Komponen terbesar adalah komponen #" + (largestIdx + 1)
            + " dengan " + largest.size() + " node: " + largest));

        StringBuilder sb = new StringBuilder();
        sb.append("Total komponen: ").append(components.size()).append("\n");
        sb.append("Komponen terbesar: #").append(largestIdx + 1)
          .append(" (").append(largest.size()).append(" node)\n");
        sb.append("Node: ").append(largest);

        Map<String, Object> data = new HashMap<>();
        data.put("componentCount", components.size());
        data.put("components", components);
        data.put("largestIndex", largestIdx);
        data.put("largestComponent", largest);
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
