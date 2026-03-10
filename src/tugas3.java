package src;

import src.utils.GraphVisualizer;
import java.io.*;
import java.util.*;

public class tugas3 {
    private Map<Integer, List<Integer>> graph;
    private List<List<Integer>> connectedComponents;
    private GraphVisualizer visualizer;
    
    public tugas3() {
        this.graph = new HashMap<>();
        this.connectedComponents = new ArrayList<>();
        this.visualizer = new GraphVisualizer();
    }
    
    // Membaca graph dari file
    public void readGraphFromFile(String filename) {
        graph.clear();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/" + filename));
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    int from = Integer.parseInt(parts[0]);
                    int to = Integer.parseInt(parts[1]);
                    
                    graph.putIfAbsent(from, new ArrayList<>());
                    graph.putIfAbsent(to, new ArrayList<>());
                    
                    graph.get(from).add(to);
                    graph.get(to).add(from);  // Undirected graph
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
    
    // DFS untuk menemukan connected component
    private void dfsConnectedComponent(int node, Set<Integer> visited, List<Integer> component) {
        visited.add(node);
        component.add(node);
        
        if (graph.containsKey(node)) {
            for (int neighbor : graph.get(node)) {
                if (!visited.contains(neighbor)) {
                    dfsConnectedComponent(neighbor, visited, component);
                }
            }
        }
    }
    
    // Menemukan semua connected components
    public void findConnectedComponents() {
        connectedComponents.clear();
        Set<Integer> visited = new HashSet<>();
        
        // Get all nodes
        Set<Integer> allNodes = new HashSet<>(graph.keySet());
        
        for (int node : allNodes) {
            if (!visited.contains(node)) {
                List<Integer> component = new ArrayList<>();
                dfsConnectedComponent(node, visited, component);
                Collections.sort(component);
                connectedComponents.add(component);
            }
        }
    }
    
    // Mengecek apakah dua node terhubung
    public boolean areNodesConnected(int node1, int node2) {
        if (!graph.containsKey(node1) || !graph.containsKey(node2)) {
            return false;
        }
        
        Set<Integer> visited = new HashSet<>();
        return dfsReachability(node1, node2, visited);
    }
    
    // DFS untuk mengecek reachability
    private boolean dfsReachability(int current, int target, Set<Integer> visited) {
        if (current == target) {
            return true;
        }
        
        visited.add(current);
        
        if (graph.containsKey(current)) {
            for (int neighbor : graph.get(current)) {
                if (!visited.contains(neighbor)) {
                    if (dfsReachability(neighbor, target, visited)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    // Mengecek apakah graph terhubung
    public boolean isGraphConnected() {
        findConnectedComponents();
        return connectedComponents.size() == 1;
    }
    
    // Menghitung bridge edges
    public List<String> findBridges() {
        List<String> bridges = new ArrayList<>();
        Set<String> checkedEdges = new HashSet<>();
        
        for (int node : graph.keySet()) {
            List<Integer> neighbors = new ArrayList<>(graph.get(node)); // Create copy to avoid ConcurrentModificationException
            for (int neighbor : neighbors) {
                if (node < neighbor) {  // Avoid duplicate checking
                    String edge = node + "-" + neighbor;
                    if (!checkedEdges.contains(edge)) {
                        checkedEdges.add(edge);
                        if (isBridge(node, neighbor)) {
                            bridges.add(edge);
                        }
                    }
                }
            }
        }
        
        return bridges;
    }
    
    // Mengecek apakah edge adalah bridge
    private boolean isBridge(int u, int v) {
        // Simpan edges asli untuk restore nanti
        List<Integer> uEdges = new ArrayList<>(graph.get(u));
        List<Integer> vEdges = new ArrayList<>(graph.get(v));
        
        // Hapus edge sementara
        graph.get(u).removeAll(Collections.singleton(v));
        graph.get(v).removeAll(Collections.singleton(u));
        
        // Check connectivity
        Set<Integer> visited = new HashSet<>();
        boolean connected = dfsReachability(u, v, visited);
        
        // Kembalikan edges asli
        graph.put(u, uEdges);
        graph.put(v, vEdges);
        
        return !connected;
    }
    
    // Menghitung articulation points
    public List<Integer> findArticulationPoints() {
        List<Integer> articulationPoints = new ArrayList<>();
        List<Integer> nodeList = new ArrayList<>(graph.keySet()); // Create copy to avoid ConcurrentModificationException
        
        for (int node : nodeList) {
            if (isArticulationPoint(node)) {
                articulationPoints.add(node);
            }
        }
        
        Collections.sort(articulationPoints);
        return articulationPoints;
    }
    
    // Mengecek apakah node adalah articulation point
    private boolean isArticulationPoint(int node) {
        // Simpan edges asli
        List<Integer> originalEdges = new ArrayList<>(graph.get(node));
        Map<Integer, List<Integer>> originalGraph = new HashMap<>();
        
        // Reset connected components count
        int originalComponents = connectedComponents.size();
        
        // Backup dan hapus node sementara
        originalGraph.put(node, originalEdges);
        for (int neighbor : originalEdges) {
            if (graph.containsKey(neighbor)) {
                originalGraph.put(neighbor, new ArrayList<>(graph.get(neighbor)));
                graph.get(neighbor).removeAll(Collections.singleton(node));
            }
        }
        graph.remove(node);
        
        // Hitung connected components setelah removal
        findConnectedComponents();
        int newComponents = connectedComponents.size();
        
        // Kembalikan node dan edges asli
        for (Map.Entry<Integer, List<Integer>> entry : originalGraph.entrySet()) {
            graph.put(entry.getKey(), entry.getValue());
        }
        
        // Recompute original components
        findConnectedComponents();
        
        return newComponents > originalComponents;
    }
    
    public void printGraph() {
        System.out.println("Graph representation:");
        for (Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
    
    public void analyzeConnectivity(String filename) {
        readGraphFromFile(filename);
        
        if (graph.isEmpty()) {
            System.out.println("Graph is empty or file not found.");
            return;
        }
        
        System.out.println("\n=== Graph Connectivity Analysis ===");
        printGraph();
        
        // Analisis connected components
        findConnectedComponents();
        System.out.println("\n--- Connected Components ---");
        System.out.println("Number of connected components: " + connectedComponents.size());
        for (int i = 0; i < connectedComponents.size(); i++) {
            System.out.println("Component " + (i+1) + ": " + connectedComponents.get(i));
        }
        
        // Check if graph is connected
        boolean isConnected = isGraphConnected();
        System.out.println("\n--- Graph Connectivity ---");
        System.out.println("Is graph connected? " + (isConnected ? "YES" : "NO"));
        
        // Bridge analysis
        List<String> bridges = findBridges();
        System.out.println("\n--- Bridge Edges ---");
        if (bridges.isEmpty()) {
            System.out.println("No bridge edges found.");
        } else {
            System.out.println("Bridge edges: " + bridges);
        }
        
        // Articulation points analysis
        List<Integer> artPoints = findArticulationPoints();
        System.out.println("\n--- Articulation Points ---");
        if (artPoints.isEmpty()) {
            System.out.println("No articulation points found.");
        } else {
            System.out.println("Articulation points: " + artPoints);
        }
        
        System.out.println("\n📄 Open visualization.html in browser to see interactive analysis!");
    }
    
    public static void main(String[] args) {
        tugas3 analyzer = new tugas3();
        
        // Test dengan file yang tersedia
        String[] testFiles = {"graph1.txt", "graph2.txt", "disconnected_graph.txt", "bridge_graph.txt"};
        
        for (String filename : testFiles) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Analyzing connectivity for " + filename);
            System.out.println("=".repeat(60));
            analyzer.analyzeConnectivity(filename);
        }
    }
}