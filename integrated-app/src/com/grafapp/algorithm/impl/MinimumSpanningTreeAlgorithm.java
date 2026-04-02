package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import com.grafapp.model.GraphEdge;
import java.util.*;

/**
 * Minimum Spanning Tree / Forest menggunakan algoritma Kruskal.
 */
public class MinimumSpanningTreeAlgorithm implements GraphAlgorithm {

    private static final double EPS = 1e-9;

    @Override public String getName() { return "Minimum Spanning Tree (Kruskal)"; }
    @Override public String getCategory() { return "Spanning Tree"; }

    @Override
    public String getDescription() {
        return "Membangun pohon pembangun minimal menggunakan algoritma Kruskal. "
             + "Jika graf tidak terhubung, hasilnya berupa minimum spanning forest.";
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
            steps.add(AlgorithmStep.log("Graf kosong — tidak ada MST yang dapat dibangun."));
            data.put("found", false);
            data.put("mstEdges", Collections.emptyList());
            data.put("mstWeight", 0.0);
            data.put("connected", false);
            return new AlgorithmResult(steps, "Graf kosong, MST tidak terdefinisi.", data);
        }

        if (graph.isDirected()) {
            steps.add(AlgorithmStep.log("Graf directed tidak didukung untuk MST Kruskal."));
            data.put("found", false);
            data.put("mstEdges", Collections.emptyList());
            data.put("mstWeight", 0.0);
            data.put("connected", false);
            return new AlgorithmResult(steps,
                "Minimum spanning tree hanya didefinisikan untuk graf undirected.", data);
        }

        List<Integer> nodeIds = new ArrayList<>(graph.getNodeIds());
        Collections.sort(nodeIds);

        List<WeightedEdge> edges = new ArrayList<>();
        for (GraphEdge edge : graph.getEdges()) {
            edges.add(new WeightedEdge(edge.getSource(), edge.getTarget(), edge.getWeight()));
        }
        edges.sort((a, b) -> {
            int cmp = Double.compare(a.weight, b.weight);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(Math.min(a.source, a.target), Math.min(b.source, b.target));
            if (cmp != 0) return cmp;
            cmp = Integer.compare(Math.max(a.source, a.target), Math.max(b.source, b.target));
            if (cmp != 0) return cmp;
            cmp = Integer.compare(a.source, b.source);
            if (cmp != 0) return cmp;
            return Integer.compare(a.target, b.target);
        });

        UnionFind uf = new UnionFind(nodeIds);
        List<List<Integer>> mstEdges = new ArrayList<>();
        double totalWeight = 0.0;

        for (WeightedEdge edge : edges) {
            String weightLabel = formatWeight(edge.weight);
            steps.add(AlgorithmStep.traverseEdge(edge.source, edge.target,
                "Mengevaluasi edge " + edge.source + " \u2192 " + edge.target
                + " (w = " + weightLabel + ")"));

            if (uf.union(edge.source, edge.target)) {
                mstEdges.add(Arrays.asList(edge.source, edge.target));
                totalWeight += edge.weight;
                steps.add(AlgorithmStep.markTreeEdge(edge.source, edge.target,
                    "Edge dipilih ke MST: " + edge.source + " - " + edge.target
                    + " (w = " + weightLabel + ")"));
                steps.add(AlgorithmStep.log("Edge diterima, total bobot sementara = "
                    + formatWeight(totalWeight)));
            } else {
                steps.add(AlgorithmStep.log("Edge ditolak karena membentuk siklus."));
                steps.add(AlgorithmStep.finishEdge(edge.source, edge.target, ""));
            }
        }

        boolean connected = uf.componentCount() <= 1;

        if (mstEdges.isEmpty()) {
            steps.add(AlgorithmStep.log("Tidak ada edge yang dipilih ke MST."));
        }

        for (List<Integer> edge : mstEdges) {
            if (edge.size() == 2) {
                steps.add(AlgorithmStep.markTreeEdge(edge.get(0), edge.get(1),
                    "Edge akhir MST: " + edge.get(0) + " - " + edge.get(1)));
            }
        }

        String summary;
        if (connected) {
            summary = "MST ditemukan dengan total bobot = " + formatWeight(totalWeight)
                + " (" + mstEdges.size() + " edge).";
        } else {
            summary = "Minimum spanning forest ditemukan dengan total bobot = "
                + formatWeight(totalWeight) + " (" + mstEdges.size() + " edge).";
        }

        data.put("found", true);
        data.put("mstEdges", mstEdges);
        data.put("mstWeight", totalWeight);
        data.put("connected", connected);
        data.put("componentCount", uf.componentCount());

        return new AlgorithmResult(steps, summary, data);
    }

    private String formatWeight(double value) {
        if (Math.abs(value - Math.rint(value)) < EPS) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private static final class WeightedEdge {
        private final int source;
        private final int target;
        private final double weight;

        private WeightedEdge(int source, int target, double weight) {
            this.source = source;
            this.target = target;
            this.weight = weight;
        }
    }

    private static final class UnionFind {
        private final Map<Integer, Integer> parent = new HashMap<>();
        private final Map<Integer, Integer> rank = new HashMap<>();

        private UnionFind(Collection<Integer> nodes) {
            for (int node : nodes) {
                parent.put(node, node);
                rank.put(node, 0);
            }
        }

        private int find(int x) {
            int root = parent.get(x);
            if (root != x) {
                root = find(root);
                parent.put(x, root);
            }
            return root;
        }

        private boolean union(int a, int b) {
            int rootA = find(a);
            int rootB = find(b);
            if (rootA == rootB) {
                return false;
            }

            int rankA = rank.get(rootA);
            int rankB = rank.get(rootB);
            if (rankA < rankB) {
                parent.put(rootA, rootB);
            } else if (rankA > rankB) {
                parent.put(rootB, rootA);
            } else {
                parent.put(rootB, rootA);
                rank.put(rootA, rankA + 1);
            }
            return true;
        }

        private int componentCount() {
            Set<Integer> roots = new HashSet<>();
            for (int node : parent.keySet()) {
                roots.add(find(node));
            }
            return roots.size();
        }
    }
}