package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

/**
 * Breadth-First Search — traversal graf secara melebar menggunakan queue.
 */
public class BFSAlgorithm implements GraphAlgorithm {

    @Override public String getName() { return "Breadth-First Search (BFS)"; }
    @Override public String getCategory() { return "Traversal"; }

    @Override
    public String getDescription() {
        return "Menelusuri graf secara melebar (breadth-first) menggunakan queue. "
             + "Mengunjungi semua tetangga terlebih dahulu sebelum ke level berikutnya.";
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
        Set<Integer> visited = new LinkedHashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        List<Integer> order = new ArrayList<>();

        visited.add(startNode);
        queue.add(startNode);
        steps.add(AlgorithmStep.markStart(startNode, "Memulai BFS dari node " + startNode));
        steps.add(AlgorithmStep.visitNode(startNode, "Enqueue node " + startNode));

        while (!queue.isEmpty()) {
            int current = queue.poll();
            order.add(current);
            steps.add(AlgorithmStep.processNode(current,
                "Dequeue & proses node " + current));

            for (int neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    // Edge diwarnai DULU, baru node
                    steps.add(AlgorithmStep.traverseEdge(current, neighbor,
                        "Menelusuri edge " + current + " \u2192 " + neighbor));
                    steps.add(AlgorithmStep.visitNode(neighbor,
                        "Enqueue node " + neighbor));
                }
            }

            steps.add(AlgorithmStep.finishNode(current, "Selesai memproses node " + current));
        }

        String summary = "BFS selesai. Urutan: " + order + " (" + order.size() + " node)";
        Map<String, Object> data = new HashMap<>();
        data.put("traversalOrder", order);
        return new AlgorithmResult(steps, summary, data);
    }
}
