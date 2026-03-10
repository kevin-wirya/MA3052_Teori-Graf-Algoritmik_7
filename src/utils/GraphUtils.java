package utils;

import java.util.ArrayList;

public class GraphUtils {
    /**
     * Menambahkan edge ke dalam adjacency list (undirected graph)
     * @param adj Adjacency list representation dari graph
     * @param u Node source
     * @param v Node destination
     */
    public static void addEdge(ArrayList<ArrayList<Integer>> adj, int u, int v) {
        adj.get(u).add(v);
        adj.get(v).add(u);
    }
}
