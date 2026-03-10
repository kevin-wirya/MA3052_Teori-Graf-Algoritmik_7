package dfs;

import java.util.*;

/**
 * Kelas Graph untuk merepresentasikan graf menggunakan adjacency list
 */
public class Graph {
    private int vertices; // Jumlah vertex
    private LinkedList<Integer>[] adjList; // Adjacency list
    
    // Constructor
    @SuppressWarnings("unchecked")
    public Graph(int vertices) {
        this.vertices = vertices;
        adjList = new LinkedList[vertices];
        
        for (int i = 0; i < vertices; i++) {
            adjList[i] = new LinkedList<>();
        }
    }
    
    // Menambahkan edge ke graf (undirected)
    public void addEdge(int source, int destination) {
        adjList[source].add(destination);
        adjList[destination].add(source); // Untuk undirected graph
    }
    
    // Menambahkan edge ke graf (directed)
    public void addDirectedEdge(int source, int destination) {
        adjList[source].add(destination);
    }
    
    // Getter untuk adjacency list
    public LinkedList<Integer>[] getAdjList() {
        return adjList;
    }
    
    // Getter untuk jumlah vertices
    public int getVertices() {
        return vertices;
    }
    
    // Menampilkan representasi graf
    public void printGraph() {
        System.out.println("Representasi Graf (Adjacency List):");
        for (int i = 0; i < vertices; i++) {
            System.out.print("Vertex " + i + ": ");
            for (int neighbor : adjList[i]) {
                System.out.print(neighbor + " ");
            }
            System.out.println();
        }
    }
    
    // Mendapatkan tetangga dari sebuah vertex
    public List<Integer> getNeighbors(int vertex) {
        if (vertex >= 0 && vertex < vertices) {
            return new ArrayList<>(adjList[vertex]);
        }
        return new ArrayList<>();
    }
}