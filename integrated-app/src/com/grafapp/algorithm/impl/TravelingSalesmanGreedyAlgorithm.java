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
            + "Memeriksa semua node sebagai titik awal, cepat (O(n^3)) namun hasil mungkin bukan yang paling optimal.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Collections.emptyList();
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

        if (graph.getNodeCount() == 1) {
            int startNode = nodeIds.get(0);
            List<Integer> trivialTour = Arrays.asList(startNode, startNode);
            putSuccessData(data, trivialTour, 0.0);
            return new AlgorithmResult(steps,
                "Tur TSP trivial: " + formatTour(trivialTour) + " (total bobot = 0)", data);
        }

        steps.add(AlgorithmStep.log("Menggunakan pendekatan Greedy (Nearest Neighbor) untuk semua start node."));

        GreedyResult best = null;
        int bestStart = -1;
        for (int startCandidate : nodeIds) {
            GreedyResult candidate = computeGreedyTour(graph, startCandidate);
            if (!candidate.found) {
                continue;
            }
            if (best == null || candidate.totalCost + EPS < best.totalCost
                || (Math.abs(candidate.totalCost - best.totalCost) < EPS && startCandidate < bestStart)) {
                best = candidate;
                bestStart = startCandidate;
            }
        }

        if (best == null) {
            steps.add(AlgorithmStep.log("Tidak ada tur Hamiltonian yang valid dari semua start node."));
            putFailureData(data, Collections.emptyList());
            return new AlgorithmResult(steps, "Tur gagal: graf tidak memiliki tur TSP valid.", data);
        }

        steps.add(AlgorithmStep.log(
            "Start terbaik: " + bestStart + " (total bobot = " + formatWeight(best.totalCost) + ")"));
        steps.add(AlgorithmStep.markStart(bestStart, "Mulai tur terbaik dari node " + bestStart));
        steps.add(AlgorithmStep.markPathNode(bestStart, "Node awal tur: " + bestStart));

        List<Integer> tour = best.tour;
        for (int i = 1; i < tour.size(); i++) {
            int from = tour.get(i - 1);
            int to = tour.get(i);
            GraphEdge edge = graph.getEdge(from, to);
            double weight = edge != null ? edge.getWeight() : INF;
            steps.add(AlgorithmStep.traverseEdge(from, to,
                "Greedy pilih edge " + from + " -> " + to + " (w=" + formatWeight(weight) + ")"));
            steps.add(AlgorithmStep.markPathEdge(from, to, "Masuk tur: " + from + " - " + to));
            if (i < tour.size() - 1) {
                steps.add(AlgorithmStep.markPathNode(to, "Terkunjungi: " + to));
            }
        }
        steps.add(AlgorithmStep.markEnd(bestStart, "Tur selesai."));

        putSuccessData(data, tour, best.totalCost);

        String summary = "Tur TSP (Greedy) terbaik dari start " + bestStart + ": "
            + formatTour(tour) + " (total bobot = " + formatWeight(best.totalCost) + ")";
        return new AlgorithmResult(steps, summary, data);
    }

    private GreedyResult computeGreedyTour(Graph graph, int startNode) {
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
                        if (edge.getWeight() < bestWeight
                            || (Math.abs(edge.getWeight() - bestWeight) < EPS && neighbor < bestNext)) {
                            bestWeight = edge.getWeight();
                            bestNext = neighbor;
                        }
                    }
                }
            }

            if (bestNext == -1) {
                return new GreedyResult(false, tour, INF);
            }

            tour.add(bestNext);
            visited.add(bestNext);
            totalCost += bestWeight;
            current = bestNext;
        }

        GraphEdge closingEdge = graph.getEdge(current, startNode);
        if (closingEdge == null || !Double.isFinite(closingEdge.getWeight())) {
            return new GreedyResult(false, tour, INF);
        }

        tour.add(startNode);
        totalCost += closingEdge.getWeight();

        return new GreedyResult(true, tour, totalCost);
    }

    private static final class GreedyResult {
        private final boolean found;
        private final List<Integer> tour;
        private final double totalCost;

        private GreedyResult(boolean found, List<Integer> tour, double totalCost) {
            this.found = found;
            this.tour = tour;
            this.totalCost = totalCost;
        }
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
