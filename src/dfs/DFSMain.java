package dfs;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Scanner;

/**
 * Kelas utama untuk demonstrasi implementasi dan visualisasi DFS
 */
public class DFSMain {
    
    public static void main(String[] args) {
        // Tampilkan menu
        showMenu();
    }
    
    /**
     * Menampilkan menu pilihan
     */
    private static void showMenu() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== IMPLEMENTASI DEPTH-FIRST SEARCH (DFS) ===");
        System.out.println("1. Demo dengan Graf Contoh");
        System.out.println("2. Buat Graf Custom");
        System.out.println("3. Keluar");
        System.out.print("Pilih opsi (1-3): ");
        
        try {
            int choice = scanner.nextInt();
            
            switch (choice) {
                case 1:
                    demoWithSampleGraph();
                    break;
                case 2:
                    createCustomGraph(scanner);
                    break;
                case 3:
                    System.out.println("Terima kasih!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Pilihan tidak valid!");
                    showMenu();
            }
        } catch (Exception e) {
            System.out.println("Input tidak valid!");
            scanner.nextLine(); // Clear buffer
            showMenu();
        }
    }
    
    /**
     * Demonstrasi dengan graf contoh yang sudah dibuat
     */
    private static void demoWithSampleGraph() {
        System.out.println("\n=== DEMO DENGAN GRAF CONTOH ===");
        
        // Membuat graf contoh
        Graph sampleGraph = createSampleGraph();
        sampleGraph.printGraph();
        
        // Demonstrasi DFS
        demonstrateDFS(sampleGraph);
        
        // Tampilkan visualisasi
        System.out.println("\nMembuka visualisasi...");
        SwingUtilities.invokeLater(() -> {
            DFSVisualizer.createVisualizationWindow(sampleGraph);
        });
        
        // Kembali ke menu
        System.out.println("\nTekan Enter untuk kembali ke menu...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
        showMenu();
    }
    
    /**
     * Membuat graf contoh
     */
    private static Graph createSampleGraph() {
        // Graf dengan 6 vertex
        Graph graph = new Graph(6);
        
        // Menambahkan edges
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(2, 5);
        graph.addEdge(3, 4);
        
        System.out.println("Graf contoh yang dibuat:");
        System.out.println("Vertices: 0, 1, 2, 3, 4, 5");
        System.out.println("Edges: (0,1), (0,2), (1,3), (1,4), (2,5), (3,4)");
        
        return graph;
    }
    
    /**
     * Membuat graf custom berdasarkan input user
     */
    private static void createCustomGraph(Scanner scanner) {
        System.out.println("\n=== BUAT GRAF CUSTOM ===");
        
        try {
            System.out.print("Masukkan jumlah vertex: ");
            int vertices = scanner.nextInt();
            
            if (vertices <= 0) {
                System.out.println("Jumlah vertex harus lebih dari 0!");
                return;
            }
            
            Graph graph = new Graph(vertices);
            
            System.out.print("Masukkan jumlah edge: ");
            int edges = scanner.nextInt();
            
            System.out.println("Masukkan edges (format: source destination):");
            System.out.println("Vertex dinomori dari 0 sampai " + (vertices - 1));
            
            for (int i = 0; i < edges; i++) {
                System.out.print("Edge " + (i + 1) + ": ");
                int source = scanner.nextInt();
                int dest = scanner.nextInt();
                
                if (source >= 0 && source < vertices && dest >= 0 && dest < vertices) {
                    graph.addEdge(source, dest);
                } else {
                    System.out.println("Vertex tidak valid! Skipping edge ini.");
                    i--; // Ulangi input untuk edge ini
                }
            }
            
            System.out.println("\nGraf yang dibuat:");
            graph.printGraph();
            
            // Demonstrasi DFS
            demonstrateDFS(graph);
            
            // Tampilkan visualisasi
            System.out.println("\nMembuka visualisasi...");
            SwingUtilities.invokeLater(() -> {
                DFSVisualizer.createVisualizationWindow(graph);
            });
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Kembali ke menu
        System.out.println("\nTekan Enter untuk kembali ke menu...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
        showMenu();
    }
    
    /**
     * Demonstrasi berbagai fungsi DFS
     */
    private static void demonstrateDFS(Graph graph) {
        DFS dfs = new DFS(graph);
        
        System.out.println("\n=== DEMONSTRASI DFS ===");
        
        // 1. DFS Rekursif
        System.out.println("\n1. DFS REKURSIF dari vertex 0:");
        System.out.println("-----------------------------");
        List<Integer> recursiveResult = dfs.dfsRecursive(0);
        
        // 2. DFS Iteratif
        System.out.println("\n2. DFS ITERATIF dari vertex 0:");
        System.out.println("------------------------------");
        List<Integer> iterativeResult = dfs.dfsIterative(0);
        
        // 3. Pencarian Path
        System.out.println("\n3. PENCARIAN PATH:");
        System.out.println("-----------------");
        if (graph.getVertices() > 1) {
            int start = 0;
            int end = graph.getVertices() - 1;
            System.out.println("Mencari path dari vertex " + start + " ke vertex " + end + ":");
            dfs.findPath(start, end);
        }
        
        // 4. Cek Konektivitas
        System.out.println("\n4. ANALISIS KONEKTIVITAS:");
        System.out.println("------------------------");
        boolean connected = dfs.isConnected();
        System.out.println("Graf terhubung: " + (connected ? "Ya" : "Tidak"));
        
        int components = dfs.countConnectedComponents();
        System.out.println("Jumlah komponen terhubung: " + components);
        
        // 5. Perbandingan hasil
        System.out.println("\n5. PERBANDINGAN HASIL:");
        System.out.println("---------------------");
        System.out.println("Rekursif : " + recursiveResult);
        System.out.println("Iteratif : " + iterativeResult);
        System.out.println("Sama?    : " + recursiveResult.equals(iterativeResult));
    }
    
    /**
     * Membuat beberapa contoh graf yang berbeda untuk testing
     */
    public static void createAdditionalSamples() {
        System.out.println("\n=== CONTOH GRAF TAMBAHAN ===");
        
        // Graf Linear
        System.out.println("\n1. Graf Linear (0-1-2-3):");
        Graph linearGraph = new Graph(4);
        linearGraph.addEdge(0, 1);
        linearGraph.addEdge(1, 2);
        linearGraph.addEdge(2, 3);
        linearGraph.printGraph();
        
        // Graf Star
        System.out.println("\n2. Graf Star (center: 0):");
        Graph starGraph = new Graph(5);
        starGraph.addEdge(0, 1);
        starGraph.addEdge(0, 2);
        starGraph.addEdge(0, 3);
        starGraph.addEdge(0, 4);
        starGraph.printGraph();
        
        // Graf Cycle
        System.out.println("\n3. Graf Cycle (0-1-2-3-0):");
        Graph cycleGraph = new Graph(4);
        cycleGraph.addEdge(0, 1);
        cycleGraph.addEdge(1, 2);
        cycleGraph.addEdge(2, 3);
        cycleGraph.addEdge(3, 0);
        cycleGraph.printGraph();
        
        // Graf Disconnected
        System.out.println("\n4. Graf Tidak Terhubung:");
        Graph disconnectedGraph = new Graph(6);
        disconnectedGraph.addEdge(0, 1);
        disconnectedGraph.addEdge(1, 2);
        disconnectedGraph.addEdge(3, 4);
        // Vertex 5 isolated
        disconnectedGraph.printGraph();
    }
}