package com.grafapp.algorithm;

import com.grafapp.algorithm.impl.*;
import java.util.*;

/**
 * Registry/katalog semua algoritma yang tersedia.
 * Untuk menambahkan algoritma baru (misal Dijkstra, MST):
 *   1. Buat class baru yang implements GraphAlgorithm
 *   2. Daftarkan di constructor registry ini: register(new DijkstraAlgorithm())
 */
public class AlgorithmRegistry {
    private static final AlgorithmRegistry INSTANCE = new AlgorithmRegistry();
    private final List<GraphAlgorithm> algorithms = new ArrayList<>();

    private AlgorithmRegistry() {
        // -- Traversal --
        register(new DFSAlgorithm());
        register(new BFSAlgorithm());
        // -- Connectivity --
        register(new ConnectivityCheckAlgorithm());
        register(new ConnectedComponentsAlgorithm());
        register(new LargestComponentAlgorithm());
        // -- Path Finding --
        register(new PathFinderAlgorithm());
        // -- Properties --
        register(new BipartiteCheckAlgorithm());
        register(new CycleDetectionAlgorithm());
    }

    public static AlgorithmRegistry getInstance() { return INSTANCE; }

    public void register(GraphAlgorithm algorithm) {
        algorithms.add(algorithm);
    }

    public List<GraphAlgorithm> getAll() {
        return Collections.unmodifiableList(algorithms);
    }

    public List<GraphAlgorithm> getByCategory(String category) {
        List<GraphAlgorithm> result = new ArrayList<>();
        for (GraphAlgorithm algo : algorithms) {
            if (algo.getCategory().equals(category)) {
                result.add(algo);
            }
        }
        return result;
    }

    public List<String> getCategories() {
        Set<String> cats = new LinkedHashSet<>();
        for (GraphAlgorithm algo : algorithms) {
            cats.add(algo.getCategory());
        }
        return new ArrayList<>(cats);
    }
}
