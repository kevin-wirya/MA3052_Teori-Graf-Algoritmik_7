package dfs;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Canvas for rendering graph visualization
 * Handles dynamic layout, node positioning, and DFS animation
 */
public class GraphCanvas extends JPanel {
    // Visual constants
    private static final int NODE_RADIUS = 25;
    private static final Color NODE_COLOR = new Color(240, 240, 240);
    private static final Color VISITED_COLOR = new Color(144, 238, 144);
    private static final Color CURRENT_COLOR = new Color(255, 99, 99);
    private static final Color EDGE_COLOR = new Color(100, 100, 100);
    private static final Color DIRECTED_ARROW_COLOR = new Color(80, 80, 80);
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Font NODE_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 10);
    
    // Graph data
    private Graph graph;
    private List<Point> nodePositions;
    private Map<Integer, String> nodeLabels;
    
    // Animation data
    private List<Integer> dfsResult;
    private Set<Integer> visitedNodes;
    private int currentVisitingNode;
    private Timer animationTimer;
    private int animationStep;
    
    // Layout settings
    private boolean isDirected = false;
    private LayoutMode layoutMode = LayoutMode.CIRCULAR;
    
    // Mouse interaction
    private int draggedNode = -1;
    private Point lastMousePos;
    
    public enum LayoutMode {
        CIRCULAR, FORCE_DIRECTED, GRID, HIERARCHICAL
    }
    
    public GraphCanvas() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 500));
        
        nodePositions = new ArrayList<>();
        nodeLabels = new HashMap<>();
        visitedNodes = new HashSet<>();
        currentVisitingNode = -1;
        
        setupMouseListeners();
    }
    
    private void setupMouseListeners() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                draggedNode = findNodeAtPosition(e.getPoint());
                lastMousePos = e.getPoint();
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedNode >= 0 && draggedNode < nodePositions.size()) {
                    Point delta = new Point(
                        e.getX() - lastMousePos.x,
                        e.getY() - lastMousePos.y
                    );
                    
                    Point nodePos = nodePositions.get(draggedNode);
                    nodePos.x += delta.x;
                    nodePos.y += delta.y;
                    
                    // Keep within bounds
                    nodePos.x = Math.max(NODE_RADIUS, Math.min(getWidth() - NODE_RADIUS, nodePos.x));
                    nodePos.y = Math.max(NODE_RADIUS, Math.min(getHeight() - NODE_RADIUS, nodePos.y));
                    
                    lastMousePos = e.getPoint();
                    repaint();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                draggedNode = -1;
            }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }
    
    private int findNodeAtPosition(Point p) {
        for (int i = 0; i < nodePositions.size(); i++) {
            Point nodePos = nodePositions.get(i);
            double distance = Math.sqrt(
                Math.pow(p.x - nodePos.x, 2) + Math.pow(p.y - nodePos.y, 2)
            );
            if (distance <= NODE_RADIUS) {
                return i;
            }
        }
        return -1;
    }
    
    public void setGraph(Graph graph) {
        this.graph = graph;
        generateNodePositions();
        generateNodeLabels();
        repaint();
    }
    
    public void setDirected(boolean directed) {
        this.isDirected = directed;
        repaint();
    }
    
    public void setLayoutMode(LayoutMode mode) {
        this.layoutMode = mode;
        generateNodePositions();
        repaint();
    }
    
    private void generateNodePositions() {
        nodePositions.clear();
        
        if (graph == null || graph.getVertices() == 0) {
            return;
        }
        
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int numVertices = graph.getVertices();
        
        switch (layoutMode) {
            case CIRCULAR:
                generateCircularLayout(centerX, centerY, numVertices);
                break;
            case GRID:
                generateGridLayout(width, height, numVertices);
                break;
            case FORCE_DIRECTED:
                generateForceDirectedLayout(centerX, centerY, numVertices);
                break;
            case HIERARCHICAL:
                generateHierarchicalLayout(width, height, numVertices);
                break;
        }
    }
    
    private void generateCircularLayout(int centerX, int centerY, int numVertices) {
        if (numVertices == 1) {
            nodePositions.add(new Point(centerX, centerY));
            return;
        }
        
        int radius = Math.min(centerX, centerY) - NODE_RADIUS - 50;
        
        for (int i = 0; i < numVertices; i++) {
            double angle = 2 * Math.PI * i / numVertices - Math.PI / 2;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            nodePositions.add(new Point(x, y));
        }
    }
    
    private void generateGridLayout(int width, int height, int numVertices) {
        int cols = (int) Math.ceil(Math.sqrt(numVertices));
        int rows = (int) Math.ceil((double) numVertices / cols);
        
        int marginX = 80;
        int marginY = 80;
        int availableWidth = width - 2 * marginX;
        int availableHeight = height - 2 * marginY;
        
        int cellWidth = availableWidth / cols;
        int cellHeight = availableHeight / rows;
        
        for (int i = 0; i < numVertices; i++) {
            int row = i / cols;
            int col = i % cols;
            
            int x = marginX + col * cellWidth + cellWidth / 2;
            int y = marginY + row * cellHeight + cellHeight / 2;
            
            nodePositions.add(new Point(x, y));
        }
    }
    
    private void generateForceDirectedLayout(int centerX, int centerY, int numVertices) {
        // Simple spring-embedded layout
        Random rand = new Random();
        
        // Initial random positions
        for (int i = 0; i < numVertices; i++) {
            int x = centerX + (rand.nextInt(200) - 100);
            int y = centerY + (rand.nextInt(200) - 100);
            nodePositions.add(new Point(x, y));
        }
        
        // Simple force-directed iterations
        for (int iteration = 0; iteration < 50; iteration++) {
            for (int i = 0; i < numVertices; i++) {
                Point pos = nodePositions.get(i);
                double fx = 0, fy = 0;
                
                // Repulsive forces from other nodes
                for (int j = 0; j < numVertices; j++) {
                    if (i != j) {
                        Point otherPos = nodePositions.get(j);
                        double dx = pos.x - otherPos.x;
                        double dy = pos.y - otherPos.y;
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        
                        if (distance > 0) {
                            double force = 1000 / (distance * distance);
                            fx += force * dx / distance;
                            fy += force * dy / distance;
                        }
                    }
                }
                
                // Attractive forces from connected nodes
                if (graph != null) {
                    for (int neighbor : graph.getNeighbors(i)) {
                        Point neighborPos = nodePositions.get(neighbor);
                        double dx = neighborPos.x - pos.x;
                        double dy = neighborPos.y - pos.y;
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        
                        if (distance > 0) {
                            double force = distance / 100.0;
                            fx += force * dx / distance;
                            fy += force * dy / distance;
                        }
                    }
                }
                
                // Apply forces with damping
                pos.x += (int) (fx * 0.1);
                pos.y += (int) (fy * 0.1);
                
                // Keep within bounds
                pos.x = Math.max(NODE_RADIUS + 10, Math.min(getWidth() - NODE_RADIUS - 10, pos.x));
                pos.y = Math.max(NODE_RADIUS + 10, Math.min(getHeight() - NODE_RADIUS - 10, pos.y));
            }
        }
    }
    
    private void generateHierarchicalLayout(int width, int height, int numVertices) {
        // Simple top-down hierarchical layout
        Map<Integer, Integer> levels = new HashMap<>();
        Map<Integer, Boolean> visited = new HashMap<>();
        
        // BFS to assign levels
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(0);
        levels.put(0, 0);
        visited.put(0, true);
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            int currentLevel = levels.get(current);
            
            if (graph != null) {
                for (int neighbor : graph.getNeighbors(current)) {
                    if (!visited.getOrDefault(neighbor, false)) {
                        levels.put(neighbor, currentLevel + 1);
                        visited.put(neighbor, true);
                        queue.offer(neighbor);
                    }
                }
            }
        }
        
        // Position nodes by level
        int maxLevel = levels.values().stream().mapToInt(i -> i).max().orElse(0);
        Map<Integer, Integer> levelCounts = new HashMap<>();
        
        for (int level : levels.values()) {
            levelCounts.put(level, levelCounts.getOrDefault(level, 0) + 1);
        }
        
        Map<Integer, Integer> levelPositions = new HashMap<>();
        
        for (int i = 0; i < numVertices; i++) {
            int level = levels.getOrDefault(i, maxLevel);
            int positionInLevel = levelPositions.getOrDefault(level, 0);
            levelPositions.put(level, positionInLevel + 1);
            
            int y = 60 + level * (height - 120) / Math.max(1, maxLevel);
            int x = (width * (positionInLevel + 1)) / (levelCounts.get(level) + 1);
            
            nodePositions.add(new Point(x, y));
        }
    }
    
    private void generateNodeLabels() {
        nodeLabels.clear();
        
        if (graph == null) return;
        
        for (int i = 0; i < graph.getVertices(); i++) {
            nodeLabels.put(i, String.valueOf(i));
        }
    }
    
    public void setDFSResult(List<Integer> result) {
        this.dfsResult = new ArrayList<>(result);
        this.visitedNodes.clear();
        this.currentVisitingNode = -1;
        this.animationStep = 0;
    }
    
    public void startDFSAnimation() {
        if (dfsResult == null || dfsResult.isEmpty()) return;
        
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        animationStep = 0;
        visitedNodes.clear();
        currentVisitingNode = -1;
        
        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (animationStep < dfsResult.size()) {
                    currentVisitingNode = dfsResult.get(animationStep);
                    visitedNodes.add(currentVisitingNode);
                    animationStep++;
                    repaint();
                } else {
                    animationTimer.stop();
                    currentVisitingNode = -1;
                }
            }
        });
        
        animationTimer.start();
    }
    
    public void reset() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        visitedNodes.clear();
        currentVisitingNode = -1;
        dfsResult = null;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (graph == null) {
            drawEmptyState(g);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Regenerate positions if needed
        if (nodePositions.size() != graph.getVertices()) {
            generateNodePositions();
        }
        
        // Draw edges first
        drawEdges(g2d);
        
        // Draw nodes on top
        drawNodes(g2d);
        
        // Draw legend and info
        drawInfo(g2d);
    }
    
    private void drawEmptyState(Graphics g) {
        g.setColor(Color.GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();
        
        String message = "Create a graph to see visualization";
        int x = (getWidth() - fm.stringWidth(message)) / 2;
        int y = getHeight() / 2;
        
        g.drawString(message, x, y);
    }
    
    private void drawEdges(Graphics2D g2d) {
        if (graph == null || nodePositions.size() != graph.getVertices()) return;
        
        g2d.setColor(EDGE_COLOR);
        g2d.setStroke(new BasicStroke(2));
        
        for (int i = 0; i < graph.getVertices(); i++) {
            Point p1 = nodePositions.get(i);
            
            for (int neighbor : graph.getNeighbors(i)) {
                if (!isDirected && neighbor > i) continue; // Avoid duplicate undirected edges
                
                Point p2 = nodePositions.get(neighbor);
                
                if (isDirected) {
                    drawArrow(g2d, p1, p2);
                } else {
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
    }
    
    private void drawArrow(Graphics2D g2d, Point from, Point to) {
        // Calculate direction vector
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        if (length == 0) return;
        
        // Normalize
        dx /= length;
        dy /= length;
        
        // Calculate start and end points (accounting for node radius)
        int startX = (int) (from.x + dx * NODE_RADIUS);
        int startY = (int) (from.y + dy * NODE_RADIUS);
        int endX = (int) (to.x - dx * NODE_RADIUS);
        int endY = (int) (to.y - dy * NODE_RADIUS);
        
        // Draw line
        g2d.drawLine(startX, startY, endX, endY);
        
        // Draw arrowhead
        double arrowLength = 15;
        double arrowAngle = Math.PI / 6;
        
        double angle1 = Math.atan2(dy, dx) + Math.PI - arrowAngle;
        double angle2 = Math.atan2(dy, dx) + Math.PI + arrowAngle;
        
        int x1 = (int) (endX + arrowLength * Math.cos(angle1));
        int y1 = (int) (endY + arrowLength * Math.sin(angle1));
        int x2 = (int) (endX + arrowLength * Math.cos(angle2));
        int y2 = (int) (endY + arrowLength * Math.sin(angle2));
        
        g2d.drawLine(endX, endY, x1, y1);
        g2d.drawLine(endX, endY, x2, y2);
    }
    
    private void drawNodes(Graphics2D g2d) {
        if (nodePositions.size() != graph.getVertices()) return;
        
        for (int i = 0; i < graph.getVertices(); i++) {
            Point pos = nodePositions.get(i);
            
            // Determine node color
            Color nodeColor = NODE_COLOR;
            if (visitedNodes.contains(i)) {
                nodeColor = VISITED_COLOR;
            }
            if (currentVisitingNode == i) {
                nodeColor = CURRENT_COLOR;
            }
            
            // Draw node
            g2d.setColor(nodeColor);
            g2d.fillOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS, 
                        2 * NODE_RADIUS, 2 * NODE_RADIUS);
            
            // Draw border
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS, 
                        2 * NODE_RADIUS, 2 * NODE_RADIUS);
            
            // Draw label
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(NODE_FONT);
            String label = nodeLabels.getOrDefault(i, String.valueOf(i));
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getAscent();
            
            g2d.drawString(label, 
                          pos.x - labelWidth / 2, 
                          pos.y + labelHeight / 2);
        }
    }
    
    private void drawInfo(Graphics2D g2d) {
        // Draw legend
        g2d.setFont(LABEL_FONT);
        g2d.setColor(Color.BLACK);
        
        int legendX = 10;
        int legendY = getHeight() - 80;
        
        g2d.drawString("Legend:", legendX, legendY);
        
        // Unvisited node
        g2d.setColor(NODE_COLOR);
        g2d.fillOval(legendX, legendY + 10, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(legendX, legendY + 10, 15, 15);
        g2d.drawString("Unvisited", legendX + 25, legendY + 21);
        
        // Visited node
        g2d.setColor(VISITED_COLOR);
        g2d.fillOval(legendX, legendY + 35, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(legendX, legendY + 35, 15, 15);
        g2d.drawString("Visited", legendX + 25, legendY + 46);
        
        // Current node
        g2d.setColor(CURRENT_COLOR);
        g2d.fillOval(legendX, legendY + 60, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(legendX, legendY + 60, 15, 15);
        g2d.drawString("Current", legendX + 25, legendY + 71);
        
        // Graph info
        if (graph != null) {
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString("Nodes: " + graph.getVertices(), getWidth() - 100, 20);
            
            // Count edges
            int edgeCount = 0;
            for (int i = 0; i < graph.getVertices(); i++) {
                edgeCount += graph.getNeighbors(i).size();
            }
            if (!isDirected) edgeCount /= 2;
            
            g2d.drawString("Edges: " + edgeCount, getWidth() - 100, 40);
        }
    }
}