import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.SwingUtilities;

public class Connectivity {
    
    static void dfsVisit(ArrayList<ArrayList<Integer>> adj, boolean[] visited, int node) {
        visited[node] = true;
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                dfsVisit(adj, visited, neighbor);
            }
        }
    }
    
    static boolean isConnected(ArrayList<ArrayList<Integer>> adj) {
        if (adj.isEmpty()) return true;
        boolean[] visited = new boolean[adj.size()];
        dfsVisit(adj, visited, 0);
        for (boolean v : visited) if (!v) return false;
        return true;
    }
    
    static ArrayList<Integer> getDFSTraversal(ArrayList<ArrayList<Integer>> adj) {
        ArrayList<Integer> result = new ArrayList<>();
        if (adj.isEmpty()) return result;
        boolean[] visited = new boolean[adj.size()];
        dfsTraversalHelper(adj, visited, 0, result);
        return result;
    }
    
    static void dfsTraversalHelper(ArrayList<ArrayList<Integer>> adj, boolean[] visited, int node, ArrayList<Integer> result) {
        visited[node] = true;
        result.add(node);
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) dfsTraversalHelper(adj, visited, neighbor, result);
        }
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
            
            boolean connected = isConnected(adj);
            System.out.println("\nApakah graf terhubung?");
            System.out.println(connected ? "Ya" : "Tidak");
            
            ArrayList<Integer> traversal = getDFSTraversal(adj);
            SwingUtilities.invokeLater(() -> new ConnectivityVisual(adj, traversal, connected));
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
