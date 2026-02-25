import java.util.ArrayList;
import java.util.Scanner;
import java.util.Queue;
import java.util.LinkedList;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.SwingUtilities;

public class BFS {
    static ArrayList<ArrayList<Integer>> bfsLevelByLevel(ArrayList<ArrayList<Integer>> adj, int startVertex) {
        boolean[] visited = new boolean[adj.size()];
        ArrayList<ArrayList<Integer>> levels = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        visited[startVertex] = true;
        queue.add(startVertex);
        while (!queue.isEmpty()) {
            ArrayList<Integer> currentLevel = new ArrayList<>();
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                int currentNode = queue.poll();
                currentLevel.add(currentNode);
                for (int neighbor : adj.get(currentNode)) {
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        queue.add(neighbor);
                    }
                }
            }
            levels.add(currentLevel);
        }
        return levels;
    }
    
    static ArrayList<Integer> bfs(ArrayList<ArrayList<Integer>> adj, int startVertex) {
        boolean[] visited = new boolean[adj.size()];
        ArrayList<Integer> res = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        visited[startVertex] = true;
        queue.add(startVertex);
        while (!queue.isEmpty()) {
            int currentNode = queue.poll();
            res.add(currentNode);
            for (int neighbor : adj.get(currentNode)) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.add(neighbor);
                }
            }
        }
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

            System.out.println("Hasil BFS mulai dari vertex " + startVertex + ":");
            ArrayList<Integer> res = bfs(adj, startVertex);
            for (int i = 0; i < res.size(); i++) System.out.print(res.get(i) + " ");
            System.out.println();
            ArrayList<ArrayList<Integer>> levels = bfsLevelByLevel(adj, startVertex);
            SwingUtilities.invokeLater(() -> new BFSVisual(adj, startVertex, res, levels));
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
