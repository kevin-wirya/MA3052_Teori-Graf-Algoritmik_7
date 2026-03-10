package dfs;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Kelas untuk visualisasi algoritma DFS menggunakan Java Swing
 */
public class DFSVisualizer extends JPanel {
    private Graph graph;
    private DFS dfs;
    private List<Point> nodePositions;
    private List<Integer> currentPath;
    private Set<Integer> visitedNodes;
    private int currentStep;
    private List<List<Integer>> allSteps;
    private Timer animationTimer;
    
    // Konstanta untuk tampilan
    private static final int NODE_RADIUS = 25;
    private static final Color NODE_COLOR = Color.LIGHT_GRAY;
    private static final Color VISITED_COLOR = Color.GREEN;
    private static final Color CURRENT_COLOR = Color.RED;
    private static final Color EDGE_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.BLACK;
    
    public DFSVisualizer(Graph graph) {
        this.graph = graph;
        this.dfs = new DFS(graph);
        this.nodePositions = new ArrayList<>();
        this.currentPath = new ArrayList<>();
        this.visitedNodes = new HashSet<>();
        this.currentStep = 0;
        this.allSteps = new ArrayList<>();
        
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        
        // Generate posisi node secara otomatis
        generateNodePositions();
    }
    
    /**
     * Generate posisi node dalam pattern circular
     */
    private void generateNodePositions() {
        int centerX = 400;
        int centerY = 300;
        int radius = 200;
        int numVertices = graph.getVertices();
        
        nodePositions.clear();
        
        if (numVertices == 1) {
            nodePositions.add(new Point(centerX, centerY));
        } else {
            for (int i = 0; i < numVertices; i++) {
                double angle = 2 * Math.PI * i / numVertices - Math.PI / 2; // Mulai dari atas
                int x = (int) (centerX + radius * Math.cos(angle));
                int y = (int) (centerY + radius * Math.sin(angle));
                nodePositions.add(new Point(x, y));
            }
        }
    }
    
    /**
     * Mulai animasi DFS
     */
    public void startDFSAnimation(int startVertex, boolean isRecursive) {
        // Reset state
        visitedNodes.clear();
        currentPath.clear();
        currentStep = 0;
        
        // Jalankan DFS dan dapatkan langkah-langkahnya
        if (isRecursive) {
            dfs.dfsRecursive(startVertex);
        } else {
            dfs.dfsIterative(startVertex);
        }
        
        allSteps = dfs.getStepByStep();
        
        // Mulai animasi
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentStep < allSteps.size()) {
                    currentPath = new ArrayList<>(allSteps.get(currentStep));
                    visitedNodes = new HashSet<>(currentPath);
                    currentStep++;
                    repaint();
                } else {
                    animationTimer.stop();
                }
            }
        });
        
        animationTimer.start();
    }
    
    /**
     * Reset visualisasi
     */
    public void reset() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        visitedNodes.clear();
        currentPath.clear();
        currentStep = 0;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Gambar edges
        drawEdges(g2d);
        
        // Gambar nodes
        drawNodes(g2d);
        
        // Gambar informasi
        drawInfo(g2d);
    }
    
    /**
     * Gambar edges/sisi
     */
    private void drawEdges(Graphics2D g2d) {
        g2d.setColor(EDGE_COLOR);
        g2d.setStroke(new BasicStroke(2));
        
        for (int i = 0; i < graph.getVertices(); i++) {
            Point p1 = nodePositions.get(i);
            for (int neighbor : graph.getNeighbors(i)) {
                if (neighbor > i) { // Hindari menggambar edge yang sama dua kali
                    Point p2 = nodePositions.get(neighbor);
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
    }
    
    /**
     * Gambar nodes/vertex
     */
    private void drawNodes(Graphics2D g2d) {
        for (int i = 0; i < graph.getVertices(); i++) {
            Point pos = nodePositions.get(i);
            
            // Tentukan warna node
            Color nodeColor = NODE_COLOR;
            if (visitedNodes.contains(i)) {
                nodeColor = VISITED_COLOR;
            }
            if (!currentPath.isEmpty() && currentPath.get(currentPath.size() - 1) == i) {
                nodeColor = CURRENT_COLOR;
            }
            
            // Gambar node
            g2d.setColor(nodeColor);
            g2d.fillOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
            
            // Gambar border
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
            
            // Gambar label vertex
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String label = String.valueOf(i);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getAscent();
            g2d.drawString(label, pos.x - labelWidth / 2, pos.y + labelHeight / 2);
        }
    }
    
    /**
     * Gambar informasi status
     */
    private void drawInfo(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Informasi langkah saat ini
        String stepInfo = "Langkah: " + currentStep + " / " + allSteps.size();
        g2d.drawString(stepInfo, 20, 30);
        
        // Urutan kunjungan
        String pathInfo = "Urutan kunjungan: " + currentPath;
        g2d.drawString(pathInfo, 20, 50);
        
        // Legend
        g2d.drawString("Legend:", 20, 80);
        
        // Node belum dikunjungi
        g2d.setColor(NODE_COLOR);
        g2d.fillOval(20, 90, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(20, 90, 20, 20);
        g2d.drawString("Belum dikunjungi", 50, 105);
        
        // Node dikunjungi
        g2d.setColor(VISITED_COLOR);
        g2d.fillOval(20, 120, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(20, 120, 20, 20);
        g2d.drawString("Sudah dikunjungi", 50, 135);
        
        // Node saat ini
        g2d.setColor(CURRENT_COLOR);
        g2d.fillOval(20, 150, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(20, 150, 20, 20);
        g2d.drawString("Sedang dikunjungi", 50, 165);
    }
    
    /**
     * Membuat window untuk visualisasi
     */
    public static void createVisualizationWindow(Graph graph) {
        JFrame frame = new JFrame("Visualisasi Algoritma DFS");
        DFSVisualizer visualizer = new DFSVisualizer(graph);
        
        // Panel untuk kontrol
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        
        // Input untuk start vertex
        JLabel startLabel = new JLabel("Start Vertex:");
        JTextField startField = new JTextField("0", 5);
        
        // Button untuk DFS rekursif
        JButton recursiveButton = new JButton("DFS Rekursif");
        recursiveButton.addActionListener(e -> {
            try {
                int startVertex = Integer.parseInt(startField.getText());
                if (startVertex >= 0 && startVertex < graph.getVertices()) {
                    visualizer.startDFSAnimation(startVertex, true);
                } else {
                    JOptionPane.showMessageDialog(frame, "Vertex tidak valid! (0-" + (graph.getVertices()-1) + ")");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Masukkan angka yang valid!");
            }
        });
        
        // Button untuk DFS iteratif
        JButton iterativeButton = new JButton("DFS Iteratif");
        iterativeButton.addActionListener(e -> {
            try {
                int startVertex = Integer.parseInt(startField.getText());
                if (startVertex >= 0 && startVertex < graph.getVertices()) {
                    visualizer.startDFSAnimation(startVertex, false);
                } else {
                    JOptionPane.showMessageDialog(frame, "Vertex tidak valid! (0-" + (graph.getVertices()-1) + ")");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Masukkan angka yang valid!");
            }
        });
        
        // Button untuk reset
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> visualizer.reset());
        
        // Tambahkan komponen ke control panel
        controlPanel.add(startLabel);
        controlPanel.add(startField);
        controlPanel.add(recursiveButton);
        controlPanel.add(iterativeButton);
        controlPanel.add(resetButton);
        
        // Setup frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(visualizer, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}