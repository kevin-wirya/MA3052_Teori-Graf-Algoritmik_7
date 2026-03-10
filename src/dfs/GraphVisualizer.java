package dfs;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Main Graph Visualizer Application
 * Interactive graph creator and visualizer with DFS integration
 */
public class GraphVisualizer extends JFrame {
    // UI Components
    private JSplitPane mainSplitPane;
    private JPanel controlPanel;
    private GraphCanvas graphCanvas;
    
    // Graph controls
    private JRadioButton undirectedRadio, directedRadio;
    private JRadioButton zeroIndexRadio, oneIndexRadio, customLabelsRadio;
    private JComboBox<GraphCanvas.LayoutMode> layoutComboBox;
    private JSpinner nodeCountSpinner;
    private JTextArea graphDataArea;
    private JButton addEdgeButton, clearGraphButton, runDFSButton, resetButton;
    private JButton exportButton, importButton, presetsButton;
    
    // Data
    private Graph currentGraph;
    private boolean isDirected = false;
    private IndexingMode indexingMode = IndexingMode.ZERO_INDEX;
    private Map<String, Integer> customLabels = new HashMap<>();
    private DFS dfs;
    
    // Enums
    public enum IndexingMode {
        ZERO_INDEX, ONE_INDEX, CUSTOM_LABELS
    }
    
    public GraphVisualizer() {
        initializeComponents();
        setupLayout();
        setupEventListeners();
        updateGraph();
        
        setTitle("Graph Visualizer - Interactive Graph Creator & DFS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // Main split pane
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(400);
        mainSplitPane.setResizeWeight(0.33);
        
        // Control panel
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Graph canvas
        graphCanvas = new GraphCanvas();
        
        // Radio buttons for graph type
        undirectedRadio = new JRadioButton("Undirected", true);
        directedRadio = new JRadioButton("Directed");
        ButtonGroup graphTypeGroup = new ButtonGroup();
        graphTypeGroup.add(undirectedRadio);
        graphTypeGroup.add(directedRadio);
        
        // Radio buttons for indexing
        zeroIndexRadio = new JRadioButton("0-index", true);
        oneIndexRadio = new JRadioButton("1-index");
        customLabelsRadio = new JRadioButton("Custom Labels");
        ButtonGroup indexingGroup = new ButtonGroup();
        indexingGroup.add(zeroIndexRadio);
        indexingGroup.add(oneIndexRadio);
        indexingGroup.add(customLabelsRadio);
        
        // Node count spinner
        nodeCountSpinner = new JSpinner(new SpinnerNumberModel(6, 1, 50, 1));
        
        // Layout selection combo box
        layoutComboBox = new JComboBox<>(GraphCanvas.LayoutMode.values());
        layoutComboBox.setSelectedItem(GraphCanvas.LayoutMode.CIRCULAR);
        
        // Graph data text area
        graphDataArea = new JTextArea(15, 20);
        graphDataArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        graphDataArea.setText(getDefaultGraphData());
        
        // Buttons
        addEdgeButton = new JButton("Add Edge");
        clearGraphButton = new JButton("Clear Graph");
        runDFSButton = new JButton("Run DFS");
        resetButton = new JButton("Reset");
        exportButton = new JButton("Export");
        importButton = new JButton("Import");
        presetsButton = new JButton("Presets");
        
        // Initialize graph
        currentGraph = new Graph(6);
        dfs = new DFS(currentGraph);
    }
    
    private void setupLayout() {
        // Graph type panel
        JPanel graphTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        graphTypePanel.setBorder(new TitledBorder("Graph Type"));
        graphTypePanel.add(undirectedRadio);
        graphTypePanel.add(directedRadio);
        
        // Indexing panel
        JPanel indexingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        indexingPanel.setBorder(new TitledBorder("Node Indexing"));
        indexingPanel.add(zeroIndexRadio);
        indexingPanel.add(oneIndexRadio);
        indexingPanel.add(customLabelsRadio);
        
        // Node count panel
        JPanel nodeCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nodeCountPanel.setBorder(new TitledBorder("Node Count"));
        nodeCountPanel.add(new JLabel("Nodes:"));
        nodeCountPanel.add(nodeCountSpinner);
        
        // Layout panel
        JPanel layoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        layoutPanel.setBorder(new TitledBorder("Layout"));
        layoutPanel.add(new JLabel("Layout:"));
        layoutPanel.add(layoutComboBox);
        
        // Graph data panel
        JPanel graphDataPanel = new JPanel(new BorderLayout());
        graphDataPanel.setBorder(new TitledBorder("Graph Data"));
        JScrollPane scrollPane = new JScrollPane(graphDataArea);
        graphDataPanel.add(scrollPane, BorderLayout.CENTER);
        
        JLabel instructionLabel = new JLabel("<html>Format: node1 node2<br/>One edge per line</html>");
        instructionLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        graphDataPanel.add(instructionLabel, BorderLayout.SOUTH);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        buttonsPanel.add(addEdgeButton);
        buttonsPanel.add(clearGraphButton);
        buttonsPanel.add(runDFSButton);
        buttonsPanel.add(resetButton);
        buttonsPanel.add(exportButton);
        buttonsPanel.add(importButton);
        buttonsPanel.add(presetsButton);
        buttonsPanel.add(new JLabel()); // Empty space
        
        // Add all to control panel
        controlPanel.add(graphTypePanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(indexingPanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(nodeCountPanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(layoutPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(graphDataPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(buttonsPanel);
        
        // Setup split pane
        mainSplitPane.setLeftComponent(new JScrollPane(controlPanel));
        mainSplitPane.setRightComponent(graphCanvas);
        
        add(mainSplitPane);
    }
    
    private void setupEventListeners() {
        // Graph type listeners
        undirectedRadio.addActionListener(e -> {
            isDirected = false;
            graphCanvas.setDirected(false);
            updateGraph();
        });
        
        directedRadio.addActionListener(e -> {
            isDirected = true;
            graphCanvas.setDirected(true);
            updateGraph();
        });
        
        // Layout listener
        layoutComboBox.addActionListener(e -> {
            GraphCanvas.LayoutMode selectedLayout = (GraphCanvas.LayoutMode) layoutComboBox.getSelectedItem();
            graphCanvas.setLayoutMode(selectedLayout);
        });
        
        // Indexing listeners
        zeroIndexRadio.addActionListener(e -> {
            indexingMode = IndexingMode.ZERO_INDEX;
            updateGraphDataDisplay();
        });
        
        oneIndexRadio.addActionListener(e -> {
            indexingMode = IndexingMode.ONE_INDEX;
            updateGraphDataDisplay();
        });
        
        customLabelsRadio.addActionListener(e -> {
            indexingMode = IndexingMode.CUSTOM_LABELS;
            updateGraphDataDisplay();
        });
        
        // Node count listener
        nodeCountSpinner.addChangeListener(e -> {
            int newCount = (Integer) nodeCountSpinner.getValue();
            resizeGraph(newCount);
        });
        
        // Button listeners
        addEdgeButton.addActionListener(this::showAddEdgeDialog);
        clearGraphButton.addActionListener(e -> clearGraph());
        runDFSButton.addActionListener(this::runDFS);
        resetButton.addActionListener(e -> resetVisualization());
        exportButton.addActionListener(this::exportGraph);
        importButton.addActionListener(this::importGraph);
        presetsButton.addActionListener(e -> showPresetsDialog());
        
        // Graph data area listener
        graphDataArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateGraphFromText(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateGraphFromText(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateGraphFromText(); }
        });
    }
    
    private String getDefaultGraphData() {
        StringBuilder sb = new StringBuilder();
        // Default graph similar to the image
        String[] edges = {
            "0 2", "0 4", "0 5", "1 4", "1 5", "2 3", "2 4", "4 5"
        };
        
        for (int i = 0; i < edges.length; i++) {
            sb.append(String.format("%d %s%n", i + 1, edges[i]));
        }
        
        return sb.toString();
    }
    
    private void updateGraph() {
        int nodeCount = (Integer) nodeCountSpinner.getValue();
        currentGraph = new Graph(nodeCount);
        dfs = new DFS(currentGraph);
        
        // Parse and add edges from text area
        updateGraphFromText();
        
        // Update canvas
        graphCanvas.setGraph(currentGraph);
        graphCanvas.setDirected(isDirected);
    }
    
    private void updateGraphFromText() {
        if (currentGraph == null) return;
        
        // Create new graph to avoid issues
        int nodeCount = (Integer) nodeCountSpinner.getValue();
        currentGraph = new Graph(nodeCount);
        
        String[] lines = graphDataArea.getText().split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // Skip line numbers (format: "1 0 2" -> extract "0 2")
            String[] parts = line.split("\\s+");
            if (parts.length >= 3) {
                // Format with line number
                try {
                    int node1 = parseNodeIndex(parts[1]);
                    int node2 = parseNodeIndex(parts[2]);
                    addEdgeToGraph(node1, node2);
                } catch (Exception e) {
                    // Ignore invalid lines
                }
            } else if (parts.length == 2) {
                // Direct format "0 2"
                try {
                    int node1 = parseNodeIndex(parts[0]);
                    int node2 = parseNodeIndex(parts[1]);
                    addEdgeToGraph(node1, node2);
                } catch (Exception e) {
                    // Ignore invalid lines
                }
            }
        }
        
        dfs = new DFS(currentGraph);
        graphCanvas.setGraph(currentGraph);
        graphCanvas.repaint();
    }
    
    private int parseNodeIndex(String nodeStr) {
        switch (indexingMode) {
            case ZERO_INDEX:
                return Integer.parseInt(nodeStr);
            case ONE_INDEX:
                return Integer.parseInt(nodeStr) - 1;
            case CUSTOM_LABELS:
                return customLabels.getOrDefault(nodeStr, Integer.parseInt(nodeStr));
            default:
                return Integer.parseInt(nodeStr);
        }
    }
    
    private void addEdgeToGraph(int node1, int node2) {
        if (node1 >= 0 && node1 < currentGraph.getVertices() && 
            node2 >= 0 && node2 < currentGraph.getVertices()) {
            if (isDirected) {
                currentGraph.addDirectedEdge(node1, node2);
            } else {
                currentGraph.addEdge(node1, node2);
            }
        }
    }
    
    private void resizeGraph(int newCount) {
        currentGraph = new Graph(newCount);
        dfs = new DFS(currentGraph);
        updateGraphFromText();
    }
    
    private void clearGraph() {
        graphDataArea.setText("");
        updateGraph();
    }
    
    private void updateGraphDataDisplay() {
        // Update the display based on indexing mode
        // For now, keep the current format
        updateGraphFromText();
    }
    
    private void showAddEdgeDialog(ActionEvent e) {
        JDialog dialog = new JDialog(this, "Add Edge", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JTextField node1Field = new JTextField(5);
        JTextField node2Field = new JTextField(5);
        JButton okButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Node 1:"), gbc);
        gbc.gridx = 1;
        dialog.add(node1Field, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Node 2:"), gbc);
        gbc.gridx = 1;
        dialog.add(node2Field, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(okButton, gbc);
        gbc.gridx = 1;
        dialog.add(cancelButton, gbc);
        
        okButton.addActionListener(ev -> {
            try {
                String node1Str = node1Field.getText().trim();
                String node2Str = node2Field.getText().trim();
                
                // Add to text area
                String newLine = String.format("%d %s %s%n", 
                    graphDataArea.getLineCount() + 1, node1Str, node2Str);
                graphDataArea.append(newLine);
                
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(ev -> dialog.dispose());
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void runDFS(ActionEvent e) {
        if (currentGraph.getVertices() == 0) {
            JOptionPane.showMessageDialog(this, "Please create a graph first!");
            return;
        }
        
        String startVertexStr = JOptionPane.showInputDialog(this, 
            "Enter start vertex (0-" + (currentGraph.getVertices()-1) + "):", "0");
        
        if (startVertexStr != null) {
            try {
                int startVertex = parseNodeIndex(startVertexStr);
                if (startVertex >= 0 && startVertex < currentGraph.getVertices()) {
                    // Run DFS and show result
                    List<Integer> result = dfs.dfsRecursive(startVertex);
                    
                    // Update canvas with DFS result
                    graphCanvas.setDFSResult(result);
                    graphCanvas.startDFSAnimation();
                    
                    // Show result in dialog
                    JOptionPane.showMessageDialog(this, 
                        "DFS Traversal Order: " + result, 
                        "DFS Result", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid vertex index!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        }
    }
    
    private void resetVisualization() {
        graphCanvas.reset();
        graphCanvas.repaint();
    }
    
    private void exportGraph(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Graph");
        fileChooser.setSelectedFile(new java.io.File("graph.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                    writer.println("# Graph Export");
                    writer.println("# Nodes: " + currentGraph.getVertices());
                    writer.println("# Directed: " + isDirected);
                    writer.println("# Indexing: " + indexingMode);
                    writer.println();
                    
                    // Export current graph data
                    writer.print(graphDataArea.getText());
                }
                JOptionPane.showMessageDialog(this, "Graph exported successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting graph: " + ex.getMessage());
            }
        }
    }
    
    private void importGraph(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Graph");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                StringBuilder content = new StringBuilder();
                
                try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
                    String line;
                    boolean skipComments = true;
                    
                    while ((line = reader.readLine()) != null) {
                        if (skipComments && line.trim().startsWith("#")) {
                            // Parse metadata from comments if needed
                            if (line.contains("Directed: true")) {
                                directedRadio.setSelected(true);
                                isDirected = true;
                            } else if (line.contains("Directed: false")) {
                                undirectedRadio.setSelected(true);
                                isDirected = false;
                            }
                            continue;
                        }
                        
                        if (line.trim().isEmpty() && skipComments) {
                            skipComments = false;
                            continue;
                        }
                        
                        if (!skipComments) {
                            content.append(line).append("\n");
                        }
                    }
                }
                
                graphDataArea.setText(content.toString().trim());
                updateGraph();
                
                JOptionPane.showMessageDialog(this, "Graph imported successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error importing graph: " + ex.getMessage());
            }
        }
    }
    
    private void showPresetsDialog() {
        GraphPresetDialog dialog = new GraphPresetDialog(this);
        dialog.setVisible(true);
    }
    
    public void loadGraphPreset(GraphPresetDialog.GraphPreset preset) {
        // Update UI controls
        nodeCountSpinner.setValue(preset.nodeCount);
        
        if (preset.directed) {
            directedRadio.setSelected(true);
            isDirected = true;
        } else {
            undirectedRadio.setSelected(true);
            isDirected = false;
        }
        
        // Set graph data
        graphDataArea.setText(preset.edges);
        
        // Update graph
        updateGraph();
        
        // Show info
        JOptionPane.showMessageDialog(this, 
            "Loaded preset: " + preset.name + "\n" + preset.description,
            "Preset Loaded",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new GraphVisualizer().setVisible(true);
        });
    }
}