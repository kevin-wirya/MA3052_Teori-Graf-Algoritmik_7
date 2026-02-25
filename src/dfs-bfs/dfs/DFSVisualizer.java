import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class DFSVisualizer extends JFrame {
    private GraphPanel graphPanel;
    
    public DFSVisualizer(ArrayList<ArrayList<Integer>> adj, int startVertex, ArrayList<Integer> traversalResult) {
        setTitle("Visualisasi DFS - Depth First Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        graphPanel = new GraphPanel(adj, startVertex, traversalResult);
        add(graphPanel);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(60, 60, 60));
        controlPanel.setPreferredSize(new Dimension(1000, 100));
        
        JLabel infoLabel = new JLabel("Tekan SPACE untuk mulai visualisasi | Tekan R untuk reset");
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel traversalLabel = new JLabel("DFS Traversal Order: " + traversalResult.toString());
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
    private int nodeRadius = 25;
    private int[] nodeX;
    private int[] nodeY;
    private ArrayList<Integer> visitedNodes = new ArrayList<>();
    private ArrayList<Integer> visitedEdges = new ArrayList<>();
    private volatile boolean isAnimating = false;
    private volatile boolean stopAnimation = false;
    private volatile Thread animationThread = null;
    private int currentStep = 0;
    
    public GraphPanel(ArrayList<ArrayList<Integer>> adj, int startVertex, ArrayList<Integer> traversalResult) {
        this.adj = adj;
        this.startVertex = startVertex;
        this.traversalResult = traversalResult;
        
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
            for (int step = 0; step < traversalResult.size(); step++) {
                if (stopAnimation) {
                    isAnimating = false;
                    return;
                }
                currentStep = step;
                int currentNode = traversalResult.get(step);
                visitedNodes.add(currentNode);
                if (step > 0) {
                    int prevNode = -1;
                    for (int j = step - 1; j >= 0; j--) {
                        int candidate = traversalResult.get(j);
                        if (adj.get(candidate).contains(currentNode)) {
                            prevNode = candidate;
                            break;
                        }
                    }
                    if (prevNode != -1) {
                        int edgeKey = prevNode * 1000 + currentNode;
                        visitedEdges.add(edgeKey);
                    }
                }
                
                repaint();
                
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
        currentStep = 0;
        repaint();
    }
    
    private void calculateNodePositions() {
        int numNodes = adj.size();
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int centerX = panelWidth / 2;
        int centerY = (panelHeight - 100) / 2;
        int radius = Math.min(panelWidth, panelHeight) / 3;
        
        for (int i = 0; i < numNodes; i++) {
            double angle = 2 * Math.PI * i / numNodes - Math.PI / 2;
            nodeX[i] = centerX + (int)(radius * Math.cos(angle));
            nodeY[i] = centerY + (int)(radius * Math.sin(angle));
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
                g2d.setColor(new Color(0, 200, 100)); // Hijau untuk visited
            } else if (i == startVertex) {
                g2d.setColor(new Color(255, 150, 0)); // Orange untuk starting node
            } else {
                g2d.setColor(Color.WHITE); // Putih untuk unvisited
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
        
        // Starting node
        g2d.setColor(new Color(255, 150, 0));
        g2d.fillOval(legendX, legendY + 15, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Starting Node", legendX + 30, legendY + 30);
        
        // Visited node
        g2d.setColor(new Color(0, 200, 100));
        g2d.fillOval(legendX, legendY + 45, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Visited Node", legendX + 30, legendY + 60);
        
        // Status
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        if (isAnimating) {
            g2d.drawString("Status: Step " + currentStep + "/" + traversalResult.size(), legendX, legendY - 20);
        } else if (visitedNodes.isEmpty()) {
            g2d.drawString("Status: Ready", legendX, legendY - 20);
        } else {
            g2d.drawString("Status: Done", legendX, legendY - 20);
        }
    }
}
