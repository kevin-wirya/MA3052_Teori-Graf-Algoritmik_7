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
            createGraphTypeSection(),
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

    private VBox createGraphTypeSection() {
        VBox sec = section();

        Label title = sectionTitle("GRAPH TYPES");
        Label hint = new Label("Pilih tipe graf untuk mengisi input otomatis.");
        hint.setFont(Font.font("Segoe UI", 10));
        hint.setTextFill(Color.web("#757575"));
        hint.setWrapText(true);

        FlowPane buttonPane = new FlowPane(6, 6);
        buttonPane.setPrefWrapLength(260);

        addGraphTypeButton(buttonPane, "Graf Lengkap Kn", () -> {
            Integer n = promptInt("Graf Lengkap Kn", "Masukkan n", 6, 1, 20);
            if (n == null) return;
            applyGeneratedGraphText(generateCompleteGraphKn(n));
        });

        addGraphTypeButton(buttonPane, "Graf Bipartit Lengkap Km,n", () -> {
            Map<String, Integer> values = promptIntGroup(
                "Graf Bipartit Lengkap Km,n",
                new String[]{"m", "n"},
                new int[]{3, 4},
                new int[]{1, 1},
                new int[]{20, 20}
            );
            if (values == null) return;
            int m = values.get("m");
            int n = values.get("n");
            applyGeneratedGraphText(generateCompleteBipartiteKmn(m, n));
        });

        addGraphTypeButton(buttonPane, "Pohon Tn", () -> {
            Integer n = promptInt("Pohon Tn", "Masukkan n", 10, 2, 80);
            if (n == null) return;
            applyGeneratedGraphText(generateTree(n));
        });

        addGraphTypeButton(buttonPane, "Siklus Cn", () -> {
            Integer n = promptInt("Siklus Cn", "Masukkan n", 8, 3, 80);
            if (n == null) return;
            applyGeneratedGraphText(generateCycleCn(n));
        });

        addGraphTypeButton(buttonPane, "Lintasan Pn", () -> {
            Integer n = promptInt("Lintasan Pn", "Masukkan n", 8, 2, 80);
            if (n == null) return;
            applyGeneratedGraphText(generatePathPn(n));
        });

        addGraphTypeButton(buttonPane, "Graf Roda Wn", () -> {
            Integer n = promptInt("Graf Roda Wn", "Masukkan n", 8, 4, 40);
            if (n == null) return;
            applyGeneratedGraphText(generateWheelWn(n));
        });

        addGraphTypeButton(buttonPane, "Graf Prisma", () -> {
            Integer n = promptInt("Graf Prisma", "Masukkan n", 6, 3, 30);
            if (n == null) return;
            applyGeneratedGraphText(generatePrismGraph(n));
        });

        addGraphTypeButton(buttonPane, "Petersen Graph", () ->
            applyGeneratedGraphText(generateGeneralizedPetersen(5, 2)));

        addGraphTypeButton(buttonPane, "Generalized Petersen P(n,k)", () -> {
            Map<String, Integer> values = promptIntGroup(
                "Generalized Petersen P(n,k)",
                new String[]{"n", "k"},
                new int[]{8, 2},
                new int[]{5, 1},
                new int[]{30, 14}
            );
            if (values == null) return;

            int n = values.get("n");
            int k = values.get("k");
            int maxK = (n - 1) / 2;
            if (k > maxK) {
                warn("Untuk n = " + n + ", nilai k harus 1-" + maxK + ".");
                return;
            }

            applyGeneratedGraphText(generateGeneralizedPetersen(n, k));
        });

        addGraphTypeButton(buttonPane, "Circulant Cn(a1,a2)", () -> {
            Map<String, Integer> values = promptIntGroup(
                "Circulant Graph Cn(a1,a2)",
                new String[]{"n", "a1", "a2"},
                new int[]{10, 1, 2},
                new int[]{5, 1, 1},
                new int[]{50, 25, 25}
            );
            if (values == null) return;

            int n = values.get("n");
            int a1 = values.get("a1");
            int a2 = values.get("a2");
            int maxA = Math.max(1, n / 2);

            if (a1 >= a2) {
                warn("a1 harus lebih kecil dari a2.");
                return;
            }
            if (a1 > maxA || a2 > maxA) {
                warn("Untuk n = " + n + ", nilai a1 dan a2 harus <= " + maxA + ".");
                return;
            }

            applyGeneratedGraphText(generateCirculantGraph(n, a1, a2));
        });

        addGraphTypeButton(buttonPane, "Hypercubes H(n)", () -> {
            Integer n = promptInt("Hypercube H(n)", "Masukkan dimensi n", 4, 1, 7);
            if (n == null) return;
            applyGeneratedGraphText(generateHypercube(n));
        });

        addGraphTypeButton(buttonPane, "Grid Graph G(m,n)", () -> {
            Map<String, Integer> values = promptIntGroup(
                "Grid Graph G(m,n)",
                new String[]{"m", "n"},
                new int[]{4, 4},
                new int[]{2, 2},
                new int[]{20, 20}
            );
            if (values == null) return;
            int m = values.get("m");
            int n = values.get("n");
            applyGeneratedGraphText(generateGridGraph(m, n));
        });

        sec.getChildren().addAll(title, hint, buttonPane);
        return sec;
    }

    private void addGraphTypeButton(FlowPane parent, String text, Runnable action) {
        Button btn = new Button(text);
        btn.setWrapText(true);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 10));
        btn.setPrefWidth(124);
        btn.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #0D47A1; "
            + "-fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 5 8;");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        btn.setOnAction(e -> action.run());
        parent.getChildren().add(btn);
    }

    private Integer promptInt(String title, String prompt, int defaultValue, int min, int max) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(defaultValue));
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt + " (" + min + "-" + max + "):");

        Optional<String> res = dialog.showAndWait();
        if (res.isEmpty()) return null;

        try {
            int val = Integer.parseInt(res.get().trim());
            if (val < min || val > max) {
                warn("Nilai harus pada rentang " + min + "-" + max + ".");
                return null;
            }
            return val;
        } catch (NumberFormatException ex) {
            warn("Input harus berupa bilangan bulat.");
            return null;
        }
    }

    private Map<String, Integer> promptIntGroup(
        String title,
        String[] names,
        int[] defaults,
        int[] mins,
        int[] maxs
    ) {
        if (names.length != defaults.length || names.length != mins.length || names.length != maxs.length) {
            throw new IllegalArgumentException("Konfigurasi input group tidak konsisten.");
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Masukkan parameter:");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(6, 0, 0, 0));

        List<TextField> fields = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Label lbl = new Label(names[i] + " (" + mins[i] + "-" + maxs[i] + "): ");
            TextField tf = new TextField(String.valueOf(defaults[i]));
            tf.setPrefColumnCount(8);
            fields.add(tf);
            grid.add(lbl, 0, i);
            grid.add(tf, 1, i);
        }

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return null;

        Map<String, Integer> values = new LinkedHashMap<>();
        for (int i = 0; i < names.length; i++) {
            String raw = fields.get(i).getText().trim();
            try {
                int val = Integer.parseInt(raw);
                if (val < mins[i] || val > maxs[i]) {
                    warn("Nilai '" + names[i] + "' harus pada rentang " + mins[i] + "-" + maxs[i] + ".");
                    return null;
                }
                values.put(names[i], val);
            } catch (NumberFormatException ex) {
                warn("Nilai '" + names[i] + "' harus berupa bilangan bulat.");
                return null;
            }
        }
        return values;
    }

    private void applyGeneratedGraphText(String graphText) {
        fileCombo.getSelectionModel().clearSelection();
        directedCb.setSelected(false);
        weightedCb.setSelected(false);
        graphInputArea.setText(graphText);
        autoLoadGraph();
    }

    private String generateCompleteGraphKn(int n) {
        List<int[]> edges = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                edges.add(new int[]{u, v});
            }
        }
        return toEdgeListText(n, edges);
    }

    private String generateCompleteBipartiteKmn(int m, int n) {
        List<int[]> edges = new ArrayList<>();
        int offset = m;
        for (int u = 0; u < m; u++) {
            for (int v = 0; v < n; v++) {
                edges.add(new int[]{u, offset + v});
            }
        }
        return toEdgeListText(m + n, edges);
    }

    private String generateTree(int n) {
        List<int[]> edges = new ArrayList<>();
        for (int child = 1; child < n; child++) {
            int parent = (child - 1) / 2;
            edges.add(new int[]{parent, child});
        }
        return toEdgeListText(n, edges);
    }

    private String generateCycleCn(int n) {
        List<int[]> edges = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            int v = (u + 1) % n;
            edges.add(new int[]{u, v});
        }
        return toEdgeListText(n, edges);
    }

    private String generatePathPn(int n) {
        List<int[]> edges = new ArrayList<>();
        for (int u = 0; u < n - 1; u++) {
            edges.add(new int[]{u, u + 1});
        }
        return toEdgeListText(n, edges);
    }

    private String generateWheelWn(int n) {
        List<int[]> edges = new ArrayList<>();
        int rim = n - 1;
        int center = n - 1;

        for (int u = 0; u < rim; u++) {
            int v = (u + 1) % rim;
            edges.add(new int[]{u, v});
            edges.add(new int[]{center, u});
        }
        return toEdgeListText(n, edges);
    }

    private String generatePrismGraph(int n) {
        List<int[]> edges = new ArrayList<>();
        int offset = n;
        int totalNodes = 2 * n;

        for (int u = 0; u < n; u++) {
            int next = (u + 1) % n;
            edges.add(new int[]{u, next});
            edges.add(new int[]{offset + u, offset + next});
            edges.add(new int[]{u, offset + u});
        }
        return toEdgeListText(totalNodes, edges);
    }

    private String generateGeneralizedPetersen(int n, int k) {
        List<int[]> edges = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int offset = n;

        for (int i = 0; i < n; i++) {
            addUndirectedEdge(edges, seen, i, (i + 1) % n);
            addUndirectedEdge(edges, seen, i, offset + i);
            addUndirectedEdge(edges, seen, offset + i, offset + ((i + k) % n));
        }
        return toEdgeListText(2 * n, edges);
    }

    private String generateCirculantGraph(int n, int a1, int a2) {
        List<int[]> edges = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int[] jumps = {a1, a2};

        for (int u = 0; u < n; u++) {
            for (int jump : jumps) {
                int v = (u + jump) % n;
                addUndirectedEdge(edges, seen, u, v);
            }
        }
        return toEdgeListText(n, edges);
    }

    private String generateHypercube(int dimension) {
        List<int[]> edges = new ArrayList<>();
        int n = 1 << dimension;

        for (int u = 0; u < n; u++) {
            for (int bit = 0; bit < dimension; bit++) {
                int v = u ^ (1 << bit);
                if (u < v) {
                    edges.add(new int[]{u, v});
                }
            }
        }
        return toEdgeListText(n, edges);
    }

    private String generateGridGraph(int rows, int cols) {
        List<int[]> edges = new ArrayList<>();
        int n = rows * cols;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int u = r * cols + c;
                if (c + 1 < cols) {
                    edges.add(new int[]{u, u + 1});
                }
                if (r + 1 < rows) {
                    edges.add(new int[]{u, u + cols});
                }
            }
        }
        return toEdgeListText(n, edges);
    }

    private void addUndirectedEdge(List<int[]> edges, Set<String> seen, int u, int v) {
        int a = Math.min(u, v);
        int b = Math.max(u, v);
        String key = a + ":" + b;
        if (seen.add(key)) {
            edges.add(new int[]{a, b});
        }
    }

    private String toEdgeListText(int nodeCount, List<int[]> edges) {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeCount).append(" ").append(edges.size());
        for (int[] e : edges) {
            sb.append("\n").append(e[0]).append(" ").append(e[1]);
        }
        return sb.toString();
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
        resultPanel.setDetail(buildResultDetailText(resData));

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

        // MST actions
        if (resData.containsKey("mstEdges")) {
            resultPanel.setMstActions(resData);
            addMstButtons(resData);
        }

        simulation.play();
    }

    @SuppressWarnings("unchecked")
    private String buildResultDetailText(Map<String, Object> resData) {
        List<Integer> order = (List<Integer>) resData.get("traversalOrder");
        if (order != null && !order.isEmpty()) {
            return "Traversal: " + formatNodePath(order);
        }

        List<Integer> path = (List<Integer>) resData.get("shortestPath");
        if ((path == null || path.isEmpty()) && resData.get("path") instanceof List) {
            path = (List<Integer>) resData.get("path");
        }

        if (path != null && !path.isEmpty()) {
            Object distanceObj = resData.containsKey("shortestDistance")
                ? resData.get("shortestDistance")
                : resData.get("distance");

            if (distanceObj instanceof Number) {
                double distance = ((Number) distanceObj).doubleValue();
                if (Double.isFinite(distance)) {
                    return "Path: " + formatNodePath(path)
                        + " | Total Bobot: " + formatWeight(distance);
                }
            }
            return "Path: " + formatNodePath(path);
        }

        List<List<Integer>> mstEdges = (List<List<Integer>>) resData.get("mstEdges");
        if (mstEdges != null && !mstEdges.isEmpty()) {
            Object totalWeightObj = resData.get("mstWeight");
            if (totalWeightObj instanceof Number) {
                double totalWeight = ((Number) totalWeightObj).doubleValue();
                return "MST: " + formatMstEdges(mstEdges)
                    + " | Total Bobot: " + formatWeight(totalWeight);
            }
            return "MST: " + formatMstEdges(mstEdges);
        }

        return "";
    }

    private String formatNodePath(List<Integer> nodes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            if (i > 0) sb.append(" \u2192 ");
            sb.append(nodes.get(i));
        }
        return sb.toString();
    }

    private String formatWeight(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-9) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.2f", value);
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

    @SuppressWarnings("unchecked")
    private void addMstButtons(Map<String, Object> data) {
        List<List<Integer>> mstEdges = (List<List<Integer>>) data.get("mstEdges");
        if (mstEdges == null || mstEdges.isEmpty()) return;

        Button btn = smallBtn("Highlight MST", "#1976D2");
        btn.setOnAction(e -> highlightTreeEdges(mstEdges));
        resultActionPane.getChildren().add(btn);
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

    private void highlightTreeEdges(List<List<Integer>> mstEdges) {
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

    private String formatMstEdges(List<List<Integer>> edges) {
        StringBuilder sb = new StringBuilder();
        for (List<Integer> edge : edges) {
            if (edge == null || edge.size() < 2) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(edge.get(0)).append("-").append(edge.get(1));
        }
        return sb.toString();
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
