import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.SwingUtilities;

/**
 * Program untuk menghitung jumlah pulau dalam grid menggunakan teori graf.
 */
public class IslandCount {
    
    // Arah pergerakan: atas, bawah, kiri, kanan
    static int[] dRow = {-1, 1, 0, 0};
    static int[] dCol = {0, 0, -1, 1};
    
    /**
     * DFS untuk menandai semua sel dalam satu pulau
     * @param grid grid 2D
     * @param visited matrix untuk tracking sel yang sudah dikunjungi
     * @param row baris saat ini
     * @param col kolom saat ini
     * @param islandCells list untuk menyimpan koordinat sel dalam pulau ini
     */
    static void dfsIsland(int[][] grid, boolean[][] visited, int row, int col, ArrayList<int[]> islandCells) {
        int rows = grid.length;
        int cols = grid[0].length;
        
        // Base case: cek batas dan validitas
        if (row < 0 || row >= rows || col < 0 || col >= cols) return;
        if (visited[row][col] || grid[row][col] == 0) return;
        
        // Tandai sebagai dikunjungi dan tambahkan ke list
        visited[row][col] = true;
        islandCells.add(new int[]{row, col});
        
        // Rekursif ke 4 arah (edge dalam graf)
        for (int i = 0; i < 4; i++) {
            int newRow = row + dRow[i];
            int newCol = col + dCol[i];
            dfsIsland(grid, visited, newRow, newCol, islandCells);
        }
    }
    
    /**
     * Class untuk menyimpan informasi hasil penghitungan pulau
     */
    static class IslandInfo {
        int islandCount;                              // Jumlah total pulau
        ArrayList<ArrayList<int[]>> allIslands;      // List semua pulau beserta koordinat selnya
        int[][] grid;                                 // Grid asli
        
        IslandInfo(int islandCount, ArrayList<ArrayList<int[]>> allIslands, int[][] grid) {
            this.islandCount = islandCount;
            this.allIslands = allIslands;
            this.grid = grid;
        }
    }
    
    /**
     * Menghitung jumlah pulau dalam grid menggunakan DFS
     * @param grid grid 2D (0 = air, 1 = daratan)
     * @return IslandInfo berisi jumlah pulau dan detail setiap pulau
     */
    static IslandInfo countIslands(int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        ArrayList<ArrayList<int[]>> allIslands = new ArrayList<>();
        int islandCount = 0;
        
        // Iterasi semua sel dalam grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Jika sel adalah daratan dan belum dikunjungi, ini adalah pulau baru
                if (grid[i][j] == 1 && !visited[i][j]) {
                    ArrayList<int[]> islandCells = new ArrayList<>();
                    dfsIsland(grid, visited, i, j, islandCells);
                    allIslands.add(islandCells);
                    islandCount++;
                }
            }
        }
        
        return new IslandInfo(islandCount, allIslands, grid);
    }
    
    /**
     * Membaca grid dari file
     * Format file:
     * - Baris pertama: rows cols
     * - Baris selanjutnya: nilai grid (0 atau 1)
     */
    static int[][] readGridFromFile(String filePath) throws FileNotFoundException {
        Scanner fileScanner = new Scanner(new File(filePath));
        int rows = fileScanner.nextInt();
        int cols = fileScanner.nextInt();
        int[][] grid = new int[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = fileScanner.nextInt();
            }
        }
        fileScanner.close();
        return grid;
    }
    
    /**
     * Mencetak grid ke console
     */
    static void printGrid(int[][] grid) {
        System.out.println("Grid:");
        for (int[] row : grid) {
            for (int cell : row) {
                System.out.print(cell == 1 ? "█ " : "· ");
            }
            System.out.println();
        }
    }
    
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("   PENGHITUNGAN JUMLAH PULAU (ISLAND COUNT)");
        System.out.println("   Menggunakan Teori Graf - DFS Algorithm");
        System.out.println("==============================================");
        System.out.println();
        System.out.print("Masukkan nama file (tanpa .txt): ");
        String fileName = input.nextLine();
        
        int[][] grid;
        try {
            String dataPath = "../../data/" + fileName + ".txt";
            grid = readGridFromFile(dataPath);
            System.out.println("File berhasil dibaca!");
        } catch (FileNotFoundException e) {
            System.out.println("File tidak ditemukan: " + fileName + ".txt");
            input.close();
            return;
        }
        
        System.out.println();
        printGrid(grid);
        
        IslandInfo result = countIslands(grid);
        
        // Tampilkan hasil
        System.out.println();
        System.out.println("========================================");
        System.out.println("     HASIL PENGHITUNGAN PULAU");
        System.out.println("========================================");
        System.out.println("Jumlah Pulau: " + result.islandCount);
        System.out.println();
        System.out.println("Detail setiap pulau:");
        for (int i = 0; i < result.allIslands.size(); i++) {
            ArrayList<int[]> island = result.allIslands.get(i);
            System.out.print("  Pulau " + (i + 1) + " (" + island.size() + " sel): ");
            for (int[] cell : island) {
                System.out.print("(" + cell[0] + "," + cell[1] + ") ");
            }
            System.out.println();
        }
        
        // Tampilkan visualisasi
        SwingUtilities.invokeLater(() -> new IslandCountVisual(result));
        
        input.close();
    }
}
