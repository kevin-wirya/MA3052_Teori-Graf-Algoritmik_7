package dfs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for selecting and loading preset graph examples
 */
public class GraphPresetDialog extends JDialog {
    private GraphVisualizer parent;
    private JList<GraphPreset> presetList;
    
    public static class GraphPreset {
        public final String name;
        public final String description;
        public final int nodeCount;
        public final String edges;
        public final boolean directed;
        
        public GraphPreset(String name, String description, int nodeCount, String edges, boolean directed) {
            this.name = name;
            this.description = description;
            this.nodeCount = nodeCount;
            this.edges = edges;
            this.directed = directed;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    private static final GraphPreset[] PRESETS = {
        new GraphPreset(
            "Simple Path",
            "A simple linear path: 0-1-2-3",
            4,
            "1 0 1\n2 1 2\n3 2 3",
            false
        ),
        
        new GraphPreset(
            "Complete K4",
            "Complete graph with 4 nodes",
            4,
            "1 0 1\n2 0 2\n3 0 3\n4 1 2\n5 1 3\n6 2 3",
            false
        ),
        
        new GraphPreset(
            "Binary Tree",
            "Perfect binary tree structure",
            7,
            "1 0 1\n2 0 2\n3 1 3\n4 1 4\n5 2 5\n6 2 6",
            false
        ),
        
        new GraphPreset(
            "Cycle Graph",
            "Circular graph: 0-1-2-3-0",
            4,
            "1 0 1\n2 1 2\n3 2 3\n4 3 0",
            false
        ),
        
        new GraphPreset(
            "Star Graph",
            "Star configuration with center node",
            5,
            "1 0 1\n2 0 2\n3 0 3\n4 0 4",
            false
        ),
        
        new GraphPreset(
            "Directed DAG",
            "Directed Acyclic Graph example",
            5,
            "1 0 1\n2 0 2\n3 1 3\n4 2 3\n5 3 4",
            true
        ),
        
        new GraphPreset(
            "Complex Network",
            "More complex interconnected network",
            6,
            "1 0 1\n2 0 2\n3 1 3\n4 1 4\n5 2 5\n6 3 4\n7 3 5\n8 4 5",
            false
        ),
        
        new GraphPreset(
            "Disconnected Graph",
            "Two separate components",
            6,
            "1 0 1\n2 1 2\n3 3 4\n4 4 5",
            false
        )
    };
    
    public GraphPresetDialog(GraphVisualizer parent) {
        super(parent, "Graph Presets", true);
        this.parent = parent;
        
        initComponents();
        setupLayout();
        setupListeners();
        
        setSize(400, 500);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        presetList = new JList<>(PRESETS);
        presetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        presetList.setCellRenderer(new PresetCellRenderer());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Select a Graph Preset", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // List with scroll
        JScrollPane scrollPane = new JScrollPane(presetList);
        add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loadButton = new JButton("Load");
        JButton cancelButton = new JButton("Cancel");
        
        loadButton.addActionListener(this::loadPreset);
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(loadButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Padding
        ((JComponent) getContentPane()).setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
    }
    
    private void setupListeners() {
        presetList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    loadPreset(null);
                }
            }
        });
    }
    
    private void loadPreset(ActionEvent e) {
        GraphPreset selected = presetList.getSelectedValue();
        if (selected != null) {
            parent.loadGraphPreset(selected);
            dispose();
        }
    }
    
    private static class PresetCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof GraphPreset) {
                GraphPreset preset = (GraphPreset) value;
                setText(String.format("<html><b>%s</b><br/><small>%s</small></html>", 
                       preset.name, preset.description));
            }
            
            return this;
        }
    }
}