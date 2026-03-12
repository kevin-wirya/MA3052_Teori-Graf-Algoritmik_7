package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

/**
 * Girth — menentukan girth dari graf, yaitu panjang siklus terpendek.
 * Menggunakan BFS dari setiap node untuk menemukan siklus terpendek.
 * Jika graf tidak memiliki siklus (tree/forest), girth = ∞.
 */
public class GirthAlgorithm implements GraphAlgorithm {

    @Override public String getName() { return "Graph Girth"; }
    @Override public String getCategory() { return "Properties"; }

    @Override
    public String getDescription() {
        return "Menentukan girth dari graf, yaitu panjang siklus terpendek. "
             + "Jika graf tidak memiliki siklus (tree/forest), maka girth = ∞.";
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
            return new AlgorithmResult(steps, "Graf kosong, girth tidak terdefinisi.");
        }

        int girth = Integer.MAX_VALUE;
        List<Integer> girthCycle = null;

        // BFS from each node to find shortest cycle through that node
        for (int source : sortedIds) {
            steps.add(AlgorithmStep.visitNode(source,
                "Memulai BFS dari node " + source + " untuk mencari siklus terpendek."));

            Map<Integer, Integer> dist = new HashMap<>();
            Map<Integer, Integer> parent = new HashMap<>();
            Queue<Integer> queue = new LinkedList<>();

            dist.put(source, 0);
            parent.put(source, -1);
            queue.add(source);

            boolean foundCycle = false;

            while (!queue.isEmpty() && !foundCycle) {
                int current = queue.poll();

                for (int neighbor : graph.getNeighbors(current)) {
                    if (!dist.containsKey(neighbor)) {
                        // Tree edge
                        dist.put(neighbor, dist.get(current) + 1);
                        parent.put(neighbor, current);
                        queue.add(neighbor);

                        steps.add(AlgorithmStep.traverseEdge(current, neighbor,
                            "Edge " + current + " → " + neighbor
                            + ", jarak dari " + source + " = " + dist.get(neighbor)));

                    } else if (neighbor != parent.getOrDefault(current, -1)) {
                        // Back/cross edge → cycle detected
                        int cycleLength = dist.get(current) + dist.get(neighbor) + 1;

                        steps.add(AlgorithmStep.traverseEdge(current, neighbor,
                            "Siklus ditemukan via edge " + current + " → " + neighbor
                            + ", panjang = " + cycleLength));

                        if (cycleLength < girth) {
                            girth = cycleLength;
                            girthCycle = reconstructCycle(parent, current, neighbor, source);
                        }

                        // We found the shortest cycle through this source; skip further
                        foundCycle = true;
                        break;
                    }
                }
            }

            steps.add(AlgorithmStep.finishNode(source,
                foundCycle
                    ? "Siklus terpendek via node " + source + " ditemukan."
                    : "Tidak ada siklus baru ditemukan via node " + source + "."));
        }

        // Highlight the girth cycle
        if (girth != Integer.MAX_VALUE && girthCycle != null) {
            steps.add(AlgorithmStep.log("Girth cycle: " + girthCycle + " (panjang " + girth + ")"));
            for (int node : girthCycle) {
                steps.add(AlgorithmStep.markPathNode(node, "Node pada girth cycle"));
            }
            for (int i = 0; i < girthCycle.size(); i++) {
                int a = girthCycle.get(i);
                int b = girthCycle.get((i + 1) % girthCycle.size());
                steps.add(AlgorithmStep.markPathEdge(a, b, "Edge pada girth cycle"));
            }
        }

        String summary;
        Map<String, Object> data = new HashMap<>();
        if (girth == Integer.MAX_VALUE) {
            summary = "Graf tidak memiliki siklus (tree/forest). Girth = ∞.";
            data.put("girth", "∞");
            data.put("hasCycle", false);
        } else {
            summary = "Girth graf = " + girth + " (siklus terpendek: " + girthCycle + ").";
            data.put("girth", girth);
            data.put("hasCycle", true);
            data.put("girthCycle", girthCycle);
        }

        return new AlgorithmResult(steps, summary, data);
    }

    /**
     * Reconstruct the cycle found when BFS from 'source' discovers that
     * 'current' has a neighbor 'neighbor' already visited (and neighbor != parent[current]).
     * The cycle is: path(source→current) + edge(current→neighbor) + reverse(path(source→neighbor)).
     */
    private List<Integer> reconstructCycle(Map<Integer, Integer> parent,
                                           int current, int neighbor, int source) {
        // Path from source to current
        List<Integer> pathToCurrent = new ArrayList<>();
        for (int v = current; v != -1; v = parent.getOrDefault(v, -1)) {
            pathToCurrent.add(v);
        }
        Collections.reverse(pathToCurrent);

        // Path from source to neighbor
        List<Integer> pathToNeighbor = new ArrayList<>();
        for (int v = neighbor; v != -1; v = parent.getOrDefault(v, -1)) {
            pathToNeighbor.add(v);
        }
        Collections.reverse(pathToNeighbor);

        // Find LCA (lowest common ancestor in BFS tree)
        Set<Integer> ancestorsOfCurrent = new HashSet<>(pathToCurrent);
        int lca = source;
        for (int v : pathToNeighbor) {
            if (ancestorsOfCurrent.contains(v)) {
                lca = v;
            }
        }

        // Build cycle: lca → ... → current → neighbor → ... → lca
        List<Integer> cycle = new ArrayList<>();
        boolean adding = false;
        for (int v : pathToCurrent) {
            if (v == lca) adding = true;
            if (adding) cycle.add(v);
        }

        // Reverse path from neighbor back to lca
        List<Integer> reversePart = new ArrayList<>();
        adding = false;
        for (int v : pathToNeighbor) {
            if (v == lca) adding = true;
            if (adding) reversePart.add(v);
        }
        Collections.reverse(reversePart);
        // Skip the first element of reversePart (it's 'neighbor' end) — add all except the last (which is lca, already in cycle)
        for (int i = 0; i < reversePart.size() - 1; i++) {
            cycle.add(reversePart.get(i));
        }

        return cycle;
    }
}
