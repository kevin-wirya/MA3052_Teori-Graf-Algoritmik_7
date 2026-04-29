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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class TravelingSalesmanGreedyAlgorithm implements GraphAlgorithm {

    private static final double EPS = 1e-9;
    private static final double INF = Double.POSITIVE_INFINITY;

    @Override
    public String getName() {
        return "Traveling Salesman (Greedy)";
    }

    @Override
    public String getCategory() {
        return "Optimization";
    }

    @Override
    public String getDescription() {
        return "Mencari pendekatan tur TSP menggunakan algoritma Greedy (Nearest Neighbor). "
            + "Sangat cepat (O(n^2)) namun hasil mungkin bukan yang paling optimal.";
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
            putFailureData(data, Collections.emptyList());
            return new AlgorithmResult(steps, "Graf kosong, tur TSP tidak dapat dibangun.", data);
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

        steps.add(AlgorithmStep.log("Menggunakan pendekatan Greedy (Nearest Neighbor)."));
        steps.add(AlgorithmStep.markStart(startNode, "Mulai tur dari node " + startNode));
        steps.add(AlgorithmStep.markPathNode(startNode, "Node awal tur: " + startNode));

        if (graph.getNodeCount() == 1) {
            List<Integer> trivialTour = new ArrayList<>();
            trivialTour.add(startNode);
            trivialTour.add(startNode);
            putSuccessData(data, trivialTour, 0.0);
            return new AlgorithmResult(steps,
                "Tur TSP trivial: " + formatTour(trivialTour) + " (total bobot = 0)", data);
        }

        Set<Integer> visited = new HashSet<>();
        List<Integer> tour = new ArrayList<>();
        tour.add(startNode);
        visited.add(startNode);

        int current = startNode;
        double totalCost = 0.0;

        while (visited.size() < graph.getNodeCount()) {
            double bestWeight = INF;
            int bestNext = -1;

            for (int neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    GraphEdge edge = graph.getEdge(current, neighbor);
                    if (edge != null && Double.isFinite(edge.getWeight())) {
                        if (edge.getWeight() < bestWeight || (Math.abs(edge.getWeight() - bestWeight) < EPS && neighbor < bestNext)) {
                            bestWeight = edge.getWeight();
                            bestNext = neighbor;
                        }
                    }
                }
            }

            if (bestNext == -1) {
                steps.add(AlgorithmStep.log("Jalan buntu! Tidak ada rute ke node yang belum dikunjungi. Graf tidak terhubung sempurna."));
                putFailureData(data, tour);
                return new AlgorithmResult(steps, "Tur gagal: graf tidak memiliki jalur Hamiltonian.", data);
            }

            steps.add(AlgorithmStep.traverseEdge(current, bestNext, "Greedy pilih tetangga terdekat: " + bestNext + " (w=" + formatWeight(bestWeight) + ")"));
            steps.add(AlgorithmStep.markPathEdge(current, bestNext, "Masuk tur: " + current + " - " + bestNext));
            steps.add(AlgorithmStep.markPathNode(bestNext, "Terkunjungi: " + bestNext));
            
            tour.add(bestNext);
            visited.add(bestNext);
            totalCost += bestWeight;
            current = bestNext;
        }

        // Return to start
        GraphEdge closingEdge = graph.getEdge(current, startNode);
        if (closingEdge == null || !Double.isFinite(closingEdge.getWeight())) {
            steps.add(AlgorithmStep.log("Tidak ada jalur kembali dari node terakhir " + current + " ke node awal " + startNode + "."));
            putFailureData(data, tour);
            return new AlgorithmResult(steps, "Tur gagal: tidak bisa kembali ke titik awal.", data);
        }

        steps.add(AlgorithmStep.traverseEdge(current, startNode, "Kembali ke awal: " + current + " -> " + startNode + " (w=" + formatWeight(closingEdge.getWeight()) + ")"));
        steps.add(AlgorithmStep.markPathEdge(current, startNode, "Selesai: kembali ke start"));
        steps.add(AlgorithmStep.markEnd(startNode, "Tur selesai."));
        
        tour.add(startNode);
        totalCost += closingEdge.getWeight();

        putSuccessData(data, tour, totalCost);

        String summary = "Tur TSP (Greedy) selesai: " + formatTour(tour)
            + " (total bobot = " + formatWeight(totalCost) + ")";
        return new AlgorithmResult(steps, summary, data);
    }

    private void putSuccessData(Map<String, Object> data, List<Integer> tour, double cost) {
        data.put("found", true);
        data.put("path", new ArrayList<>(tour));
        data.put("distance", cost);
        data.put("tspTour", new ArrayList<>(tour));
        data.put("tspCost", cost);
    }

    private void putFailureData(Map<String, Object> data, List<Integer> tour) {
        List<Integer> safeTour = tour == null ? Collections.emptyList() : new ArrayList<>(tour);
        data.put("found", false);
        data.put("path", safeTour);
        data.put("distance", INF);
        data.put("tspTour", safeTour);
        data.put("tspCost", INF);
    }

    private String formatTour(List<Integer> tour) {
        if (tour.size() > 10) {
            return tour.get(0) + " -> " + tour.get(1) + " -> ... (" + (tour.size() - 2) + " nodes) -> " + tour.get(tour.size() - 1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tour.size(); i++) {
            if (i > 0) sb.append(" -> ");
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
}
