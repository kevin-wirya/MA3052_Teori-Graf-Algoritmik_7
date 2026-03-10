import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.SwingUtilities;

public class PathFinder {
    
    static void dfsRecursive(ArrayList<ArrayList<Integer>> adj,
           boolean[] visited, int current, int dest, 
           ArrayList<Integer> path, ArrayList<Integer> result)
    {
        visited[current] = true;
        path.add(current);
        
        if (current == dest) {
            result.addAll(path);
            return;
        }
        
        for (int neighbor : adj.get(current)) {
            if (!visited[neighbor]) {
                dfsRecursive(adj, visited, neighbor, dest, path, result);
                if (!result.isEmpty()) return;
            }
        }
        
        path.remove(path.size() - 1);
    }

    static ArrayList<Integer> findPath(ArrayList<ArrayList<Integer>> adj, int startVertex, int endVertex) {
        boolean[] visited = new boolean[adj.size()];
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Integer> path = new ArrayList<>();
        dfsRecursive(adj, visited, startVertex, endVertex, path, result);
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
            int startVertex = fileScanner.nextInt();
            int endVertex = fileScanner.nextInt();
            fileScanner.close();
            
            System.out.println("Mencari lintasan dari vertex " + startVertex + " ke " + endVertex);
            ArrayList<Integer> path = findPath(adj, startVertex, endVertex);
            
            if (path.isEmpty()) {
                System.out.println("Tidak ada lintasan dari vertex " + startVertex + " ke " + endVertex);
            } else {
                System.out.print("Lintasan: ");
                for (int i = 0; i < path.size(); i++) {
                    System.out.print(path.get(i));
                    if (i < path.size() - 1) System.out.print(" -> ");
                }
                System.out.println();
                SwingUtilities.invokeLater(() -> new PathFinderVisual(adj, startVertex, endVertex, path));
            }
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
