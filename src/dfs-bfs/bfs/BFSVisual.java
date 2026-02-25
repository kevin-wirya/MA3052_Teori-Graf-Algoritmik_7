import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class BFSVisual extends JFrame {
    private GraphPanel graphPanel;
    
    public BFSVisual(ArrayList<ArrayList<Integer>> adj, int startVertex, ArrayList<Integer> traversalResult, ArrayList<ArrayList<Integer>> levels) {
        setTitle("Visualisasi BFS - Breadth First Search (Level by Level)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        graphPanel = new GraphPanel(adj, startVertex, traversalResult, levels);
        add(graphPanel);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(60, 60, 60));
        controlPanel.setPreferredSize(new Dimension(1000, 100));
        
        JLabel infoLabel = new JLabel("Tekan SPACE untuk mulai visualisasi | Tekan R untuk reset");
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel traversalLabel = new JLabel("BFS Traversal Order: " + traversalResult.toString());
        traversalLabel.setForeground(Color.YELLOW);
        traversalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        traversalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(infoLabel);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(traversalLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        
        add(controlPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
}

class GraphPanel extends JPanel {
    private ArrayList<ArrayList<Integer>> adj;
    private int startVertex;
    private ArrayList<Integer> traversalResult;
    private ArrayList<ArrayList<Integer>> levels;
    private int nodeRadius = 25;
    private int[] nodeX;
    private int[] nodeY;
    private ArrayList<Integer> visitedNodes = new ArrayList<>();
    private ArrayList<Integer> visitedEdges = new ArrayList<>();
    private volatile boolean isAnimating = false;
    private volatile boolean stopAnimation = false;
    private volatile Thread animationThread = null;
    private int currentLevel = 0;
    
    public GraphPanel(ArrayList<ArrayList<Integer>> adj, int startVertex, ArrayList<Integer> traversalResult, ArrayList<ArrayList<Integer>> levels) {
        this.adj = adj;
        this.startVertex = startVertex;
        this.traversalResult = traversalResult;
        this.levels = levels;
        
        setBackground(Color.WHITE);
        int numNodes = adj.size();
        nodeX = new int[numNodes];
        nodeY = new int[numNodes];
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
            for (int level = 0; level < levels.size(); level++) {
                if (stopAnimation) {
                    isAnimating = false;
                    return;
                }
                
                currentLevel = level;
                ArrayList<Integer> nodesInLevel = levels.get(level);
                // tandai semua nodes sebagai visited
                for (int node : nodesInLevel) visitedNodes.add(node);
                // tandai edges dari level sebelumnya ke level saat ini
                if (level > 0) {
                    ArrayList<Integer> prevLevel = levels.get(level - 1);
                    for (int currentNode : nodesInLevel) {
                        for (int prevNode : prevLevel) {
                            if (adj.get(prevNode).contains(currentNode)) {
                                int edgeKey = prevNode * 1000 + currentNode;
                                visitedEdges.add(edgeKey);
                                break; 
                            }
                        }
                    }
                }
                repaint();
                try {
                    Thread.sleep(3000); 
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
        currentLevel = 0;
        repaint();
    }
    
    private void calculateNodePositions() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int verticalSpacing = (panelHeight - 150) / (levels.size() + 1);
        int topMargin = 50;
        int horizontalPadding = 400; 
        
        for (int levelIdx = 0; levelIdx < levels.size(); levelIdx++) {
            ArrayList<Integer> nodesInLevel = levels.get(levelIdx);
            int levelY = topMargin + (levelIdx + 1) * verticalSpacing;
            int maxNodesWidth = panelWidth - horizontalPadding;
            int numNodesInLevel = nodesInLevel.size();
            int horizontalSpacing = maxNodesWidth / Math.max(numNodesInLevel, 1);
            int startX = (panelWidth - (horizontalSpacing * (numNodesInLevel - 1))) / 2;
            for (int i = 0; i < numNodesInLevel; i++) {
                int nodeId = nodesInLevel.get(i);
                nodeX[nodeId] = startX + (i * horizontalSpacing);
                nodeY[nodeId] = levelY;
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        calculateNodePositions();
        
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2));
        
        for (int u = 0; u < adj.size(); u++) {
            for (int v : adj.get(u)) {
                if (u < v) {
                    boolean isVisited = visitedEdges.contains(u * 1000 + v) || visitedEdges.contains(v * 1000 + u);
                    
                    if (isVisited) {
                        g2d.setColor(new Color(0, 200, 100));
                        g2d.setStroke(new BasicStroke(3));
                    } else {
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(2));
                    }
                    g2d.drawLine(nodeX[u], nodeY[u], nodeX[v], nodeY[v]);
                }
            }
        }
        
        for (int i = 0; i < adj.size(); i++) {
            if (visitedNodes.contains(i)) {
                g2d.setColor(new Color(0, 200, 100)); // hijau untuk visited
            } else if (i == startVertex) {
                g2d.setColor(new Color(255, 150, 0)); // orange untuk starting node
            } else {
                g2d.setColor(Color.WHITE);
            }
            
            g2d.fillOval(nodeX[i] - nodeRadius, nodeY[i] - nodeRadius, nodeRadius * 2, nodeRadius * 2);
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(nodeX[i] - nodeRadius, nodeY[i] - nodeRadius, nodeRadius * 2, nodeRadius * 2);
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            String nodeLabel = String.valueOf(i);
            FontMetrics fm = g2d.getFontMetrics();
            int x = nodeX[i] - fm.stringWidth(nodeLabel) / 2;
            int y = nodeY[i] + fm.getAscent() / 2 - 2;
            g2d.drawString(nodeLabel, x, y);
        }
        
        int legendX = 20;
        int legendY = getHeight() - 130;
        
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Informasi:", legendX, legendY);
        
        g2d.setColor(new Color(255, 150, 0));
        g2d.fillOval(legendX, legendY + 15, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Starting Node", legendX + 30, legendY + 30);
        
        g2d.setColor(new Color(0, 200, 100));
        g2d.fillOval(legendX, legendY + 45, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Visited Node", legendX + 30, legendY + 60);
        
        // Status
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        if (isAnimating) g2d.drawString("Status: Level " + currentLevel + "/" + levels.size(), legendX, legendY - 20);
        else if (visitedNodes.isEmpty()) g2d.drawString("Status: Ready", legendX, legendY - 20);
        else g2d.drawString("Status: Done", legendX, legendY - 20);
    }
}
