import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class ComponentCount {
    /**
     * DFS untuk menandai semua node dalam satu komponen
     * @param adj adjacency list
     * @param visited array untuk tracking node yang sudah dikunjungi
     * @param node node saat ini
     * @param componentNodes list untuk menyimpan node dalam komponen ini
     */
    static void dfsComponent(ArrayList<ArrayList<Integer>> adj, boolean[] visited, int node, ArrayList<Integer> componentNodes) {
        visited[node] = true;
        componentNodes.add(node);
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                dfsComponent(adj, visited, neighbor, componentNodes);
            }
        }
    }
    
    /**
     * Class untuk menyimpan informasi hasil penghitungan komponen
     */
    static class ComponentInfo {
        int componentCount;                              // Jumlah total komponen
        ArrayList<ArrayList<Integer>> allComponents;    // List semua komponen beserta node-nya
        
        ComponentInfo(int componentCount, ArrayList<ArrayList<Integer>> allComponents) {
            this.componentCount = componentCount;
            this.allComponents = allComponents;
        }
    }
    
    /**
     * Menghitung jumlah komponen dalam graf menggunakan DFS
     * @param adj adjacency list representasi graf
     * @return ComponentInfo berisi jumlah komponen dan detail setiap komponen
     */
    static ComponentInfo countComponents(ArrayList<ArrayList<Integer>> adj) {
        int n = adj.size();
        boolean[] visited = new boolean[n];
        ArrayList<ArrayList<Integer>> allComponents = new ArrayList<>();
        int componentCount = 0;
        
        // Iterasi semua node, setiap kali menemukan node yang belum dikunjungi
        // berarti ini adalah komponen baru
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                ArrayList<Integer> componentNodes = new ArrayList<>();
                dfsComponent(adj, visited, i, componentNodes);
                allComponents.add(componentNodes);
                componentCount++;
            }
        }
        return new ComponentInfo(componentCount, allComponents);
    }
    
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Masukkan nama file (tanpa .txt): ");
        String fileName = input.nextLine();
        try {
            String dataPath = "../../data/" + fileName + ".txt";
            Scanner fileScanner = new Scanner(new File(dataPath));
            int N = fileScanner.nextInt();
            int M = fileScanner.nextInt();
            ArrayList<ArrayList<Integer>> adj = new ArrayList<>();
            for (int i = 0; i < N; i++) adj.add(new ArrayList<>());
            System.out.println("Membaca " + M + " edges dari file " + fileName + ".txt");
            for (int i = 0; i < M; i++) {
                int u = fileScanner.nextInt();
                int v = fileScanner.nextInt();
                addEdge(adj, u, v);
            }
            fileScanner.close();
            
            ComponentInfo result = countComponents(adj);
            
            // Tampilkan hasil
            System.out.println("\n========================================");
            System.out.println("       HASIL PENGHITUNGAN KOMPONEN");
            System.out.println("========================================");
            System.out.println("Jumlah Komponen: " + result.componentCount);
            System.out.println("\nDetail setiap komponen:");
            for (int i = 0; i < result.allComponents.size(); i++) {
                ArrayList<Integer> component = result.allComponents.get(i);
                System.out.println("  Komponen " + (i + 1) + " (ukuran " + component.size() + "): " + component);
            }
            System.out.println("========================================\n");
            
            SwingUtilities.invokeLater(() -> new ComponentCountVisual(adj, result));
        } catch (FileNotFoundException e) {
            System.out.println("File tidak ditemukan: " + fileName + ".txt");
        } catch (Exception e) {
            System.out.println("Error saat membaca file: " + e.getMessage());
            e.printStackTrace();
        }
        input.close();
    }
    
    static void addEdge(ArrayList<ArrayList<Integer>> adj, int u, int v) {
        adj.get(u).add(v);
        adj.get(v).add(u);
    }
}
