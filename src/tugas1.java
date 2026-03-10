package src;

import src.utils.GraphVisualizer;
import java.io.*;
import java.util.*;

public class tugas1 {
    private Map<Integer, List<Integer>> graph;
    private List<String> steps;  // Untuk menyimpan langkah-langkah algoritma
    private GraphVisualizer visualizer;
    
    public tugas1() {
        this.graph = new HashMap<>();
        this.steps = new ArrayList<>();
        this.visualizer = new GraphVisualizer();
    }
    
    // Membaca graph dari file
    public void readGraphFromFile(String filename) {
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
    
    // Algoritma DFS
    public List<Integer> dfs(int startNode) {
        List<Integer> visited = new ArrayList<>();
        Set<Integer> visitedSet = new HashSet<>();
        Stack<Integer> stack = new Stack<>();
        
        steps.clear();
        stack.push(startNode);
        steps.add("Push " + startNode + " to stack");
        
        while (!stack.isEmpty()) {
            int current = stack.pop();
            steps.add("Pop " + current + " from stack");
            
            if (!visitedSet.contains(current)) {
                visited.add(current);
                visitedSet.add(current);
                steps.add("Visit node " + current);
                
                // Add neighbors to stack in reverse order
                if (graph.containsKey(current)) {
                    List<Integer> neighbors = new ArrayList<>(graph.get(current));
                    Collections.sort(neighbors, Collections.reverseOrder());
                    
                    for (int neighbor : neighbors) {
                        if (!visitedSet.contains(neighbor)) {
                            stack.push(neighbor);
                            steps.add("Push " + neighbor + " to stack");
                        }
                    }
                }
            }
        }
        
        return visited;
    }
    
    // Algoritma BFS
    public List<Integer> bfs(int startNode) {
        List<Integer> visited = new ArrayList<>();
        Set<Integer> visitedSet = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        
        steps.clear();
        queue.offer(startNode);
        visitedSet.add(startNode);
        steps.add("Enqueue " + startNode + " to queue");
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            visited.add(current);
            steps.add("Dequeue " + current + " from queue and visit");
            
            if (graph.containsKey(current)) {
                List<Integer> neighbors = new ArrayList<>(graph.get(current));
                Collections.sort(neighbors);
                
                for (int neighbor : neighbors) {
                    if (!visitedSet.contains(neighbor)) {
                        visitedSet.add(neighbor);
                        queue.offer(neighbor);
                        steps.add("Enqueue " + neighbor + " to queue");
                    }
                }
            }
        }
        
        return visited;
    }
    
    public void printGraph() {
        System.out.println("Graph representation:");
        for (Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
    
    public void simulateAndVisualize(String filename) {
        readGraphFromFile(filename);
        
        if (graph.isEmpty()) {
            System.out.println("Graph is empty or file not found.");
            return;
        }
        
        int startNode = graph.keySet().iterator().next();  // Ambil node pertama sebagai start
        
        System.out.println("\n=== DFS and BFS Simulation ===");
        printGraph();
        
        // DFS Simulation
        System.out.println("\n--- DFS Traversal ---");
        List<Integer> dfsResult = dfs(startNode);
        System.out.println("DFS Result: " + dfsResult);
        System.out.println("Steps:");
        for (int i = 0; i < steps.size(); i++) {
            System.out.println((i+1) + ". " + steps.get(i));
        }
        
        // BFS Simulation
        System.out.println("\n--- BFS Traversal ---");
        List<Integer> bfsResult = bfs(startNode);
        System.out.println("BFS Result: " + bfsResult);
        System.out.println("Steps:");
        for (int i = 0; i < steps.size(); i++) {
            System.out.println((i+1) + ". " + steps.get(i));
        }
        
        System.out.println("\n📄 Open visualization.html in browser to see interactive visualization!");
    }
    
    public static void main(String[] args) {
        tugas1 simulator = new tugas1();
        
        // Test dengan file yang tersedia
        String[] testFiles = {"graph1.txt", "graph2.txt", "simple_graph.txt"};
        
        for (String filename : testFiles) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Testing with " + filename);
            System.out.println("=".repeat(50));
            simulator.simulateAndVisualize(filename);
        }
    }
}