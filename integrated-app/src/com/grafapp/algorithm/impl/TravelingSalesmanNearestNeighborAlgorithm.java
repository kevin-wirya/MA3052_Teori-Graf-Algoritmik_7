package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.AlgorithmResult;
import com.grafapp.algorithm.AlgorithmStep;
import com.grafapp.algorithm.GraphAlgorithm;
import com.grafapp.algorithm.ParameterInfo;
import com.grafapp.model.Graph;
import com.grafapp.model.GraphEdge;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Traveling Salesman Problem menggunakan nearest neighbor (greedy)
 * tetapi tidak menjamin solusi pasti optimal
 */
public class TravelingSalesmanNearestNeighborAlgorithm implements GraphAlgorithm {

    private static final double EPS = 1e-9;

    @Override
    public String getName() {
        return "Traveling Salesman (Nearest Neighbor)";
    }

    @Override
    public String getCategory() {
        return "Optimization";
    }

    @Override
    public String getDescription() {
        return "Mencari tur TSP dengan heuristik Nearest Neighbor dari node awal. "
            + "Pendekatan ini sederhana dan cepat, namun tidak selalu menghasilkan tur optimal.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Arrays.asList(
            new ParameterInfo("startNode", "Start Node", ParameterInfo.Type.NODE_SELECT, 0, true)
        );
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();

        if (graph == null || graph.getNodeCount() == 0) {
            data.put("found", false);
            data.put("tspTour", Collections.emptyList());
            data.put("tspCost", Double.POSITIVE_INFINITY);
            return new AlgorithmResult(steps, "Graf kosong, tur TSP tidak dapat dibangun.", data);
        }

        if (graph.isDirected()) {
            steps.add(AlgorithmStep.log("TSP Nearest Neighbor saat ini hanya mendukung graf undirected."));
            data.put("found", false);
            data.put("tspTour", Collections.emptyList());
            data.put("tspCost", Double.POSITIVE_INFINITY);
            return new AlgorithmResult(steps,
                "Algoritma TSP sederhana ini hanya mendukung graf undirected.", data);
        }

        List<Integer> nodeIds = new ArrayList<>(graph.getNodeIds());
        Collections.sort(nodeIds);

        int requestedStart = parameters.get("startNode") instanceof Number
            ? ((Number) parameters.get("startNode")).intValue()
            : nodeIds.get(0);
        int startNode = nodeIds.contains(requestedStart) ? requestedStart : nodeIds.get(0);

        if (startNode != requestedStart) {
            steps.add(AlgorithmStep.log("Start node tidak valid, menggunakan node " + startNode + "."));
        }

        steps.add(AlgorithmStep.markStart(startNode, "Mulai tur dari node " + startNode));
        steps.add(AlgorithmStep.markPathNode(startNode, "Node awal tur: " + startNode));

        if (graph.getNodeCount() == 1) {
            List<Integer> trivialTour = new ArrayList<>();
            trivialTour.add(startNode);
            trivialTour.add(startNode);
            data.put("found", true);
            data.put("path", new ArrayList<>(trivialTour));
            data.put("distance", 0.0);
            data.put("tspTour", new ArrayList<>(trivialTour));
            data.put("tspCost", 0.0);
            return new AlgorithmResult(steps,
                "Tur TSP trivial: " + formatTour(trivialTour) + " (total bobot = 0)", data);
        }

        Set<Integer> visited = new HashSet<>();
        List<Integer> tour = new ArrayList<>();
        visited.add(startNode);
        tour.add(startNode);

        double totalCost = 0.0;
        int current = startNode;

        while (visited.size() < graph.getNodeCount()) {
            NeighborChoice choice = pickNearestUnvisited(graph, current, visited);
            if (choice == null) {
                steps.add(AlgorithmStep.log("Tidak ada edge ke node belum dikunjungi dari node " + current + "."));
                data.put("found", false);
                data.put("path", new ArrayList<>(tour));
                data.put("distance", Double.POSITIVE_INFINITY);
                data.put("tspTour", new ArrayList<>(tour));
                data.put("tspCost", Double.POSITIVE_INFINITY);
                return new AlgorithmResult(steps,
                    "Gagal membentuk tur Hamiltonian: graf tidak memiliki jalur lengkap dari node "
                        + current + ".",
                    data);
            }

            steps.add(AlgorithmStep.traverseEdge(current, choice.nextNode,
                "Pilih tetangga terdekat " + choice.nextNode
                    + " dari node " + current
                    + " (w = " + formatWeight(choice.weight) + ")"));

            totalCost += choice.weight;
            steps.add(AlgorithmStep.markPathEdge(current, choice.nextNode,
                "Masuk tur: " + current + " - " + choice.nextNode));

            current = choice.nextNode;
            visited.add(current);
            tour.add(current);
            steps.add(AlgorithmStep.markPathNode(current, "Kunjungi node " + current));
        }

        GraphEdge closingEdge = graph.getEdge(current, startNode);
        if (closingEdge == null) {
            steps.add(AlgorithmStep.log("Tidak ada edge untuk kembali ke node awal " + startNode + "."));
            data.put("found", false);
            data.put("path", new ArrayList<>(tour));
            data.put("distance", Double.POSITIVE_INFINITY);
            data.put("tspTour", new ArrayList<>(tour));
            data.put("tspCost", Double.POSITIVE_INFINITY);
            return new AlgorithmResult(steps,
                "Gagal menutup tur TSP: edge kembali ke node awal tidak tersedia.", data);
        }

        totalCost += closingEdge.getWeight();
        steps.add(AlgorithmStep.traverseEdge(current, startNode,
            "Kembali ke start " + startNode
                + " (w = " + formatWeight(closingEdge.getWeight()) + ")"));
        steps.add(AlgorithmStep.markPathEdge(current, startNode,
            "Menutup tur: " + current + " - " + startNode));
        steps.add(AlgorithmStep.markEnd(startNode, "Tur selesai di node awal " + startNode));

        tour.add(startNode);

        data.put("found", true);
        data.put("path", new ArrayList<>(tour));
        data.put("distance", totalCost);
        data.put("tspTour", new ArrayList<>(tour));
        data.put("tspCost", totalCost);

        String summary = "Tur TSP (Nearest Neighbor) ditemukan: " + formatTour(tour)
            + " (total bobot = " + formatWeight(totalCost) + ")";
        return new AlgorithmResult(steps, summary, data);
    }

    private NeighborChoice pickNearestUnvisited(Graph graph, int current, Set<Integer> visited) {
        int bestNode = -1;
        double bestWeight = Double.POSITIVE_INFINITY;

        for (int neighbor : graph.getNeighbors(current)) {
            if (visited.contains(neighbor)) {
                continue;
            }

            GraphEdge edge = graph.getEdge(current, neighbor);
            if (edge == null) {
                continue;
            }

            double w = edge.getWeight();
            if (bestNode == -1
                || w + EPS < bestWeight
                || (Math.abs(w - bestWeight) < EPS && neighbor < bestNode)) {
                bestNode = neighbor;
                bestWeight = w;
            }
        }

        if (bestNode == -1) {
            return null;
        }
        return new NeighborChoice(bestNode, bestWeight);
    }

    private String formatTour(List<Integer> tour) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tour.size(); i++) {
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(tour.get(i));
        }
        return sb.toString();
    }

    private String formatWeight(double value) {
        if (Math.abs(value - Math.rint(value)) < EPS) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private static final class NeighborChoice {
        private final int nextNode;
        private final double weight;

        private NeighborChoice(int nextNode, double weight) {
            this.nextNode = nextNode;
            this.weight = weight;
        }
    }
}