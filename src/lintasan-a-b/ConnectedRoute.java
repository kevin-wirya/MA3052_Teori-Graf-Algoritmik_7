import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class ConnectedRoute {
    
    static Map.Entry<ArrayList<Integer>, Boolean> dfsVisit(ArrayList<ArrayList<Integer>> adj, boolean[] visited, int node, int target) {
        visited[node] = true;
        ArrayList<Integer> path = new ArrayList<>();
        path.add(node);
        System.out.println("Visiting node: " + node);
        if (node == target) return Map.entry(path, true);
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                Map.Entry<ArrayList<Integer>, Boolean> subResult = dfsVisit(adj, visited, neighbor, target);
                if (subResult.getValue()) {
                    path.addAll(subResult.getKey());
                    return Map.entry(path, true);
                }
            }
        }
        return Map.entry(path, false);
    }
    
    static Map.Entry<ArrayList<Integer>, Boolean> isRouteConnected(ArrayList<ArrayList<Integer>> adj, Integer a, Integer b) {
        if (adj.isEmpty()) return Map.entry(new ArrayList<>(), false);
        boolean[] visited = new boolean[adj.size()];
        Map.Entry<ArrayList<Integer>, Boolean> result = dfsVisit(adj, visited, a, b);
        return result;
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

            // Get input of a and b
            System.out.print("Masukkan node a: ");
            int a = input.nextInt();
            System.out.print("Masukkan node b: ");
            int b = input.nextInt();
            
            Map.Entry<ArrayList<Integer>, Boolean> result = isRouteConnected(adj, a, b);
            ArrayList<Integer> path = result.getKey();
            boolean connected = result.getValue();
            System.out.println("\nApakah lintasan a dan b terhubung?");
            System.out.println(connected ? "Ya" : "Tidak");
            
            SwingUtilities.invokeLater(() -> new ConnectedRouteVisual(adj, path, connected));
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
