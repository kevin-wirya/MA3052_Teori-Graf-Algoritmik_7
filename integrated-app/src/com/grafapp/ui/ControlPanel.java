package com.grafapp.ui;

import com.grafapp.algorithm.*;
import com.grafapp.model.*;
import com.grafapp.util.GraphParser;
import com.grafapp.visualization.GraphCanvas;
import com.grafapp.visualization.SimulationController;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.io.File;
import java.util.*;

/**
 * Panel kontrol kanan: input graf, parameter algoritma, dan kontrol simulasi.
 * Desain non-scrollable, compact, informatif.
 */
public class ControlPanel extends VBox {

    private final GraphCanvas canvas;
    private final SimulationController simulation;

    private TextArea graphInputArea;
    private ComboBox<String> fileCombo;
    private VBox parameterBox;
    private Label algoNameLabel;
    private Label stepLabel;
    private Label messageLabel;
    private Slider speedSlider;
    private Button playBtn, pauseBtn;
    private CheckBox directedCb;
    private CheckBox weightedCb;

    // Info labels
    private Label nodeCountLabel;
    private Label edgeCountLabel;
    private Label startVertexLabel;
    private TextField startNodeField;

    private GraphAlgorithm currentAlgorithm;
    private final Map<String, Control> paramControls = new HashMap<>();
    private PauseTransition autoLoadDebounce;
    private int parsedStartVertex = -1;
    private ResultPanel resultPanel;

    // Right-panel result section
    private Label resultSummaryLabel;
    private FlowPane resultActionPane;

    public ControlPanel(GraphCanvas canvas, SimulationController simulation, ResultPanel resultPanel) {
        this.canvas = canvas;
        this.simulation = simulation;
        this.resultPanel = resultPanel;

        setPrefWidth(300);
        setMinWidth(280);
        setMaxWidth(320);
        setSpacing(0);
        setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 0 1;");

        // Non-scrollable: langsung VBox
        VBox content = new VBox(0,
            createGraphInputSection(),
            sep(),
            createGraphInfoSection(),
            sep(),
            createAlgorithmSection(),
            sep(),
            createSimulationSection(),
            sep(),
            createResultSection()
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().add(scroll);

        setupBindings();
    }

    public void setAlgorithm(GraphAlgorithm algorithm) {
        this.currentAlgorithm = algorithm;
        algoNameLabel.setText(algorithm.getName());

        parameterBox.getChildren().clear();
        paramControls.clear();

        for (ParameterInfo p : algorithm.getRequiredParameters()) {
            if (p.getKey().equals("startNode")) {
                // Gunakan startNodeField yang sudah ada di info section
                paramControls.put(p.getKey(), startNodeField);
            } else {
                Label lbl = new Label(p.getLabel() + ":");
                lbl.setFont(Font.font("Segoe UI", 11));

                Control ctrl;
                if (p.getType() == ParameterInfo.Type.BOOLEAN) {
                    CheckBox cb = new CheckBox();
                    if (p.getDefaultValue() instanceof Boolean)
                        cb.setSelected((Boolean) p.getDefaultValue());
                    ctrl = cb;
                } else {
                    TextField tf = new TextField();
                    tf.setPromptText("Masukkan nilai");
                    if (p.getDefaultValue() != null)
                        tf.setText(String.valueOf(p.getDefaultValue()));
                    ctrl = tf;
                }
                paramControls.put(p.getKey(), ctrl);
                parameterBox.getChildren().addAll(lbl, ctrl);
            }
        }
    }

    private VBox createGraphInputSection() {
        VBox sec = section();

        Label title = sectionTitle("INPUT");

        // File loader
        fileCombo = new ComboBox<>();
        fileCombo.setPromptText("Pilih file");
        fileCombo.setMaxWidth(Double.MAX_VALUE);
        fileCombo.setStyle("-fx-font-size: 11;");
        populateFileList();
        fileCombo.setOnAction(e -> loadFromFile());

        graphInputArea = new TextArea();
        graphInputArea.setPromptText("Masukkan input");
        graphInputArea.setPrefRowCount(5);
        graphInputArea.setFont(Font.font("Consolas", 11));
        graphInputArea.setStyle("-fx-control-inner-background: #FAFAFA;");

        // Auto-generate graf saat mengetik
        autoLoadDebounce = new PauseTransition(Duration.millis(400));
        autoLoadDebounce.setOnFinished(e -> autoLoadGraph());
        graphInputArea.textProperty().addListener((obs, o, n) -> autoLoadDebounce.playFromStart());

        directedCb = new CheckBox("Directed");
        directedCb.setFont(Font.font("Segoe UI", 11));
        directedCb.setOnAction(e -> autoLoadGraph());

        weightedCb = new CheckBox("Weighted");
        weightedCb.setFont(Font.font("Segoe UI", 11));
        weightedCb.setOnAction(e -> autoLoadGraph());

        HBox row = new HBox(6);
        Button clearBtn = smallBtn("Clear", "#757575");

        clearBtn.setOnAction(e -> {
            fileCombo.getSelectionModel().clearSelection();
            graphInputArea.clear();
            canvas.setGraph(new Graph());
            canvas.draw();
            updateGraphInfo(new Graph(), -1);
        });
        row.getChildren().addAll(clearBtn, directedCb, weightedCb);
        row.setAlignment(Pos.CENTER_LEFT);

        sec.getChildren().addAll(title, fileCombo, graphInputArea, row);
        return sec;
    }

    private VBox createGraphInfoSection() {
        VBox sec = section();

        Label title = sectionTitle("INFORMATION");

        nodeCountLabel = infoLabel("Node: 0");
        edgeCountLabel = infoLabel("Edge: 0");
        startVertexLabel = infoLabel("Start Vertex: -");

        HBox counts = new HBox(16, nodeCountLabel, edgeCountLabel);

        // Start node input
        Label startLbl = new Label("Starting Node:");
        startLbl.setFont(Font.font("Segoe UI", 11));
        startLbl.setTextFill(Color.web("#424242"));

        startNodeField = new TextField("0");
        startNodeField.setPrefWidth(70);
        startNodeField.setFont(Font.font("Consolas", 12));
        startNodeField.setStyle("-fx-background-color: #FFF8E1; -fx-border-color: #FFC107; "
            + "-fx-border-radius: 3; -fx-background-radius: 3;");

        HBox startRow = new HBox(8, startLbl, startNodeField);
        startRow.setAlignment(Pos.CENTER_LEFT);

        sec.getChildren().addAll(title, counts, startVertexLabel, startRow);
        return sec;
    }

    private VBox createAlgorithmSection() {
        VBox sec = section();
        Label title = sectionTitle("ALGORITHM");

        algoNameLabel = new Label("Pilih dari sidebar \u2190");
        algoNameLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        algoNameLabel.setTextFill(Color.web("#424242"));
        algoNameLabel.setWrapText(true);

        parameterBox = new VBox(4);

        Button runBtn = new Button("\u25B6  Run Algorithm");
        runBtn.setMaxWidth(Double.MAX_VALUE);
        runBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        runBtn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; "
            + "-fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 6 12;");
        runBtn.setOnMouseEntered(e -> runBtn.setOpacity(0.85));
        runBtn.setOnMouseExited(e -> runBtn.setOpacity(1.0));
        runBtn.setOnAction(e -> runAlgorithm());

        sec.getChildren().addAll(title, algoNameLabel, parameterBox, runBtn);
        return sec;
    }

    private VBox createSimulationSection() {
        VBox sec = section();
        Label title = sectionTitle("PLAYER");

        HBox ctrlRow = new HBox(4);
        ctrlRow.setAlignment(Pos.CENTER);

        Button backBtn = smallBtn("\u23EE", "#424242");
        playBtn = smallBtn("\u25B6 Play", "#43A047");
        pauseBtn = smallBtn("\u23F8 Pause", "#FF8F00");
        Button fwdBtn = smallBtn("\u23ED", "#424242");
        Button stopBtn = smallBtn("\u23F9", "#E53935");

        backBtn.setOnAction(e -> simulation.stepBackward());
        playBtn.setOnAction(e -> simulation.play());
        pauseBtn.setOnAction(e -> simulation.pause());
        fwdBtn.setOnAction(e -> simulation.stepForward());
        stopBtn.setOnAction(e -> simulation.stop());
        ctrlRow.getChildren().addAll(backBtn, playBtn, pauseBtn, fwdBtn, stopBtn);

        speedSlider = new Slider(0.25, 4.0, 1.0);
        speedSlider.setShowTickLabels(false);
        speedSlider.setShowTickMarks(false);
        speedSlider.setBlockIncrement(0.25);

        Label speedLbl = new Label("Speed: 1.00x");
        speedLbl.setFont(Font.font("Segoe UI", 10));
        speedLbl.setTextFill(Color.web("#9E9E9E"));
        speedSlider.valueProperty().addListener((o, ov, nv) -> {
            simulation.speedProperty().set(nv.doubleValue());
            speedLbl.setText(String.format("Speed: %.2fx", nv.doubleValue()));
        });

        stepLabel = new Label("Step: \u2014 / \u2014");
        stepLabel.setFont(Font.font("Segoe UI", 11));
        stepLabel.setTextFill(Color.web("#616161"));

        messageLabel = new Label("");
        messageLabel.setFont(Font.font("Segoe UI", 11));
        messageLabel.setTextFill(Color.web("#1565C0"));
        messageLabel.setWrapText(true);
        messageLabel.setMaxHeight(36);

        sec.getChildren().addAll(title, ctrlRow, speedSlider, speedLbl, stepLabel, messageLabel);
        return sec;
    }

    private VBox createResultSection() {
        VBox sec = section();
        Label title = sectionTitle("ALGORITHM RESULT");

        resultSummaryLabel = new Label("Jalankan algoritma untuk melihat hasil.");
        resultSummaryLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        resultSummaryLabel.setTextFill(Color.web("#212121"));
        resultSummaryLabel.setStyle("-fx-text-fill: #212121;");
        resultSummaryLabel.setWrapText(true);

        resultActionPane = new FlowPane(6, 6);
        resultActionPane.setAlignment(Pos.CENTER_LEFT);

        sec.getChildren().addAll(title, resultSummaryLabel, resultActionPane);
        return sec;
    }

    // === Bindings ===

    private void setupBindings() {
        simulation.currentStepProperty().addListener((o, ov, nv) -> {
            int curr = nv.intValue() + 1;
            int total = simulation.totalStepsProperty().get();
            stepLabel.setText("Step: " + curr + " / " + total);
        });
        simulation.currentMessageProperty().addListener((o, ov, nv) -> messageLabel.setText(nv));
        simulation.playingProperty().addListener((o, ov, nv) -> {
            playBtn.setDisable(nv);
            pauseBtn.setDisable(!nv);
        });
    }

    // Actions
    private void updateGraphInfo(Graph g, int startVtx) {
        nodeCountLabel.setText("Node: " + g.getNodeCount());
        edgeCountLabel.setText("Edge: " + g.getEdgeCount());
        if (startVtx >= 0) {
            startVertexLabel.setText("Start Vertex (dari file): " + startVtx);
            startNodeField.setText(String.valueOf(startVtx));
        } else {
            startVertexLabel.setText("Start Vertex: -");
        }
    }

    // Auto load graph
    private void autoLoadGraph() {
        String text = graphInputArea.getText().trim();
        if (text.isEmpty()) {
            canvas.setGraph(new Graph());
            canvas.draw();
            updateGraphInfo(new Graph(), -1);
            return;
        }
        GraphParser.ParseResult result = GraphParser.parseEdgeListWithStart(text, directedCb.isSelected(), weightedCb.isSelected());
        Graph g = result.getGraph();
        parsedStartVertex = result.getStartVertex();
        if (g.getNodeCount() > 0) {
            canvas.setGraph(g);
            canvas.startLayout();
        }
        updateGraphInfo(g, parsedStartVertex);
    }

    private void loadFromFile() {
        String selected = fileCombo.getValue();
        if (selected == null || selected.isEmpty()) return;
        try {
            File dataDir = resolveDataDir();
            String content = GraphParser.readFileText(
                new File(dataDir, selected).getAbsolutePath());
            graphInputArea.setText(content.replace("\r\n", "\n").replace("\r", "\n"));
        } catch (Exception ex) {
            warn("Gagal membaca file: " + ex.getMessage());
        }
    }

    private void populateFileList() {
        File dataDir = resolveDataDir();
        if (dataDir != null && dataDir.isDirectory()) {
            File[] files = dataDir.listFiles((d, name) -> name.endsWith(".txt"));
            if (files != null) {
                Arrays.sort(files);
                for (File f : files) {
                    fileCombo.getItems().add(f.getName());
                }
            }
        }
    }

    private File resolveDataDir() {
        File dir = new File("data");
        if (dir.isDirectory()) return dir;
        dir = new File("..", "data");
        if (dir.isDirectory()) return dir;
        return null;
    }

    private void runAlgorithm() {
        if (currentAlgorithm == null) { warn("Pilih algoritma dari sidebar."); return; }
        if (canvas.getGraph() == null || canvas.getGraph().getNodeCount() == 0) {
            warn("Masukkan graf terlebih dahulu."); return;
        }

        Map<String, Object> params = new HashMap<>();
        for (ParameterInfo p : currentAlgorithm.getRequiredParameters()) {
            Control ctrl = paramControls.get(p.getKey());
            if (ctrl instanceof TextField) {
                String txt = ((TextField) ctrl).getText().trim();
                if (txt.isEmpty() && p.isRequired()) {
                    warn("Parameter '" + p.getLabel() + "' wajib diisi."); return;
                }
                try {
                    params.put(p.getKey(), Integer.parseInt(txt));
                } catch (NumberFormatException ex) {
                    warn("Parameter '" + p.getLabel() + "' harus angka."); return;
                }
            } else if (ctrl instanceof CheckBox) {
                params.put(p.getKey(), ((CheckBox) ctrl).isSelected());
            }
        }

        canvas.getGraph().resetStates();
        AlgorithmResult result = currentAlgorithm.execute(canvas.getGraph(), params);
        simulation.loadResult(result);

        // Tampilkan hasil di bottom result panel
        resultPanel.setSummary(result.getSummary());
        resultPanel.clearActions();

        // Tampilkan hasil di right-panel result section
        resultSummaryLabel.setText(result.getSummary());
        resultActionPane.getChildren().clear();

        Map<String, Object> resData = result.getData();

        // Tampilkan traversal order jika ada
        @SuppressWarnings("unchecked")
        List<Integer> order = (List<Integer>) resData.get("traversalOrder");
        if (order != null && !order.isEmpty()) {
            StringBuilder sb = new StringBuilder("Traversal: ");
            for (int i = 0; i < order.size(); i++) {
                if (i > 0) sb.append(" \u2192 ");
                sb.append(order.get(i));
            }
            resultPanel.setDetail(sb.toString());
        } else {
            resultPanel.setDetail("");
        }

        // Bipartite actions
        if (resData.containsKey("bipartite")) {
            resultPanel.setBipartiteActions(resData);
            addBipartiteButtons(resData);
        }

        // Cycle actions
        if (resData.containsKey("hasCycle")) {
            resultPanel.setCycleActions(resData);
            addCycleButtons(resData);
        }

        simulation.play();
    }

    // Right panel results
    @SuppressWarnings("unchecked")
    private void addBipartiteButtons(Map<String, Object> data) {
        boolean bipartite = (Boolean) data.getOrDefault("bipartite", false);
        if (!bipartite) return;

        List<Integer> setA = (List<Integer>) data.get("setA");
        List<Integer> setB = (List<Integer>) data.get("setB");

        if (setA != null && !setA.isEmpty()) {
            Button btnA = smallBtn("Highlight A " + setA, "#1565C0");
            btnA.setOnAction(e -> highlightNodes(setA, NodeState.COMPONENT_1));
            resultActionPane.getChildren().add(btnA);
        }
        if (setB != null && !setB.isEmpty()) {
            Button btnB = smallBtn("Highlight B " + setB, "#E65100");
            btnB.setOnAction(e -> highlightNodes(setB, NodeState.COMPONENT_2));
            resultActionPane.getChildren().add(btnB);
        }
    }

    @SuppressWarnings("unchecked")
    private void addCycleButtons(Map<String, Object> data) {
        boolean hasCycle = (Boolean) data.getOrDefault("hasCycle", false);
        if (!hasCycle) return;

        List<List<Integer>> allCycles = (List<List<Integer>>) data.get("allCycles");
        if (allCycles == null || allCycles.isEmpty()) return;

        String[] colors = {"#1565C0", "#E65100", "#2E7D32", "#AD1457", "#6A1B9A", "#F57F17", "#00695C"};
        for (int i = 0; i < allCycles.size(); i++) {
            List<Integer> cycle = allCycles.get(i);
            String color = colors[i % colors.length];
            Button btn = smallBtn("Cycle " + (i + 1), color);
            NodeState cs = NodeState.forComponent(i);
            btn.setOnAction(e -> highlightCycle(cycle, cs));
            resultActionPane.getChildren().add(btn);
        }
    }

    private void highlightNodes(List<Integer> nodes, NodeState state) {
        Graph g = canvas.getGraph();
        if (g == null) return;
        g.resetStates();
        for (int id : nodes) {
            GraphNode n = g.getNode(id);
            if (n != null) n.setState(state);
        }
        canvas.draw();
    }

    private void highlightCycle(List<Integer> cycle, NodeState state) {
        Graph g = canvas.getGraph();
        if (g == null) return;
        g.resetStates();
        for (int id : cycle) {
            GraphNode n = g.getNode(id);
            if (n != null) n.setState(state);
        }
        for (int i = 0; i < cycle.size(); i++) {
            int u = cycle.get(i);
            int v = cycle.get((i + 1) % cycle.size());
            GraphEdge e = g.getEdge(u, v);
            if (e != null) e.setState(EdgeState.PATH);
        }
        canvas.draw();
    }

    // UI helper builder

    private VBox section() {
        VBox v = new VBox(6);
        v.setPadding(new Insets(10, 12, 10, 12));
        return v;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        l.setTextFill(Color.web("#9E9E9E"));
        l.setStyle("-fx-letter-spacing: 1;");
        return l;
    }

    private Label infoLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        l.setTextFill(Color.web("#424242"));
        return l;
    }

    private Separator sep() { return new Separator(); }

    private Button smallBtn(String text, String color) {
        Button b = new Button(text);
        b.setFont(Font.font("Segoe UI", 10));
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
            + "-fx-background-radius: 3; -fx-cursor: hand; -fx-padding: 4 8;");
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        return b;
    }

    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Perhatian");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
