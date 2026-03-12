package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

public class DiameterAlgorithm implements GraphAlgorithm {

    @Override public String getName() { return "Graph Diameter"; }
    @Override public String getCategory() { return "Properties"; }

    @Override
    public String getDescription() {
        return "Menghitung diameter graf, yaitu jarak terpanjang di antara semua "
             + "pasangan shortest path. Diameter = max eccentricity dari semua node. "
             + "Hanya dihitung pada graf terhubung (connected).";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Collections.emptyList();
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();
        List<Integer> sortedIds = new ArrayList<>(graph.getNodeIds());
        Collections.sort(sortedIds);

        if (sortedIds.isEmpty()) {
            steps.add(AlgorithmStep.log("Graf kosong — tidak ada node."));
            return new AlgorithmResult(steps, "Graf kosong, diameter tidak terdefinisi.");
        }

        Set<Integer> reachable = new HashSet<>();
        Queue<Integer> checkQueue = new LinkedList<>();
        int first = sortedIds.get(0);
        reachable.add(first);
        checkQueue.add(first);
        while (!checkQueue.isEmpty()) {
            int cur = checkQueue.poll();
            for (int nb : graph.getNeighbors(cur)) {
                if (reachable.add(nb)) {
                    checkQueue.add(nb);
                }
            }
        }
        if (reachable.size() != sortedIds.size()) {
            steps.add(AlgorithmStep.log("Graf tidak terhubung — diameter tidak terdefinisi (∞)."));
            return new AlgorithmResult(steps,
                "Graf tidak terhubung. Diameter = ∞ (tidak terdefinisi).");
        }

        int diameter = 0;
        int diameterU = -1, diameterV = -1;
        Map<Integer, Integer> eccentricities = new LinkedHashMap<>();

        
        for (int source : sortedIds) {
            steps.add(AlgorithmStep.visitNode(source,
                "Memulai BFS dari node " + source + " untuk menghitung eccentricity."));

            Map<Integer, Integer> dist = new HashMap<>();
            Queue<Integer> queue = new LinkedList<>();
            dist.put(source, 0);
            queue.add(source);

            int farthestNode = source;
            int maxDist = 0;

            while (!queue.isEmpty()) {
                int current = queue.poll();
                for (int neighbor : graph.getNeighbors(current)) {
                    if (!dist.containsKey(neighbor)) {
                        int d = dist.get(current) + 1;
                        dist.put(neighbor, d);
                        queue.add(neighbor);

                        steps.add(AlgorithmStep.traverseEdge(current, neighbor,
                            "Edge " + current + " → " + neighbor + ", jarak dari " + source + " = " + d));

                        if (d > maxDist) {
                            maxDist = d;
                            farthestNode = neighbor;
                        }
                    }
                }
            }

            eccentricities.put(source, maxDist);
            steps.add(AlgorithmStep.finishNode(source,
                "Eccentricity(" + source + ") = " + maxDist
                + " (node terjauh: " + farthestNode + ")"));

            if (maxDist > diameter) {
                diameter = maxDist;
                diameterU = source;
                diameterV = farthestNode;
            }
        }

        
        if (diameterU != -1 && diameterV != -1) {
            List<Integer> path = bfsPath(graph, diameterU, diameterV);
            if (path != null) {
                steps.add(AlgorithmStep.log("Diameter path: " + path));
                for (int node : path) {
                    steps.add(AlgorithmStep.markPathNode(node,
                        "Node pada diameter path"));
                }
                for (int i = 0; i < path.size() - 1; i++) {
                    steps.add(AlgorithmStep.markPathEdge(path.get(i), path.get(i + 1),
                        "Edge pada diameter path"));
                }
            }
        }

        String summary = "Diameter graf = " + diameter
            + " (antara node " + diameterU + " dan " + diameterV + ").";

        Map<String, Object> data = new HashMap<>();
        data.put("diameter", diameter);
        data.put("endpointU", diameterU);
        data.put("endpointV", diameterV);
        data.put("eccentricities", eccentricities);
        return new AlgorithmResult(steps, summary, data);
    }

    private List<Integer> bfsPath(Graph graph, int source, int target) {
        Map<Integer, Integer> parent = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        parent.put(source, -1);
        queue.add(source);

        while (!queue.isEmpty()) {
            int cur = queue.poll();
            if (cur == target) break;
            for (int nb : graph.getNeighbors(cur)) {
                if (!parent.containsKey(nb)) {
                    parent.put(nb, cur);
                    queue.add(nb);
                }
            }
        }

        if (!parent.containsKey(target)) return null;

        List<Integer> path = new ArrayList<>();
        for (int v = target; v != -1; v = parent.get(v)) {
            path.add(v);
        }
        Collections.reverse(path);
        return path;
    }
}
