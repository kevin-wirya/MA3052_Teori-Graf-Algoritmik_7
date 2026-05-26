package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import com.grafapp.model.GraphEdge;
import java.util.*;

/**
 * Dijkstra shortest path dari node A ke node B pada graf berbobot non-negatif.
 */
public class DijkstraShortestPathAlgorithm implements GraphAlgorithm {

    private static final double EPS = 1e-9;

    @Override public String getName() { return "Shortest Path (Dijkstra)"; }
    @Override public String getCategory() { return "Path Finding"; }

    @Override
    public String getDescription() {
        return "Menentukan lintasan terpendek dari node A ke node B menggunakan Dijkstra. "
             + "Mendukung graf directed/undirected dengan bobot edge non-negatif.";
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
        Map<String, Object> data = new HashMap<>();

        if (graph.getNode(startNode) == null || graph.getNode(endNode) == null) {
            data.put("found", false);
            return new AlgorithmResult(steps,
                "Node start/end tidak valid. Pastikan kedua node ada di graf.", data);
        }

        if (hasNegativeWeight(graph)) {
            steps.add(AlgorithmStep.log("Terdeteksi bobot negatif pada edge."));
            data.put("found", false);
            return new AlgorithmResult(steps,
                "Dijkstra tidak mendukung edge dengan bobot negatif.", data);
        }

        steps.add(AlgorithmStep.markStart(startNode, "Node awal: " + startNode));
        steps.add(AlgorithmStep.markEnd(endNode, "Node tujuan: " + endNode));

        if (startNode == endNode) {
            List<Integer> trivialPath = Collections.singletonList(startNode);
            steps.add(AlgorithmStep.markPathNode(startNode,
                "Start dan end sama, lintasan terpendek bernilai 0."));
            data.put("found", true);
            data.put("path", new ArrayList<>(trivialPath));
            data.put("shortestPath", new ArrayList<>(trivialPath));
            data.put("distance", 0.0);
            data.put("shortestDistance", 0.0);
            return new AlgorithmResult(steps,
                "Lintasan terpendek: " + formatPath(trivialPath) + " (total bobot = 0)",
                data);
        }

        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        for (int nodeId : graph.getNodeIds()) {
            dist.put(nodeId, Double.POSITIVE_INFINITY);
            parent.put(nodeId, -1);
        }
        dist.put(startNode, 0.0);

        PriorityQueue<NodeDistance> pq =
            new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        pq.offer(new NodeDistance(startNode, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            int u = current.nodeId;
            double known = dist.getOrDefault(u, Double.POSITIVE_INFINITY);

            if (current.distance > known + EPS) {
                continue;
            }

            steps.add(AlgorithmStep.processNode(u,
                "Memproses node " + u + " dengan jarak saat ini = " + formatWeight(known)));

            if (u == endNode) {
                steps.add(AlgorithmStep.log(
                    "Node tujuan " + endNode + " dipilih dari priority queue."));
                steps.add(AlgorithmStep.finishNode(u, "Selesai memproses node " + u));
                break;
            }

            for (int v : graph.getNeighbors(u)) {
                GraphEdge edge = graph.getEdge(u, v);
                if (edge == null) continue;

                double weight = edge.getWeight();
                double candidate = known + weight;

                steps.add(AlgorithmStep.traverseEdge(u, v,
                    "Relaksasi edge " + u + " -> " + v
                    + " (w = " + formatWeight(weight) + ")"));

                if (candidate + EPS < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, candidate);
                    parent.put(v, u);
                    pq.offer(new NodeDistance(v, candidate));
                    steps.add(AlgorithmStep.log(
                        "Update jarak node " + v + " = " + formatWeight(candidate)
                        + " lewat node " + u));
                } else {
                    steps.add(AlgorithmStep.log(
                        "Tidak ada update untuk node " + v + "."));
                }

                steps.add(AlgorithmStep.finishEdge(u, v, ""));
            }

            steps.add(AlgorithmStep.finishNode(u, "Selesai memproses node " + u));
        }

        double shortestDistance = dist.getOrDefault(endNode, Double.POSITIVE_INFINITY);
        if (Double.isInfinite(shortestDistance)) {
            data.put("found", false);
            data.put("distance", Double.POSITIVE_INFINITY);
            data.put("shortestDistance", Double.POSITIVE_INFINITY);
            return new AlgorithmResult(steps,
                "Tidak ada lintasan dari " + startNode + " ke " + endNode + ".", data);
        }

        List<Integer> path = buildPath(parent, startNode, endNode);
        for (int i = 0; i < path.size(); i++) {
            int node = path.get(i);
            steps.add(AlgorithmStep.markPathNode(node, "Node lintasan terpendek: " + node));
            if (i > 0) {
                int prev = path.get(i - 1);
                steps.add(AlgorithmStep.markPathEdge(prev, node,
                    "Edge lintasan terpendek: " + prev + " -> " + node));
            }
        }

        data.put("found", true);
        data.put("path", new ArrayList<>(path));
        data.put("shortestPath", new ArrayList<>(path));
        data.put("distance", shortestDistance);
        data.put("shortestDistance", shortestDistance);
        data.put("distanceFromStart", sortedDistanceMap(dist));

        String summary = "Lintasan terpendek ditemukan: " + formatPath(path)
            + " (Total bobot = " + formatWeight(shortestDistance) + ")";
        return new AlgorithmResult(steps, summary, data);
    }

    private boolean hasNegativeWeight(Graph graph) {
        for (GraphEdge edge : graph.getEdges()) {
            if (edge.getWeight() < 0) return true;
        }
        return false;
    }

    private List<Integer> buildPath(Map<Integer, Integer> parent, int startNode, int endNode) {
        List<Integer> path = new ArrayList<>();
        int cursor = endNode;

        while (cursor != -1) {
            path.add(cursor);
            if (cursor == startNode) break;
            cursor = parent.getOrDefault(cursor, -1);
        }

        Collections.reverse(path);
        return path;
    }

    private Map<Integer, Double> sortedDistanceMap(Map<Integer, Double> dist) {
        List<Integer> ids = new ArrayList<>(dist.keySet());
        Collections.sort(ids);
        Map<Integer, Double> ordered = new LinkedHashMap<>();
        for (int id : ids) {
            ordered.put(id, dist.get(id));
        }
        return ordered;
    }

    private String formatPath(List<Integer> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(path.get(i));
        }
        return sb.toString();
    }

    private String formatWeight(double value) {
        if (Double.isInfinite(value)) return "INF";
        if (Math.abs(value - Math.rint(value)) < EPS) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private static final class NodeDistance {
        private final int nodeId;
        private final double distance;

        private NodeDistance(int nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }
}