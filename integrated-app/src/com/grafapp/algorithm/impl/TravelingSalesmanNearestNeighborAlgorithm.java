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

/**
 * Traveling Salesman Problem exact dengan Dynamic Programming (Held-Karp).
 */
public class TravelingSalesmanNearestNeighborAlgorithm implements GraphAlgorithm {

    private static final double EPS = 1e-9;
    private static final double INF = Double.POSITIVE_INFINITY;
    private static final int NO_PARENT = -1;
    private static final int MAX_EXACT_NODES = 20;

    @Override
    public String getName() {
        return "Traveling Salesman (Exact DP)";
    }

    @Override
    public String getCategory() {
        return "Optimization";
    }

    @Override
    public String getDescription() {
        return "Mencari tur TSP optimal global dengan algoritma Held-Karp (dynamic programming). "
            + "Pendekatan ini exact (bukan brute force), diulang untuk semua start node.";
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

        if (graph.getNodeCount() > MAX_EXACT_NODES) {
            steps.add(AlgorithmStep.log(
                "Jumlah node " + graph.getNodeCount()
                    + " melebihi batas exact solver (" + MAX_EXACT_NODES + ")."));
            putFailureData(data, Collections.emptyList());
            return new AlgorithmResult(steps,
                "Graf terlalu besar untuk TSP exact Held-Karp. "
                    + "Kurangi jumlah node agar solusi global dapat dihitung.",
                data);
        }

        steps.add(AlgorithmStep.log("Menggunakan Held-Karp dynamic programming (exact) untuk semua start node."));

        int dpStateCount = 1 << (nodeIds.size() - 1);
        steps.add(AlgorithmStep.log("State DP per start: " + dpStateCount + " subset."));

        HeldKarpResult bestResult = null;
        List<Integer> bestTour = null;
        int bestStart = -1;

        for (int startNode : nodeIds) {
            List<Integer> orderedNodeIds = buildOrderedNodeList(nodeIds, startNode);
            double[][] weights = buildWeightMatrix(graph, orderedNodeIds);

            HeldKarpResult result = solveHeldKarp(weights, orderedNodeIds);
            if (result == null) {
                continue;
            }

            List<Integer> tour = toNodeIdTour(result.tourNodeIndices, orderedNodeIds);
            if (bestResult == null || result.totalCost + EPS < bestResult.totalCost
                || (Math.abs(result.totalCost - bestResult.totalCost) < EPS && startNode < bestStart)) {
                bestResult = result;
                bestTour = tour;
                bestStart = startNode;
            }
        }

        if (bestResult == null || bestTour == null) {
            steps.add(AlgorithmStep.log(
                "Tidak ditemukan tur Hamiltonian yang mengunjungi semua node dan kembali ke start."));
            putFailureData(data, Collections.emptyList());
            return new AlgorithmResult(steps,
                "Tidak ada tur TSP valid yang mengunjungi semua node dan kembali ke node awal.",
                data);
        }

        steps.add(AlgorithmStep.log(
            "Start terbaik: " + bestStart + " (total bobot = " + formatWeight(bestResult.totalCost) + ")"));
        steps.add(AlgorithmStep.markStart(bestStart, "Mulai tur dari node " + bestStart));
        steps.add(AlgorithmStep.markPathNode(bestStart, "Node awal tur: " + bestStart));

        steps.add(AlgorithmStep.log("Tur optimal global ditemukan. Menandai edge tur akhir."));
        for (int i = 1; i < bestTour.size(); i++) {
            int fromNode = bestTour.get(i - 1);
            int toNode = bestTour.get(i);
            GraphEdge edge = graph.getEdge(fromNode, toNode);
            double edgeWeight = edge != null ? edge.getWeight() : INF;

            steps.add(AlgorithmStep.traverseEdge(fromNode, toNode,
                "Pilih edge tur optimal " + fromNode + " -> " + toNode
                    + " (w = " + formatWeight(edgeWeight) + ")"));
            steps.add(AlgorithmStep.markPathEdge(fromNode, toNode,
                "Masuk tur optimal: " + fromNode + " - " + toNode));

            if (i < bestTour.size() - 1) {
                steps.add(AlgorithmStep.markPathNode(toNode, "Node dalam tur optimal: " + toNode));
            }
        }
        steps.add(AlgorithmStep.markEnd(bestStart, "Tur selesai di node awal " + bestStart));

        putSuccessData(data, bestTour, bestResult.totalCost);

        String summary = "Tur TSP optimal (Held-Karp) terbaik dari start " + bestStart + ": "
            + formatTour(bestTour) + " (total bobot = " + formatWeight(bestResult.totalCost) + ")";
        return new AlgorithmResult(steps, summary, data);
    }

    private HeldKarpResult solveHeldKarp(double[][] weights, List<Integer> orderedNodeIds) {
        int n = orderedNodeIds.size();
        int otherCount = n - 1;
        int stateCount = 1 << otherCount;

        double[] dp = new double[stateCount * otherCount];
        int[] parent = new int[stateCount * otherCount];
        Arrays.fill(dp, INF);
        Arrays.fill(parent, NO_PARENT);

        // Base state: dari start langsung ke salah satu node tujuan.
        for (int j = 0; j < otherCount; j++) {
            double direct = weights[0][j + 1];
            if (Double.isFinite(direct)) {
                int index = flatIndex(1 << j, j, otherCount);
                dp[index] = direct;
                parent[index] = NO_PARENT;
            }
        }

        for (int mask = 1; mask < stateCount; mask++) {
            for (int j = 0; j < otherCount; j++) {
                if ((mask & (1 << j)) == 0) {
                    continue;
                }

                int prevMask = mask ^ (1 << j);
                if (prevMask == 0) {
                    continue;
                }

                int currentIndex = flatIndex(mask, j, otherCount);
                double bestCost = dp[currentIndex];
                int bestPrev = parent[currentIndex];

                for (int k = 0; k < otherCount; k++) {
                    if ((prevMask & (1 << k)) == 0) {
                        continue;
                    }

                    double prevCost = dp[flatIndex(prevMask, k, otherCount)];
                    double edgeCost = weights[k + 1][j + 1];
                    if (!Double.isFinite(prevCost) || !Double.isFinite(edgeCost)) {
                        continue;
                    }

                    double candidate = prevCost + edgeCost;
                    int candidateTieNodeId = orderedNodeIds.get(k + 1);
                    int incumbentTieNodeId = bestPrev == NO_PARENT
                        ? Integer.MAX_VALUE
                        : orderedNodeIds.get(bestPrev + 1);

                    if (isBetterCandidate(
                        candidate,
                        candidateTieNodeId,
                        bestCost,
                        incumbentTieNodeId
                    )) {
                        bestCost = candidate;
                        bestPrev = k;
                    }
                }

                if (Double.isFinite(bestCost)) {
                    dp[currentIndex] = bestCost;
                    parent[currentIndex] = bestPrev;
                }
            }
        }

        int fullMask = stateCount - 1;
        double bestTourCost = INF;
        int bestLast = NO_PARENT;

        for (int j = 0; j < otherCount; j++) {
            double pathCost = dp[flatIndex(fullMask, j, otherCount)];
            double closingCost = weights[j + 1][0];
            if (!Double.isFinite(pathCost) || !Double.isFinite(closingCost)) {
                continue;
            }

            double candidate = pathCost + closingCost;
            int candidateTieNodeId = orderedNodeIds.get(j + 1);
            int incumbentTieNodeId = bestLast == NO_PARENT
                ? Integer.MAX_VALUE
                : orderedNodeIds.get(bestLast + 1);

            if (isBetterCandidate(candidate, candidateTieNodeId, bestTourCost, incumbentTieNodeId)) {
                bestTourCost = candidate;
                bestLast = j;
            }
        }

        if (!Double.isFinite(bestTourCost)) {
            return null;
        }

        List<Integer> reversePath = new ArrayList<>();
        int mask = fullMask;
        int cursor = bestLast;
        while (cursor != NO_PARENT) {
            reversePath.add(cursor + 1);
            int parentIndex = parent[flatIndex(mask, cursor, otherCount)];
            mask ^= (1 << cursor);
            cursor = parentIndex;
        }
        Collections.reverse(reversePath);

        List<Integer> tourNodeIndices = new ArrayList<>();
        tourNodeIndices.add(0);
        tourNodeIndices.addAll(reversePath);
        tourNodeIndices.add(0);

        return new HeldKarpResult(tourNodeIndices, bestTourCost);
    }

    private boolean isBetterCandidate(
        double candidateCost,
        int candidateTieNodeId,
        double incumbentCost,
        int incumbentTieNodeId
    ) {
        if (candidateCost + EPS < incumbentCost) {
            return true;
        }
        return Math.abs(candidateCost - incumbentCost) < EPS
            && candidateTieNodeId < incumbentTieNodeId;
    }

    private int flatIndex(int mask, int endpoint, int width) {
        return mask * width + endpoint;
    }

    private List<Integer> buildOrderedNodeList(List<Integer> sortedNodeIds, int startNode) {
        List<Integer> ordered = new ArrayList<>();
        ordered.add(startNode);
        for (int nodeId : sortedNodeIds) {
            if (nodeId != startNode) {
                ordered.add(nodeId);
            }
        }
        return ordered;
    }

    private double[][] buildWeightMatrix(Graph graph, List<Integer> orderedNodeIds) {
        int n = orderedNodeIds.size();
        double[][] weights = new double[n][n];

        for (int i = 0; i < n; i++) {
            Arrays.fill(weights[i], INF);
            weights[i][i] = 0.0;
        }

        for (int i = 0; i < n; i++) {
            int fromNode = orderedNodeIds.get(i);
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                int toNode = orderedNodeIds.get(j);
                GraphEdge edge = graph.getEdge(fromNode, toNode);
                if (edge != null && Double.isFinite(edge.getWeight())) {
                    weights[i][j] = edge.getWeight();
                }
            }
        }
        return weights;
    }

    private List<Integer> toNodeIdTour(List<Integer> tourNodeIndices, List<Integer> orderedNodeIds) {
        List<Integer> tour = new ArrayList<>();
        for (int index : tourNodeIndices) {
            tour.add(orderedNodeIds.get(index));
        }
        return tour;
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

    private static final class HeldKarpResult {
        private final List<Integer> tourNodeIndices;
        private final double totalCost;

        private HeldKarpResult(List<Integer> tourNodeIndices, double totalCost) {
            this.tourNodeIndices = tourNodeIndices;
            this.totalCost = totalCost;
        }
    }
}