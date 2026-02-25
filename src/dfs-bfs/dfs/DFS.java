import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.SwingUtilities;

public class DFS {
    
    static void dfsRecursive(ArrayList<ArrayList<Integer>> adj,
           boolean[] visited, int s, ArrayList<Integer> res)
    {
        visited[s] = true;
        res.add(s);
        for (int i : adj.get(s)) {
            if (!visited[i]) dfsRecursive(adj, visited, i, res);
        }
    }

    static ArrayList<Integer> dfs(ArrayList<ArrayList<Integer>> adj, int startVertex) {
        boolean[] visited = new boolean[adj.size()];
        ArrayList<Integer> res = new ArrayList<>();
        dfsRecursive(adj, visited, startVertex, res);
        return res;
    }
    
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Masukkan nama file (tanpa .txt): ");
        String fileName = input.nextLine();
        try {
            String dataPath = "../../../data/" + fileName + ".txt";
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
            int startVertex = fileScanner.nextInt();
            fileScanner.close();
            System.out.println("Hasil DFS mulai dari vertex " + startVertex + ":");
            ArrayList<Integer> res = dfs(adj, startVertex);
            for (int i = 0; i < res.size(); i++) {
                System.out.print(res.get(i) + " ");
            }
            System.out.println();
            SwingUtilities.invokeLater(() -> new DFSVisual(adj, startVertex, res));
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
