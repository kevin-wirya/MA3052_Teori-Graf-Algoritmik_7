package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.AlgorithmResult;
import com.grafapp.algorithm.AlgorithmStep;
import com.grafapp.algorithm.GraphAlgorithm;
import com.grafapp.algorithm.ParameterInfo;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.grafapp.model.Graph;

/**
 * Maximum Bipartite Matching dengan M-alternating tree (augmenting path).
 */
public class BipartiteMaximumMatchingAlgorithm implements GraphAlgorithm {

    @Override
    public String getName() {
        return "Maximum Matching (Bipartite)";
    }

    @Override
    public String getCategory() {
        return "Matching";
    }

    @Override
    public String getDescription() {
        return "Mencari matching maksimal pada graf bipartit dengan M-alternating tree. "
            + "Jika ditemukan augmenting path, matching diperbesar hingga maksimum.";
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
            return new AlgorithmResult(steps, "Graf kosong, matching tidak dapat dihitung.", data);
        }

        Map<Integer, List<Integer>> adj = BipartiteMatchingHelper.buildAdjacency(graph);
        BipartiteMatchingHelper.PartitionResult partition = BipartiteMatchingHelper.buildPartition(adj);
        if (!partition.bipartite) {
            String summary = "Graf tidak bipartit. Konflik pada edge "
                + partition.conflictU + " - " + partition.conflictV + ".";
            return new AlgorithmResult(steps, summary, data);
        }

        List<Integer> left = new ArrayList<>(partition.left);
        Collections.sort(left);

        Map<Integer, Integer> matchTo = new HashMap<>();
        for (int id : adj.keySet()) {
            matchTo.put(id, -1);
        }

        HallWitness hallWitness = null;

        // Jalankan augmenting path sampai tidak ada lagi yang bisa diperbesar.
        while (true) {
            boolean augmented = false;
            for (int start : left) {
                if (matchTo.get(start) != -1) {
                    continue;
                }

                AugmentSearchResult search = findAugmentingPath(start, adj, partition.side, matchTo, steps);
                if (search.found) {
                    applyAugmentingPath(search.path, matchTo, steps);
                    augmented = true;
                    hallWitness = null;
                    break;
                }

                hallWitness = new HallWitness(start, search.reachedLeft, search.reachedRight);
                steps.add(AlgorithmStep.log(
                    "Tidak ada augmenting path dari " + start
                        + ", S=" + hallWitness.leftSet + ", N(S)=" + hallWitness.rightSet));
            }

            if (!augmented) {
                break;
            }
        }

        List<List<Integer>> matchingEdges = new ArrayList<>();
        for (int x : left) {
            int y = matchTo.getOrDefault(x, -1);
            if (y != -1) {
                matchingEdges.add(Arrays.asList(x, y));
            }
        }

        int matchingSize = matchingEdges.size();

        // Tampilkan matching akhir (sekali di akhir agar visual tidak salah).
        Set<Integer> matchedNodes = new HashSet<>();
        for (List<Integer> edge : matchingEdges) {
            steps.add(AlgorithmStep.markPathEdge(edge.get(0), edge.get(1),
                "Matching edge: " + edge.get(0) + " - " + edge.get(1)));
            matchedNodes.add(edge.get(0));
            matchedNodes.add(edge.get(1));
        }
        for (int nodeId : matchedNodes) {
            steps.add(AlgorithmStep.markPathNode(nodeId, "Matched node: " + nodeId));
        }

        data.put("matchingEdges", matchingEdges);
        data.put("matchingSize", matchingSize);

        String summary = "Matching maksimal ditemukan. Ukuran matching = " + matchingSize + ".";
        if (hallWitness != null && !hallWitness.leftSet.isEmpty()) {
            data.put("hallSet", new ArrayList<>(hallWitness.leftSet));
            data.put("hallNeighbors", new ArrayList<>(hallWitness.rightSet));
            summary += " Hall witness: S=" + hallWitness.leftSet
                + ", N(S)=" + hallWitness.rightSet + ".";
        }

        return new AlgorithmResult(steps, summary, data);
    }

    private AugmentSearchResult findAugmentingPath(
        int start,
        Map<Integer, List<Integer>> adj,
        Map<Integer, Integer> side,
        Map<Integer, Integer> matchTo,
        List<AlgorithmStep> steps
    ) {
        Queue<Integer> queue = new ArrayDeque<>();
        Map<Integer, Integer> parent = new HashMap<>();
        Set<Integer> reachedLeft = new HashSet<>();
        Set<Integer> reachedRight = new HashSet<>();

        queue.add(start);
        parent.put(start, -1);
        reachedLeft.add(start);
        steps.add(AlgorithmStep.markStart(start, "Mulai dari node bebas " + start));

        while (!queue.isEmpty()) {
            int x = queue.poll();
            steps.add(AlgorithmStep.processNode(x, "Proses node " + x));

            for (int y : adj.getOrDefault(x, Collections.emptyList())) {
                if (side.getOrDefault(y, 0) != 1) {
                    continue;
                }
                if (matchTo.getOrDefault(x, -1) == y) {
                    continue;
                }
                if (reachedRight.contains(y)) {
                    continue;
                }

                reachedRight.add(y);
                parent.put(y, x);
                steps.add(AlgorithmStep.traverseEdge(x, y, "Cek edge " + x + " - " + y));

                int matched = matchTo.getOrDefault(y, -1);
                if (matched == -1) {
                    List<Integer> path = reconstructPath(y, parent);
                    steps.add(AlgorithmStep.log("Augmenting path ditemukan: " + path));
                    return new AugmentSearchResult(true, path, reachedLeft, reachedRight);
                }

                if (!reachedLeft.contains(matched)) {
                    reachedLeft.add(matched);
                    parent.put(matched, y);
                    queue.add(matched);
                    steps.add(AlgorithmStep.traverseEdge(y, matched,
                        "Ikuti edge matching " + y + " - " + matched));
                }
            }
        }

        return new AugmentSearchResult(false, Collections.emptyList(), reachedLeft, reachedRight);
    }

    private List<Integer> reconstructPath(int end, Map<Integer, Integer> parent) {
        List<Integer> path = new ArrayList<>();
        int cursor = end;
        while (cursor != -1) {
            path.add(cursor);
            cursor = parent.getOrDefault(cursor, -1);
        }
        Collections.reverse(path);
        return path;
    }

    private void applyAugmentingPath(
        List<Integer> path,
        Map<Integer, Integer> matchTo,
        List<AlgorithmStep> steps
    ) {
        // Flip matching sepanjang augmenting path.
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i);
            int v = path.get(i + 1);
            if (matchTo.getOrDefault(u, -1) == v) {
                matchTo.put(u, -1);
                matchTo.put(v, -1);
                steps.add(AlgorithmStep.log("Keluarkan edge " + u + " - " + v + " dari matching"));
            } else {
                matchTo.put(u, v);
                matchTo.put(v, u);
                steps.add(AlgorithmStep.log("Masukkan edge " + u + " - " + v + " ke matching"));
            }
        }
    }

    private static final class AugmentSearchResult {
        private final boolean found;
        private final List<Integer> path;
        private final Set<Integer> reachedLeft;
        private final Set<Integer> reachedRight;

        private AugmentSearchResult(
            boolean found,
            List<Integer> path,
            Set<Integer> reachedLeft,
            Set<Integer> reachedRight
        ) {
            this.found = found;
            this.path = path;
            this.reachedLeft = reachedLeft;
            this.reachedRight = reachedRight;
        }
    }

    private static final class HallWitness {
        private final int origin;
        private final Set<Integer> leftSet;
        private final Set<Integer> rightSet;

        private HallWitness(int origin, Set<Integer> leftSet, Set<Integer> rightSet) {
            this.origin = origin;
            this.leftSet = leftSet;
            this.rightSet = rightSet;
        }
    }
}
