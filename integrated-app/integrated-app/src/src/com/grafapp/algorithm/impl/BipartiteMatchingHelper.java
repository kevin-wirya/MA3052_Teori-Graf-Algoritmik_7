package com.grafapp.algorithm.impl;

import com.grafapp.model.Graph;
import com.grafapp.model.GraphEdge;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

final class BipartiteMatchingHelper {

    private BipartiteMatchingHelper() {}

    static Map<Integer, List<Integer>> buildAdjacency(Graph graph) {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (int id : graph.getNodeIds()) {
            adj.put(id, new ArrayList<>());
        }
        for (GraphEdge edge : graph.getEdges()) {
            adj.computeIfAbsent(edge.getSource(), k -> new ArrayList<>()).add(edge.getTarget());
            adj.computeIfAbsent(edge.getTarget(), k -> new ArrayList<>()).add(edge.getSource());
        }
        for (List<Integer> neighbors : adj.values()) {
            Collections.sort(neighbors);
        }
        return adj;
    }

    static PartitionResult buildPartition(Map<Integer, List<Integer>> adj) {
        Map<Integer, Integer> side = new HashMap<>();
        Set<Integer> left = new HashSet<>();
        Set<Integer> right = new HashSet<>();

        int conflictU = -1;
        int conflictV = -1;
        boolean bipartite = true;

        for (int start : adj.keySet()) {
            if (side.containsKey(start)) {
                continue;
            }
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(start);
            side.put(start, 0);
            left.add(start);

            while (!queue.isEmpty() && bipartite) {
                int u = queue.poll();
                int uSide = side.get(u);
                for (int v : adj.getOrDefault(u, Collections.emptyList())) {
                    if (!side.containsKey(v)) {
                        int vSide = 1 - uSide;
                        side.put(v, vSide);
                        if (vSide == 0) {
                            left.add(v);
                        } else {
                            right.add(v);
                        }
                        queue.add(v);
                    } else if (side.get(v) == uSide) {
                        bipartite = false;
                        conflictU = u;
                        conflictV = v;
                        break;
                    }
                }
            }
            if (!bipartite) {
                break;
            }
        }

        return new PartitionResult(bipartite, side, left, right, conflictU, conflictV);
    }

    static final class PartitionResult {
        final boolean bipartite;
        final Map<Integer, Integer> side;
        final Set<Integer> left;
        final Set<Integer> right;
        final int conflictU;
        final int conflictV;

        private PartitionResult(
            boolean bipartite,
            Map<Integer, Integer> side,
            Set<Integer> left,
            Set<Integer> right,
            int conflictU,
            int conflictV
        ) {
            this.bipartite = bipartite;
            this.side = side;
            this.left = left;
            this.right = right;
            this.conflictU = conflictU;
            this.conflictV = conflictV;
        }
    }
}
