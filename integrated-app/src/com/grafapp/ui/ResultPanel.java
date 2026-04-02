package com.grafapp.ui;

import com.grafapp.model.*;
import com.grafapp.visualization.GraphCanvas;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.*;

/**
 * Panel hasil di bawah canvas (tengah bawah layar).
 */
public class ResultPanel extends HBox {

    private final Label titleLabel;
    private final Label summaryLabel;
    private final Label detailLabel;
    private final FlowPane actionPane;
    private GraphCanvas canvas;

    public ResultPanel() {
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(8, 16, 8, 16));
        setSpacing(16);
        setMinHeight(100);
        setPrefHeight(120);
        setMaxHeight(160);
        setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

        titleLabel = new Label("RESULT");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web("#212121"));
        titleLabel.setStyle("-fx-text-fill: #212121;");
        titleLabel.setMinWidth(45);

        VBox titleBox = new VBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setMinWidth(45);

        // Separator visual
        Region sep = new Region();
        sep.setPrefWidth(2);
        sep.setMinWidth(2);
        sep.setMaxWidth(2);
        sep.setMinHeight(50);
        sep.setStyle("-fx-background-color: #E0E0E0;");

        // Scrollable content
        VBox textContent = new VBox(6);
        textContent.setStyle("-fx-background-color: #FAFAFA;");

        summaryLabel = new Label("Jalankan algoritma untuk melihat hasil.");
        summaryLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        summaryLabel.setTextFill(Color.web("#212121"));
        summaryLabel.setStyle("-fx-text-fill: #212121;");
        summaryLabel.setWrapText(true);

        detailLabel = new Label("");
        detailLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        detailLabel.setTextFill(Color.web("#1565C0"));
        detailLabel.setStyle("-fx-text-fill: #1565C0;");
        detailLabel.setWrapText(true);

        actionPane = new FlowPane(6, 6);
        actionPane.setAlignment(Pos.CENTER_LEFT);

        textContent.getChildren().addAll(summaryLabel, detailLabel, actionPane);

        ScrollPane scroll = new ScrollPane(textContent);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: #FAFAFA; -fx-background: #FAFAFA; -fx-padding: 0;");
        HBox.setHgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(titleBox, sep, scroll);
    }

    public void setCanvas(GraphCanvas canvas) {
        this.canvas = canvas;
    }

    public void setSummary(String text) {
        summaryLabel.setText(text);
    }

    public void setDetail(String text) {
        detailLabel.setText(text);
    }

    public void clearActions() {
        actionPane.getChildren().clear();
    }

    /** Add bipartite highlight buttons */
    @SuppressWarnings("unchecked")
    public void setBipartiteActions(Map<String, Object> data) {
        actionPane.getChildren().clear();
        boolean bipartite = (Boolean) data.getOrDefault("bipartite", false);
        if (!bipartite || canvas == null) return;

        List<Integer> setA = (List<Integer>) data.get("setA");
        List<Integer> setB = (List<Integer>) data.get("setB");

        if (setA != null && !setA.isEmpty()) {
            Button btnA = actionButton("Highlight Himpunan A " + setA, "#1565C0");
            btnA.setOnAction(e -> highlightNodes(setA, NodeState.COMPONENT_1));
            actionPane.getChildren().add(btnA);
        }
        if (setB != null && !setB.isEmpty()) {
            Button btnB = actionButton("Highlight Himpunan B " + setB, "#E65100");
            btnB.setOnAction(e -> highlightNodes(setB, NodeState.COMPONENT_2));
            actionPane.getChildren().add(btnB);
        }
    }

    // Highlight cycle
    @SuppressWarnings("unchecked")
    public void setCycleActions(Map<String, Object> data) {
        actionPane.getChildren().clear();
        boolean hasCycle = (Boolean) data.getOrDefault("hasCycle", false);
        if (!hasCycle || canvas == null) return;

        List<List<Integer>> allCycles = (List<List<Integer>>) data.get("allCycles");
        if (allCycles == null || allCycles.isEmpty()) return;

        String[] colors = {"#1565C0", "#E65100", "#2E7D32", "#AD1457", "#6A1B9A", "#F57F17", "#00695C"};

        for (int i = 0; i < allCycles.size(); i++) {
            List<Integer> cycle = allCycles.get(i);
            String color = colors[i % colors.length];
            Button btn = actionButton("Cycle " + (i + 1) + ": " + formatCycle(cycle), color);
            NodeState cs = NodeState.forComponent(i);
            final int idx = i;
            btn.setOnAction(e -> highlightCycle(cycle, cs, idx));
            actionPane.getChildren().add(btn);
        }
    }

    @SuppressWarnings("unchecked")
    public void setMstActions(Map<String, Object> data) {
        actionPane.getChildren().clear();
        if (canvas == null) return;

        List<List<Integer>> mstEdges = (List<List<Integer>>) data.get("mstEdges");
        if (mstEdges == null || mstEdges.isEmpty()) return;

        Button btn = actionButton("Highlight MST", "#1976D2");
        btn.setOnAction(e -> highlightTreeEdges(mstEdges));
        actionPane.getChildren().add(btn);
    }

    private void highlightNodes(List<Integer> nodes, NodeState state) {
        if (canvas == null) return;
        Graph g = canvas.getGraph();
        if (g == null) return;
        g.resetStates();
        for (int id : nodes) {
            GraphNode n = g.getNode(id);
            if (n != null) n.setState(state);
        }
        canvas.draw();
    }

    private void highlightCycle(List<Integer> cycle, NodeState state, int colorIdx) {
        if (canvas == null) return;
        Graph g = canvas.getGraph();
        if (g == null) return;
        g.resetStates();
        for (int id : cycle) {
            GraphNode n = g.getNode(id);
            if (n != null) n.setState(state);
        }
        // Highlight edges dari cycle
        for (int i = 0; i < cycle.size(); i++) {
            int u = cycle.get(i);
            int v = cycle.get((i + 1) % cycle.size());
            GraphEdge e = g.getEdge(u, v);
            if (e != null) e.setState(EdgeState.PATH);
        }
        canvas.draw();
    }

    private void highlightTreeEdges(List<List<Integer>> mstEdges) {
        if (canvas == null) return;
        Graph g = canvas.getGraph();
        if (g == null) return;
        g.resetStates();
        for (List<Integer> edge : mstEdges) {
            if (edge == null || edge.size() < 2) continue;
            GraphEdge e = g.getEdge(edge.get(0), edge.get(1));
            if (e != null) e.setState(EdgeState.TREE_EDGE);
        }
        canvas.draw();
    }

    private Button actionButton(String text, String color) {
        Button b = new Button(text);
        b.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
            + "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 5 10;");
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        return b;
    }

    private String formatCycle(List<Integer> cycle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cycle.size(); i++) {
            if (i > 0) sb.append("\u2192");
            sb.append(cycle.get(i));
        }
        sb.append("\u2192").append(cycle.get(0));
        return sb.toString();
    }

    public void clear() {
        summaryLabel.setText("Jalankan algoritma untuk melihat hasil.");
        detailLabel.setText("");
        actionPane.getChildren().clear();
    }
}
