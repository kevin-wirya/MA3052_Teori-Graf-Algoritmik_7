package dfs;

import java.util.*;

/**
 * Kelas untuk implementasi algoritma Depth-First Search (DFS)
 * Menyediakan implementasi rekursif dan iteratif
 */
public class DFS {
    private Graph graph;
    private boolean[] visited;
    private List<Integer> traversalOrder;
    private List<List<Integer>> stepByStep;
    
    public DFS(Graph graph) {
        this.graph = graph;
        this.visited = new boolean[graph.getVertices()];
        this.traversalOrder = new ArrayList<>();
        this.stepByStep = new ArrayList<>();
    }
    
    /**
     * DFS Rekursif
     * @param startVertex vertex awal untuk memulai DFS
     * @return list urutan kunjungan vertex
     */
    public List<Integer> dfsRecursive(int startVertex) {
        // Reset data
        visited = new boolean[graph.getVertices()];
        traversalOrder = new ArrayList<>();
        stepByStep = new ArrayList<>();
        
        System.out.println("Memulai DFS Rekursif dari vertex " + startVertex);
        dfsRecursiveUtil(startVertex);
        
        System.out.println("Urutan kunjungan: " + traversalOrder);
        return new ArrayList<>(traversalOrder);
    }
    
    /**
     * Utility function untuk DFS rekursif
     */
    private void dfsRecursiveUtil(int vertex) {
        // Tandai vertex sebagai dikunjungi
        visited[vertex] = true;
        traversalOrder.add(vertex);
        stepByStep.add(new ArrayList<>(traversalOrder));
        
        System.out.println("Mengunjungi vertex: " + vertex);
        
        // Kunjungi semua tetangga yang belum dikunjungi
        for (int neighbor : graph.getNeighbors(vertex)) {
            if (!visited[neighbor]) {
                System.out.println("Melanjutkan ke vertex: " + neighbor + " dari vertex: " + vertex);
                dfsRecursiveUtil(neighbor);
            }
        }
    }
    
    /**
     * DFS Iteratif menggunakan Stack
     * @param startVertex vertex awal untuk memulai DFS
     * @return list urutan kunjungan vertex
     */
    public List<Integer> dfsIterative(int startVertex) {
        // Reset data
        visited = new boolean[graph.getVertices()];
        traversalOrder = new ArrayList<>();
        stepByStep = new ArrayList<>();
        
        Stack<Integer> stack = new Stack<>();
        
        System.out.println("Memulai DFS Iteratif dari vertex " + startVertex);
        
        // Push vertex awal ke stack
        stack.push(startVertex);
        
        while (!stack.isEmpty()) {
            // Pop vertex dari stack
            int currentVertex = stack.pop();
            
            // Jika belum dikunjungi
            if (!visited[currentVertex]) {
                // Tandai sebagai dikunjungi
                visited[currentVertex] = true;
                traversalOrder.add(currentVertex);
                stepByStep.add(new ArrayList<>(traversalOrder));
                
                System.out.println("Mengunjungi vertex: " + currentVertex);
                
                // Push semua tetangga yang belum dikunjungi ke stack
                // Diurutkan secara descending agar hasil konsisten dengan DFS rekursif
                List<Integer> neighbors = graph.getNeighbors(currentVertex);
                neighbors.sort(Collections.reverseOrder());
                
                for (int neighbor : neighbors) {
                    if (!visited[neighbor]) {
                        stack.push(neighbor);
                        System.out.println("Menambahkan vertex " + neighbor + " ke stack");
                    }
                }
                
                System.out.println("Isi stack: " + stack);
            }
        }
        
        System.out.println("Urutan kunjungan: " + traversalOrder);
        return new ArrayList<>(traversalOrder);
    }
    
    /**
     * DFS untuk mencari path antara dua vertex
     * @param start vertex awal
     * @param target vertex tujuan
     * @return path dari start ke target, null jika tidak ada path
     */
    public List<Integer> findPath(int start, int target) {
        visited = new boolean[graph.getVertices()];
        List<Integer> path = new ArrayList<>();
        
        if (findPathUtil(start, target, path)) {
            System.out.println("Path ditemukan: " + path);
            return path;
        } else {
            System.out.println("Path tidak ditemukan dari " + start + " ke " + target);
            return null;
        }
    }
    
    /**
     * Utility function untuk mencari path
     */
    private boolean findPathUtil(int current, int target, List<Integer> path) {
        visited[current] = true;
        path.add(current);
        
        if (current == target) {
            return true; // Target ditemukan
        }
        
        for (int neighbor : graph.getNeighbors(current)) {
            if (!visited[neighbor]) {
                if (findPathUtil(neighbor, target, path)) {
                    return true;
                }
            }
        }
        
        // Backtrack
        path.remove(path.size() - 1);
        return false;
    }
    
    /**
     * Mengecek apakah graf terhubung (connected)
     * @return true jika graf terhubung
     */
    public boolean isConnected() {
        // Lakukan DFS dari vertex 0
        visited = new boolean[graph.getVertices()];
        dfsRecursiveUtil(0);
        
        // Cek apakah semua vertex terkunjungi
        for (boolean v : visited) {
            if (!v) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Menghitung jumlah komponen terhubung
     * @return jumlah komponen terhubung
     */
    public int countConnectedComponents() {
        visited = new boolean[graph.getVertices()];
        int components = 0;
        
        for (int i = 0; i < graph.getVertices(); i++) {
            if (!visited[i]) {
                dfsRecursiveUtil(i);
                components++;
            }
        }
        
        return components;
    }
    
    // Getter untuk step-by-step traversal (untuk visualisasi)
    public List<List<Integer>> getStepByStep() {
        return stepByStep;
    }
    
    // Getter untuk traversal order
    public List<Integer> getTraversalOrder() {
        return traversalOrder;
    }
}