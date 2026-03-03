import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Visualisasi penghitungan pulau dalam grid.
 * Menampilkan animasi DFS yang menyelusuri setiap pulau.
 */
public class IslandCountVisual extends JFrame {
    private GridPanel gridPanel;
    
    public IslandCountVisual(IslandCount.IslandInfo islandInfo) {
        setTitle("Visualisasi Jumlah Pulau - Total: " + islandInfo.islandCount + " pulau");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);
        setResizable(true);
        
        gridPanel = new GridPanel(islandInfo);
        add(gridPanel);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(40, 40, 50));
        controlPanel.setPreferredSize(new Dimension(900, 150));
        
        JLabel titleLabel = new JLabel("PENGHITUNGAN PULAU - TEORI GRAF");
        titleLabel.setForeground(new Color(100, 200, 255));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel infoLabel = new JLabel("Tekan SPACE untuk mulai animasi DFS | Tekan R untuk reset");
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String resultText = "Jumlah Total Pulau (Komponen Terhubung): " + islandInfo.islandCount;
        JLabel resultLabel = new JLabel(resultText);
        resultLabel.setForeground(new Color(255, 220, 100));
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Build island details string
        StringBuilder islandDetails = new StringBuilder("Detail: ");
        for (int i = 0; i < islandInfo.allIslands.size(); i++) {
            islandDetails.append("Pulau ").append(i + 1).append("(").append(islandInfo.allIslands.get(i).size()).append(" sel)");
            if (i < islandInfo.allIslands.size() - 1) {
                islandDetails.append(", ");
            }
        }
        JLabel detailLabel = new JLabel(islandDetails.toString());
        detailLabel.setForeground(new Color(200, 200, 200));
        detailLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(titleLabel);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(resultLabel);
        controlPanel.add(Box.createVerticalStrut(6));
        controlPanel.add(detailLabel);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(infoLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        
        add(controlPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
}

class GridPanel extends JPanel {
    private IslandCount.IslandInfo islandInfo;
    private int[][] grid;
    private int rows, cols;
    private int cellSize = 40;
    private int offsetX, offsetY;
    
    // Tracking untuk animasi
    private boolean[][] visited;
    private int[] cellIslandIndex; // mapping flat index ke island index
    private ArrayList<int[]> visitedCells = new ArrayList<>();
    private ArrayList<int[]> currentDFSPath = new ArrayList<>();
    private volatile boolean isAnimating = false;
    private volatile boolean stopAnimation = false;
    private volatile Thread animationThread = null;
    private int currentIslandIndex = -1;
    
    // Warna berbeda untuk setiap pulau
    private Color[] islandColors = {
        new Color(255, 100, 100),   // Merah
        new Color(100, 200, 100),   // Hijau
        new Color(100, 150, 255),   // Biru
        new Color(255, 200, 50),    // Kuning
        new Color(200, 100, 255),   // Ungu
        new Color(100, 220, 220),   // Cyan
        new Color(255, 150, 100),   // Orange
        new Color(255, 100, 200),   // Pink
        new Color(180, 255, 150),   // Light Green
        new Color(200, 180, 100)    // Olive
    };
    
    public GridPanel(IslandCount.IslandInfo islandInfo) {
        this.islandInfo = islandInfo;
        this.grid = islandInfo.grid;
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.visited = new boolean[rows][cols];
        
        // Build mapping dari koordinat ke island index
        this.cellIslandIndex = new int[rows * cols];
        for (int i = 0; i < rows * cols; i++) {
            cellIslandIndex[i] = -1;
        }
        for (int i = 0; i < islandInfo.allIslands.size(); i++) {
            for (int[] cell : islandInfo.allIslands.get(i)) {
                cellIslandIndex[cell[0] * cols + cell[1]] = i;
            }
        }
        
        setBackground(new Color(30, 60, 90)); // Ocean blue
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && !isAnimating) {
                    startAnimation();
                } else if (e.getKeyCode() == KeyEvent.VK_R) {
                    resetAnimation();
                }
            }
        });
        
        setFocusable(true);
    }
    
    private void startAnimation() {
        isAnimating = true;
        stopAnimation = false;
        
        animationThread = new Thread(() -> {
            // Animasi per pulau
            for (int islandIdx = 0; islandIdx < islandInfo.allIslands.size(); islandIdx++) {
                if (stopAnimation) {
                    isAnimating = false;
                    return;
                }
                
                currentIslandIndex = islandIdx;
                ArrayList<int[]> island = islandInfo.allIslands.get(islandIdx);
                
                // Animasi DFS untuk setiap sel dalam pulau
                for (int cellIdx = 0; cellIdx < island.size(); cellIdx++) {
                    if (stopAnimation) {
                        isAnimating = false;
                        return;
                    }
                    
                    int[] cell = island.get(cellIdx);
                    visited[cell[0]][cell[1]] = true;
                    visitedCells.add(cell);
                    currentDFSPath.add(cell);
                    
                    repaint();
                    
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        isAnimating = false;
                        return;
                    }
                }
                
                // Clear DFS path setelah selesai satu pulau
                currentDFSPath.clear();
                repaint();
                
                // Pause sebentar sebelum pindah ke pulau berikutnya
                try {
                    Thread.sleep(600);
                } catch (InterruptedException ex) {
                    isAnimating = false;
                    return;
                }
            }
            
            currentIslandIndex = -1;
            isAnimating = false;
            repaint();
        });
        animationThread.start();
    }
    
    private void resetAnimation() {
        stopAnimation = true;
        isAnimating = false;
        if (animationThread != null) {
            animationThread.interrupt();
        }
        visited = new boolean[rows][cols];
        visitedCells.clear();
        currentDFSPath.clear();
        currentIslandIndex = -1;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Calculate cell size to fit grid
        int maxCellWidth = (width - 100) / cols;
        int maxCellHeight = (height - 100) / rows;
        cellSize = Math.min(Math.min(maxCellWidth, maxCellHeight), 50);
        
        // Center the grid
        offsetX = (width - cols * cellSize) / 2;
        offsetY = (height - rows * cellSize) / 2;
        
        // Draw grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = offsetX + j * cellSize;
                int y = offsetY + i * cellSize;
                
                if (grid[i][j] == 0) {
                    // Air - biru gelap
                    g2d.setColor(new Color(30, 80, 120));
                } else {
                    // Daratan
                    int islandIdx = cellIslandIndex[i * cols + j];
                    boolean isVisited = visited[i][j];
                    
                    if (isVisited) {
                        // Warna pulau yang sudah dikunjungi
                        g2d.setColor(islandColors[islandIdx % islandColors.length]);
                    } else {
                        // Daratan yang belum dikunjungi - coklat muda
                        g2d.setColor(new Color(180, 150, 100));
                    }
                }
                
                g2d.fillRect(x, y, cellSize, cellSize);
                
                // Border
                g2d.setColor(new Color(50, 50, 70));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(x, y, cellSize, cellSize);
                
                // Jika sedang dalam DFS path, tambahkan highlight
                if (isInCurrentPath(i, j)) {
                    g2d.setColor(new Color(255, 255, 255, 150));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRect(x + 2, y + 2, cellSize - 4, cellSize - 4);
                }
            }
        }
        
        // Draw edges (connections) untuk sel yang sedang di-visit dalam komponen yang sama
        if (currentDFSPath.size() > 1) {
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.setStroke(new BasicStroke(2));
            for (int i = 1; i < currentDFSPath.size(); i++) {
                int[] prev = currentDFSPath.get(i - 1);
                int[] curr = currentDFSPath.get(i);
                
                // Cek apakah bertetangga
                if (areNeighbors(prev, curr)) {
                    int x1 = offsetX + prev[1] * cellSize + cellSize / 2;
                    int y1 = offsetY + prev[0] * cellSize + cellSize / 2;
                    int x2 = offsetX + curr[1] * cellSize + cellSize / 2;
                    int y2 = offsetY + curr[0] * cellSize + cellSize / 2;
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }
        
        // Legend
        int legendX = 15;
        int legendY = 30;
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Legend:", legendX, legendY);
        
        // Air
        g2d.setColor(new Color(30, 80, 120));
        g2d.fillRect(legendX, legendY + 10, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Air (0)", legendX + 20, legendY + 22);
        
        // Daratan belum dikunjungi
        g2d.setColor(new Color(180, 150, 100));
        g2d.fillRect(legendX, legendY + 30, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Daratan belum dikunjungi", legendX + 20, legendY + 42);
        
        // Pulau yang sudah dikunjungi
        if (islandInfo.allIslands.size() > 0) {
            int itemY = legendY + 55;
            g2d.setColor(Color.WHITE);
            g2d.drawString("Pulau yang dikunjungi:", legendX, itemY);
            
            for (int i = 0; i < Math.min(islandInfo.allIslands.size(), 6); i++) {
                int yPos = itemY + 15 + (i * 20);
                g2d.setColor(islandColors[i % islandColors.length]);
                g2d.fillRect(legendX, yPos, 15, 15);
                g2d.setColor(Color.WHITE);
                g2d.drawString("Pulau " + (i + 1), legendX + 20, yPos + 12);
            }
        }
        
        // Current status
        if (currentIslandIndex >= 0) {
            String status = "Menghitung Pulau " + (currentIslandIndex + 1) + "...";
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int statusX = (width - fm.stringWidth(status)) / 2;
            g2d.drawString(status, statusX, 25);
        } else if (!isAnimating && visitedCells.size() > 0) {
            String status = "Selesai! Total: " + islandInfo.islandCount + " pulau ditemukan";
            g2d.setColor(new Color(100, 255, 100));
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int statusX = (width - fm.stringWidth(status)) / 2;
            g2d.drawString(status, statusX, 25);
        }
        
        // Grid info
        String gridInfo = "Grid: " + rows + " x " + cols;
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.drawString(gridInfo, width - 80, 25);
    }
    
    private boolean isInCurrentPath(int row, int col) {
        for (int[] cell : currentDFSPath) {
            if (cell[0] == row && cell[1] == col) {
                return true;
            }
        }
        return false;
    }
    
    private boolean areNeighbors(int[] a, int[] b) {
        int dr = Math.abs(a[0] - b[0]);
        int dc = Math.abs(a[1] - b[1]);
        return (dr == 1 && dc == 0) || (dr == 0 && dc == 1);
    }
}
