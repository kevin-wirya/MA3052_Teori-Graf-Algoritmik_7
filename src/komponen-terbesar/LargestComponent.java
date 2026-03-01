import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.SwingUtilities;

public class LargestComponent {
    static int dfsComponent(ArrayList<ArrayList<Integer>> adj, boolean[] visited, int node, ArrayList<Integer> componentNodes) {
        visited[node] = true;
        componentNodes.add(node);
        int size = 1;
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                size += dfsComponent(adj, visited, neighbor, componentNodes);
            }
        }
        return size;
    }
    
    static class ComponentInfo {
        int largestSize;
        int startNode;
        ArrayList<Integer> largestComponentNodes;
        ArrayList<ArrayList<Integer>> allComponents;
        
        ComponentInfo(int largestSize, int startNode, ArrayList<Integer> largestComponentNodes, ArrayList<ArrayList<Integer>> allComponents) {
            this.largestSize = largestSize;
            this.startNode = startNode;
            this.largestComponentNodes = largestComponentNodes;
            this.allComponents = allComponents;
        }
    }
    
    static ComponentInfo findLargestComponent(ArrayList<ArrayList<Integer>> adj) {
        int n = adj.size();
        boolean[] visited = new boolean[n];
        int largestSize = 0;
        int startNode = -1;
        ArrayList<Integer> largestComponentNodes = new ArrayList<>();
        ArrayList<ArrayList<Integer>> allComponents = new ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                ArrayList<Integer> componentNodes = new ArrayList<>();
                int componentSize = dfsComponent(adj, visited, i, componentNodes);
                allComponents.add(componentNodes);
                
                if (componentSize > largestSize) {
                    largestSize = componentSize;
                    startNode = i;
                    largestComponentNodes = new ArrayList<>(componentNodes);
                }
            }
        }
        return new ComponentInfo(largestSize, startNode, largestComponentNodes, allComponents);
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
            
            ComponentInfo result = findLargestComponent(adj);
            System.out.println("\nUkuran Komponen Terbesar: " + result.largestSize);
            System.out.println("Node-node dalam komponen terbesar: " + result.largestComponentNodes);
            System.out.println("Node awal: " + result.startNode);
            System.out.println("Jumlah total komponen: " + result.allComponents.size());
            
            SwingUtilities.invokeLater(() -> new LargestComponentVisual(adj, result));
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
