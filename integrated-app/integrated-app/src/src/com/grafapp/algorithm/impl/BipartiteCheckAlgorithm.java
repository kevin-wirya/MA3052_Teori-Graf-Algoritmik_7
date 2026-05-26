package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

/**
 * Bipartite Check — mengecek apakah graf bipartit dan menentukan dua himpunan.
 * Menggunakan BFS coloring: jika bisa diwarnai 2 warna tanpa konflik, bipartit.
 */
public class BipartiteCheckAlgorithm implements GraphAlgorithm {

    @Override public String getName() { return "Bipartite Check"; }
    @Override public String getCategory() { return "Properties"; }

    @Override
    public String getDescription() {
        return "Mengecek apakah graf bipartit menggunakan BFS 2-coloring. "
             + "Jika bipartit, menampilkan dua himpunan node-nya.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Collections.emptyList();
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();
        Map<Integer, Integer> color = new HashMap<>(); // 0 atau 1
        List<Integer> setA = new ArrayList<>();
        List<Integer> setB = new ArrayList<>();
        boolean bipartite = true;
        int conflictU = -1, conflictV = -1;

        List<Integer> sortedIds = new ArrayList<>(graph.getNodeIds());
        Collections.sort(sortedIds);

        for (int startNode : sortedIds) {
            if (color.containsKey(startNode)) continue;

            Queue<Integer> queue = new LinkedList<>();
            queue.add(startNode);
            color.put(startNode, 0);
            setA.add(startNode);
            steps.add(AlgorithmStep.visitNode(startNode,
                "Mewarnai node " + startNode + " dengan warna A"));

            while (!queue.isEmpty() && bipartite) {
                int u = queue.poll();
                steps.add(AlgorithmStep.processNode(u, "Memproses node " + u));

                for (int v : graph.getNeighbors(u)) {
                    if (!color.containsKey(v)) {
                        int nextColor = 1 - color.get(u);
                        color.put(v, nextColor);
                        if (nextColor == 0) setA.add(v); else setB.add(v);
                        queue.add(v);

                        steps.add(AlgorithmStep.traverseEdge(u, v,
                            "Mewarnai node " + v + " dengan warna " + (nextColor == 0 ? "A" : "B")));
                        steps.add(AlgorithmStep.visitNode(v,
                            "Node " + v + " \u2192 Himpunan " + (nextColor == 0 ? "A" : "B")));
                    } else if (color.get(v).equals(color.get(u))) {
                        // Konflik: bukan bipartit
                        bipartite = false;
                        conflictU = u;
                        conflictV = v;
                        steps.add(AlgorithmStep.traverseEdge(u, v,
                            "KONFLIK! Node " + u + " dan " + v + " memiliki warna yang sama"));
                        steps.add(AlgorithmStep.markPathNode(u,
                            "Node konflik: " + u));
                        steps.add(AlgorithmStep.markPathNode(v,
                            "Node konflik: " + v));
                        break;
                    }
                }
            }
            if (!bipartite) break;
        }

        // Warnai hasil akhir
        if (bipartite) {
            // Himpunan A = COMPONENT_1, Himpunan B = COMPONENT_2
            if (!setA.isEmpty()) {
                steps.add(AlgorithmStep.markComponent(0, setA,
                    "Himpunan A: " + setA));
            }
            if (!setB.isEmpty()) {
                steps.add(AlgorithmStep.markComponent(1, setB,
                    "Himpunan B: " + setB));
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("bipartite", bipartite);

        StringBuilder sb = new StringBuilder();
        if (bipartite) {
            sb.append("Graf BIPARTIT!\n");
            sb.append("Himpunan A (").append(setA.size()).append(" node): ").append(setA).append("\n");
            sb.append("Himpunan B (").append(setB.size()).append(" node): ").append(setB);
            data.put("setA", setA);
            data.put("setB", setB);
        } else {
            sb.append("Graf TIDAK BIPARTIT!\n");
            sb.append("Konflik ditemukan pada edge ").append(conflictU).append(" \u2014 ").append(conflictV);
            data.put("conflictEdge", Arrays.asList(conflictU, conflictV));
        }

        return new AlgorithmResult(steps, sb.toString(), data);
    }
}
