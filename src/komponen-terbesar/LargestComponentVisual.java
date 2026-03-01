import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class LargestComponentVisual extends JFrame {
    private GraphPanel graphPanel;
    
    public LargestComponentVisual(ArrayList<ArrayList<Integer>> adj, LargestComponent.ComponentInfo componentInfo) {
        setTitle("Visualisasi Komponen Terbesar - Size: " + componentInfo.largestSize);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        graphPanel = new GraphPanel(adj, componentInfo);
        add(graphPanel);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(60, 60, 60));
        controlPanel.setPreferredSize(new Dimension(1000, 120));
        
        JLabel infoLabel = new JLabel("Tekan SPACE untuk mulai visualisasi | Tekan R untuk reset");
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String resultText = "Ukuran Komponen Terbesar: " + componentInfo.largestSize;
        JLabel resultLabel = new JLabel(resultText);
        resultLabel.setForeground(new Color(0, 200, 255));
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String componentText = "Node-node dalam komponen: " + componentInfo.largestComponentNodes.toString();
        JLabel componentLabel = new JLabel(componentText);
        componentLabel.setForeground(Color.YELLOW);
        componentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        componentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String countText = "Total komponen dalam graf: " + componentInfo.allComponents.size();
        JLabel countLabel = new JLabel(countText);
        countLabel.setForeground(new Color(0, 255, 150));
        countLabel.setFont(new Font("Arial", Font.BOLD, 12));
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(resultLabel);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(componentLabel);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(countLabel);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(infoLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        
        add(controlPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
}

class GraphPanel extends JPanel {
    private ArrayList<ArrayList<Integer>> adj;
    private LargestComponent.ComponentInfo componentInfo;
    private int nodeRadius = 25;
    private int[] nodeX;
    private int[] nodeY;
    private int[] parent;
    private int[] depth;
    private ArrayList<Integer> visitedNodes = new ArrayList<>();
    private ArrayList<Integer> visitedEdges = new ArrayList<>();
    private volatile boolean isAnimating = false;
    private volatile boolean stopAnimation = false;
    private volatile Thread animationThread = null;
    private int currentStep = 0;
    
    public GraphPanel(ArrayList<ArrayList<Integer>> adj, LargestComponent.ComponentInfo componentInfo) {
        this.adj = adj;
        this.componentInfo = componentInfo;
        
        setBackground(Color.WHITE);
        int numNodes = adj.size();
        nodeX = new int[numNodes];
        nodeY = new int[numNodes];
        parent = new int[numNodes];
        depth = new int[numNodes];
        
        // inisialisasi parent array dan posisi node
        for (int i = 0; i < numNodes; i++) {
            parent[i] = -1;
            depth[i] = 0;
        }
        
        // hitung parent dan depth untuk setiap node dalam komponen terbesar
        ArrayList<Integer> traversal = componentInfo.largestComponentNodes;
        for (int step = 1; step < traversal.size(); step++) {
            int currentNode = traversal.get(step);
            for (int j = step - 1; j >= 0; j--) {
                int candidate = traversal.get(j);
                if (adj.get(candidate).contains(currentNode)) {
                    parent[currentNode] = candidate;
                    depth[currentNode] = depth[candidate] + 1;
                    break;
                }
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
        
        ArrayList<Integer> traversal = componentInfo.largestComponentNodes;
        animationThread = new Thread(() -> {
            for (int step = 0; step < traversal.size(); step++) {
                if (stopAnimation) {
                    isAnimating = false;
                    return;
                }
                currentStep = step;
                int currentNode = traversal.get(step);
                visitedNodes.add(currentNode);
                if (step > 0) {
                    int prevNode = -1;
                    for (int j = step - 1; j >= 0; j--) {
                        int candidate = traversal.get(j);
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
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int numNodes = adj.size();
        
        // posisi node dalam lingkaran
        for (int i = 0; i < numNodes; i++) {
            double angle = 2 * Math.PI * i / numNodes;
            nodeX[i] = (int) (width / 2 + 200 * Math.cos(angle));
            nodeY[i] = (int) (height / 2 + 200 * Math.sin(angle));
        }
        
        // edges
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        for (int u = 0; u < numNodes; u++) {
            for (int v : adj.get(u)) {
                if (u < v) {
                    g2d.drawLine(nodeX[u], nodeY[u], nodeX[v], nodeY[v]);
                }
            }
        }
        
        // visited edges
        g2d.setColor(new Color(0, 150, 255));
        for (Integer edgeKey : visitedEdges) {
            int u = edgeKey / 1000;
            int v = edgeKey % 1000;
            g2d.drawLine(nodeX[u], nodeY[u], nodeX[v], nodeY[v]);
        }
        
        // nodes
        for (int i = 0; i < numNodes; i++) {
            boolean isInLargestComponent = componentInfo.largestComponentNodes.contains(i);
            boolean isVisited = visitedNodes.contains(i);
            
            if (isVisited) {
                g2d.setColor(new Color(255, 100, 100)); // red untuk visited
            } else if (isInLargestComponent) {
                g2d.setColor(new Color(100, 200, 255)); // light blue untuk komponen terbesar tapi belum visited
            } else {
                g2d.setColor(new Color(220, 220, 220)); // gray untuk node lain
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
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Warna Merah: Node Sedang Dikunjungi", 20, 30);
        g2d.setColor(new Color(100, 200, 255));
        g2d.fillOval(20, 45, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Warna Biru: Node dalam Komponen Terbesar", 40, 55);
    }
}
