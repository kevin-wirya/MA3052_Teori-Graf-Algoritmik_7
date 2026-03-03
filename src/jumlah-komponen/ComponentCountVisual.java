import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class ComponentCountVisual extends JFrame {
    private GraphPanel graphPanel;
    
    public ComponentCountVisual(ArrayList<ArrayList<Integer>> adj, ComponentCount.ComponentInfo componentInfo) {
        setTitle("Visualisasi Jumlah Komponen - Total: " + componentInfo.componentCount + " komponen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        graphPanel = new GraphPanel(adj, componentInfo);
        add(graphPanel);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(60, 60, 60));
        controlPanel.setPreferredSize(new Dimension(1000, 140));
        
        JLabel infoLabel = new JLabel("Tekan SPACE untuk mulai visualisasi | Tekan R untuk reset");
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String resultText = "Jumlah Total Komponen: " + componentInfo.componentCount;
        JLabel resultLabel = new JLabel(resultText);
        resultLabel.setForeground(new Color(0, 200, 255));
        resultLabel.setFont(new Font("Arial", Font.BOLD, 20));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Build component details string
        StringBuilder componentDetails = new StringBuilder("Detail: ");
        for (int i = 0; i < componentInfo.allComponents.size(); i++) {
            componentDetails.append("K").append(i + 1).append("(").append(componentInfo.allComponents.get(i).size()).append(" node)");
            if (i < componentInfo.allComponents.size() - 1) {
                componentDetails.append(", ");
            }
        }
        JLabel componentLabel = new JLabel(componentDetails.toString());
        componentLabel.setForeground(Color.YELLOW);
        componentLabel.setFont(new Font("Arial", Font.BOLD, 12));
        componentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel legendLabel = new JLabel("Setiap warna menunjukkan komponen yang berbeda");
        legendLabel.setForeground(new Color(200, 200, 200));
        legendLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        legendLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(resultLabel);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(componentLabel);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(legendLabel);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(infoLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        
        add(controlPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
}

class GraphPanel extends JPanel {
    private ArrayList<ArrayList<Integer>> adj;
    private ComponentCount.ComponentInfo componentInfo;
    private int nodeRadius = 25;
    private int[] nodeX;
    private int[] nodeY;
    private int[] nodeComponent; // komponen untuk setiap node
    private ArrayList<Integer> visitedNodes = new ArrayList<>();
    private ArrayList<Integer> visitedEdges = new ArrayList<>();
    private volatile boolean isAnimating = false;
    private volatile boolean stopAnimation = false;
    private volatile Thread animationThread = null;
    private int currentComponentIndex = 0;
    
    // Warna berbeda untuk setiap komponen
    private Color[] componentColors = {
        new Color(255, 100, 100),   // Merah
        new Color(100, 200, 100),   // Hijau
        new Color(100, 100, 255),   // Biru
        new Color(255, 200, 50),    // Kuning
        new Color(200, 100, 255),   // Ungu
        new Color(100, 255, 255),   // Cyan
        new Color(255, 150, 100),   // Orange
        new Color(255, 100, 200),   // Pink
        new Color(150, 255, 150),   // Light Green
        new Color(200, 200, 100)    // Olive
    };
    
    public GraphPanel(ArrayList<ArrayList<Integer>> adj, ComponentCount.ComponentInfo componentInfo) {
        this.adj = adj;
        this.componentInfo = componentInfo;
        
        setBackground(Color.WHITE);
        int numNodes = adj.size();
        nodeX = new int[numNodes];
        nodeY = new int[numNodes];
        nodeComponent = new int[numNodes];
        
        // Assign komponen ke setiap node
        for (int i = 0; i < componentInfo.allComponents.size(); i++) {
            for (int node : componentInfo.allComponents.get(i)) {
                nodeComponent[node] = i;
            }
        }
        
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
            // Animasi per komponen
            for (int compIdx = 0; compIdx < componentInfo.allComponents.size(); compIdx++) {
                if (stopAnimation) {
                    isAnimating = false;
                    return;
                }
                
                currentComponentIndex = compIdx;
                ArrayList<Integer> component = componentInfo.allComponents.get(compIdx);
                
                // Animasi setiap node dalam komponen
                for (int nodeIdx = 0; nodeIdx < component.size(); nodeIdx++) {
                    if (stopAnimation) {
                        isAnimating = false;
                        return;
                    }
                    
                    int currentNode = component.get(nodeIdx);
                    visitedNodes.add(currentNode);
                    
                    // Tambahkan edge yang terhubung
                    if (nodeIdx > 0) {
                        for (int j = nodeIdx - 1; j >= 0; j--) {
                            int prevNode = component.get(j);
                            if (adj.get(prevNode).contains(currentNode)) {
                                int edgeKey = Math.min(prevNode, currentNode) * 1000 + Math.max(prevNode, currentNode);
                                if (!visitedEdges.contains(edgeKey)) {
                                    visitedEdges.add(edgeKey);
                                }
                            }
                        }
                    }
                    
                    repaint();
                    
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        isAnimating = false;
                        return;
                    }
                }
                
                // Pause sebentar sebelum pindah ke komponen berikutnya
                try {
                    Thread.sleep(800);
                } catch (InterruptedException ex) {
                    isAnimating = false;
                    return;
                }
            }
            isAnimating = false;
        });
        animationThread.start();
    }
    
    private void resetAnimation() {
        stopAnimation = true;
        isAnimating = false;
        if (animationThread != null) {
            animationThread.interrupt();
        }
        visitedNodes.clear();
        visitedEdges.clear();
        currentComponentIndex = 0;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int numNodes = adj.size();
        
        // Posisi node dalam lingkaran
        for (int i = 0; i < numNodes; i++) {
            double angle = 2 * Math.PI * i / numNodes;
            nodeX[i] = (int) (width / 2 + 200 * Math.cos(angle));
            nodeY[i] = (int) (height / 2 + 200 * Math.sin(angle));
        }
        
        // Draw all edges (gray)
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        for (int u = 0; u < numNodes; u++) {
            for (int v : adj.get(u)) {
                if (u < v) {
                    g2d.drawLine(nodeX[u], nodeY[u], nodeX[v], nodeY[v]);
                }
            }
        }
        
        // Draw visited edges dengan warna komponen
        for (Integer edgeKey : visitedEdges) {
            int u = edgeKey / 1000;
            int v = edgeKey % 1000;
            int compIdx = nodeComponent[u];
            g2d.setColor(componentColors[compIdx % componentColors.length]);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(nodeX[u], nodeY[u], nodeX[v], nodeY[v]);
        }
        
        // Draw nodes
        for (int i = 0; i < numNodes; i++) {
            boolean isVisited = visitedNodes.contains(i);
            int compIdx = nodeComponent[i];
            
            if (isVisited) {
                // Node yang sudah dikunjungi - warna komponen
                g2d.setColor(componentColors[compIdx % componentColors.length]);
            } else {
                // Node yang belum dikunjungi - abu-abu
                g2d.setColor(new Color(220, 220, 220));
            }
            
            g2d.fillOval(nodeX[i] - nodeRadius, nodeY[i] - nodeRadius, nodeRadius * 2, nodeRadius * 2);
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(nodeX[i] - nodeRadius, nodeY[i] - nodeRadius, nodeRadius * 2, nodeRadius * 2);
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2d.getFontMetrics();
            String label = String.valueOf(i);
            int x = nodeX[i] - fm.stringWidth(label) / 2;
            int y = nodeY[i] + fm.getAscent() / 2;
            g2d.drawString(label, x, y);
        }
        
        // Legend
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Legend Komponen:", 20, 25);
        
        int legendY = 45;
        for (int i = 0; i < componentInfo.allComponents.size(); i++) {
            g2d.setColor(componentColors[i % componentColors.length]);
            g2d.fillOval(20, legendY + (i * 22) - 10, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Komponen " + (i + 1) + " (" + componentInfo.allComponents.get(i).size() + " node)", 42, legendY + (i * 22));
        }
    }
}
