package com.grafapp.ui;

import com.grafapp.algorithm.*;
import com.grafapp.model.*;
import com.grafapp.util.GraphParser;
import com.grafapp.visualization.GraphCanvas;
import com.grafapp.visualization.SimulationController;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

/**
 * Panel kontrol kanan: input graf, parameter algoritma, dan kontrol simulasi.
 * Desain non-scrollable, compact, informatif.
 */
public class ControlPanel extends VBox {

    private static final String INPUT_MODE_EDGE_LIST = "Edge List";
    private static final String INPUT_MODE_TSP_COORDINATES = "TSP Coordinates";
    private static final List<String> SAMPLE_DATA_FILES = List.of(
        "bandwidth_zigzag_path_10.txt",
        "bandwidth_zigzag_path_15.txt",
        "bandwidth_zigzag_path_30.txt",
        "binary_tree.txt",
        "disconnected_graph.txt",
        "djikstra_1.txt",
        "djikstra_2.txt",
        "djikstra_3.txt",
        "graph_1.txt",
        "graph_2.txt",
        "graph_3.txt",
        "graph_4.txt",
        "graph_5.txt",
        "graph_6.txt",
        "graph_7.txt",
        "grid_graph.txt",
        "indonesia.txt",
        "island_grid_1.txt",
        "island_grid_2.txt",
        "k3_3.txt",
        "k5.txt",
        "kota_bandung.txt",
        "line_graph.txt",
        "matching_large.txt",
        "matching_limited.txt",
        "matching_medium.txt",
        "matching_small.txt",
        "path_1.txt",
        "path_2.txt",
        "petersen_graph.txt",
        "star_graph.txt",
        "timetable_large.txt",
        "timetable_medium.txt",
        "timetable_small.txt",
        "tsp_1.txt",
        "tsp_2.txt",
        "tsp_3.txt",
        "tsp_4.txt",
        "tsp_5.txt",
        "tsp_6.txt",
        "tsp_coordinates_1.txt",
        "tsp_coordinates_2.txt",
        "tsp_coordinates_3.txt",
        "tsp_coordinates_4.txt",
        "tsp_coordinates_5.txt"
    );

    private final GraphCanvas canvas;
    private final SimulationController simulation;

    private TextArea graphInputArea;
    private ComboBox<String> fileCombo;
    private ComboBox<String> inputModeCombo;
    private VBox parameterBox;
    private Label algoNameLabel;
    private Label stepLabel;
    private Label messageLabel;
    private Slider speedSlider;
    private Button playBtn, pauseBtn;
    private CheckBox directedCb;
    private CheckBox weightedCb;
    private CheckBox tspHasLabelsCb;
    private CheckBox showEdgeWeightsCb;

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
    private boolean programmaticInputChange;
    private GraphLayoutPresetApplier pendingPresetLayout;

    // Right-panel result section
    private Label resultSummaryLabel;
    private FlowPane resultActionPane;

    @FunctionalInterface
    private interface GraphLayoutPresetApplier {
        void apply(Graph graph);
    }

    public ControlPanel(GraphCanvas canvas, SimulationController simulation, ResultPanel resultPanel) {
        this.canvas = canvas;
        this.simulation = simulation;
        this.resultPanel = resultPanel;

        setPrefWidth(300);
        setMinWidth(280);
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

        Label formatLabel = new Label("Format Input:");
        formatLabel.setFont(Font.font("Segoe UI", 11));
        formatLabel.setTextFill(Color.web("#616161"));

        inputModeCombo = new ComboBox<>();
        inputModeCombo.getItems().addAll(INPUT_MODE_EDGE_LIST, INPUT_MODE_TSP_COORDINATES);
        inputModeCombo.setValue(INPUT_MODE_EDGE_LIST);
        inputModeCombo.setMaxWidth(Double.MAX_VALUE);
        inputModeCombo.setStyle("-fx-font-size: 11;");
        inputModeCombo.setOnAction(e -> {
            if (isCoordinateInputMode()) {
                pendingPresetLayout = null;
            }
            applyInputModeSettings();
            autoLoadGraph();
        });

        tspHasLabelsCb = new CheckBox("Koordinat memiliki label");
        tspHasLabelsCb.setFont(Font.font("Segoe UI", 11));
        tspHasLabelsCb.setOnAction(e -> autoLoadGraph());

        // File loader
        fileCombo = new ComboBox<>();
        fileCombo.setPromptText("Pilih file");
        fileCombo.setMaxWidth(Double.MAX_VALUE);
        fileCombo.setStyle("-fx-font-size: 11;");
        populateFileList();
        fileCombo.setOnAction(e -> loadFromFile());

        graphInputArea = new TextArea();
        graphInputArea.setPrefRowCount(5);
        graphInputArea.setFont(Font.font("Consolas", 11));
        graphInputArea.setStyle("-fx-control-inner-background: #FAFAFA;");

        // Auto-generate graf saat mengetik
        autoLoadDebounce = new PauseTransition(Duration.millis(400));
        autoLoadDebounce.setOnFinished(e -> autoLoadGraph());
        graphInputArea.textProperty().addListener((obs, o, n) -> {
            if (!programmaticInputChange) {
                pendingPresetLayout = null;
                autoLoadDebounce.playFromStart();
            }
        });

        directedCb = new CheckBox("Directed");
        directedCb.setFont(Font.font("Segoe UI", 11));
        directedCb.setOnAction(e -> autoLoadGraph());

        weightedCb = new CheckBox("Weighted");
        weightedCb.setFont(Font.font("Segoe UI", 11));
        weightedCb.setOnAction(e -> autoLoadGraph());

        showEdgeWeightsCb = new CheckBox("Tampilkan Jarak");
        showEdgeWeightsCb.setFont(Font.font("Segoe UI", 11));
        showEdgeWeightsCb.setSelected(true);
        showEdgeWeightsCb.setOnAction(e -> canvas.setShowEdgeWeights(showEdgeWeightsCb.isSelected()));

        applyInputModeSettings();

        HBox row = new HBox(6);
        Button clearBtn = smallBtn("Clear", "#757575");

        clearBtn.setOnAction(e -> {
            pendingPresetLayout = null;
            fileCombo.getSelectionModel().clearSelection();
            graphInputArea.clear();
            canvas.setGraph(new Graph());
            canvas.draw();
            updateGraphInfo(new Graph(), -1);
        });
        row.getChildren().addAll(clearBtn, directedCb, weightedCb, showEdgeWeightsCb, tspHasLabelsCb);
        row.setAlignment(Pos.CENTER_LEFT);

        sec.getChildren().addAll(title, formatLabel, inputModeCombo, fileCombo, graphInputArea, row);
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
            applyGeneratedGraphText(generateCompleteGraphKn(n), this::applyCompleteGraphLayout);
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
            applyGeneratedGraphText(
                generateCompleteBipartiteKmn(m, n),
                graph -> applyCompleteBipartiteLayout(graph, m, n)
            );
        });

        addGraphTypeButton(buttonPane, "Pohon Tn", () -> {
            Integer n = promptInt("Pohon Tn", "Masukkan n", 10, 2, 80);
            if (n == null) return;
            applyGeneratedGraphText(generateTree(n), graph -> applyTreeLayout(graph, n));
        });

        addGraphTypeButton(buttonPane, "Siklus Cn", () -> {
            Integer n = promptInt("Siklus Cn", "Masukkan n", 8, 3, 80);
            if (n == null) return;
            applyGeneratedGraphText(generateCycleCn(n), this::applyCycleLayout);
        });

        addGraphTypeButton(buttonPane, "Lintasan Pn", () -> {
            Integer n = promptInt("Lintasan Pn", "Masukkan n", 8, 2, 80);
            if (n == null) return;
            applyGeneratedGraphText(generatePathPn(n), this::applyPathLayout);
        });

        addGraphTypeButton(buttonPane, "Graf Roda Wn", () -> {
            Integer n = promptInt("Graf Roda Wn", "Masukkan n", 8, 4, 40);
            if (n == null) return;
            applyGeneratedGraphText(generateWheelWn(n), graph -> applyWheelLayout(graph, n));
        });

        addGraphTypeButton(buttonPane, "Graf Prisma", () -> {
            Integer n = promptInt("Graf Prisma", "Masukkan n", 6, 3, 30);
            if (n == null) return;
            applyGeneratedGraphText(generatePrismGraph(n), graph -> applyPrismLayout(graph, n));
        });

        addGraphTypeButton(buttonPane, "Petersen Graph", () -> {
            int n = 5;
            int k = 2;
            applyGeneratedGraphText(
                generateGeneralizedPetersen(n, k),
                graph -> applyGeneralizedPetersenLayout(graph, n)
            );
        });

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

            applyGeneratedGraphText(
                generateGeneralizedPetersen(n, k),
                graph -> applyGeneralizedPetersenLayout(graph, n)
            );
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

            applyGeneratedGraphText(generateCirculantGraph(n, a1, a2), this::applyCirculantLayout);
        });

        addGraphTypeButton(buttonPane, "Hypercubes H(n)", () -> {
            Integer n = promptInt("Hypercube H(n)", "Masukkan dimensi n", 4, 1, 7);
            if (n == null) return;
            applyGeneratedGraphText(generateHypercube(n), graph -> applyHypercubeLayout(graph, n));
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
            applyGeneratedGraphText(generateGridGraph(m, n), graph -> applyGridLayout(graph, m, n));
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
        applyGeneratedGraphText(graphText, null);
    }

    private void applyGeneratedGraphText(String graphText, GraphLayoutPresetApplier layoutPreset) {
        pendingPresetLayout = layoutPreset;
        fileCombo.getSelectionModel().clearSelection();
        inputModeCombo.setValue(INPUT_MODE_EDGE_LIST);
        applyInputModeSettings();
        directedCb.setSelected(false);
        weightedCb.setSelected(false);

        programmaticInputChange = true;
        try {
            graphInputArea.setText(graphText);
        } finally {
            programmaticInputChange = false;
        }

        autoLoadDebounce.stop();
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

    private void applyCompleteGraphLayout(Graph graph) {
        applyCycleLayout(graph);
    }

    private void applyCompleteBipartiteLayout(Graph graph, int m, int n) {
        List<Integer> upper = new ArrayList<>();
        List<Integer> lower = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            upper.add(i);
        }
        for (int i = 0; i < n; i++) {
            lower.add(m + i);
        }

        layoutNodesOnLine(graph, upper, 120, 820, 200);
        layoutNodesOnLine(graph, lower, 120, 820, 500);
    }

    private void applyTreeLayout(Graph graph, int nodeCount) {
        if (nodeCount <= 0) {
            return;
        }

        double topY = 100;
        double bottomY = 620;
        int maxDepth = estimateTreeDepth(nodeCount);
        double minX = 80;
        double maxX = 860;
        double totalWidth = maxX - minX;

        for (int id = 0; id < nodeCount; id++) {
            int level = (int) (Math.log(id + 1) / Math.log(2));
            int nodesInLevel = 1 << level;
            int posInLevel = id - (nodesInLevel - 1);
            
            double y = maxDepth <= 1 ? topY : topY + (bottomY - topY) * level / (maxDepth - 1);
            double x = minX + (posInLevel + 0.5) * (totalWidth / nodesInLevel);
            
            setNodePosition(graph, id, x, y);
        }
    }

    private int estimateTreeDepth(int nodeCount) {
        int depth = 0;
        int covered = 0;
        int nodesInLevel = 1;
        while (covered < nodeCount) {
            covered += nodesInLevel;
            nodesInLevel <<= 1;
            depth++;
        }
        return Math.max(1, depth);
    }

    private void applyCycleLayout(Graph graph) {
        List<Integer> ids = new ArrayList<>(graph.getNodeIds());
        Collections.sort(ids);
        layoutNodesOnCircle(graph, ids, 460, 340, 250, -Math.PI / 2.0);
    }

    private void applyPathLayout(Graph graph) {
        List<Integer> ids = new ArrayList<>(graph.getNodeIds());
        Collections.sort(ids);
        if (ids.isEmpty()) {
            return;
        }

        int columns = Math.min(10, Math.max(2, ids.size()));
        double startX = 100;
        double startY = 140;
        double stepX = 85;
        double stepY = 95;

        for (int i = 0; i < ids.size(); i++) {
            int row = i / columns;
            int col = i % columns;
            if (row % 2 == 1) {
                col = columns - 1 - col;
            }

            setNodePosition(
                graph,
                ids.get(i),
                startX + col * stepX,
                startY + row * stepY
            );
        }
    }

    private void applyWheelLayout(Graph graph, int n) {
        if (n < 4) {
            applyCycleLayout(graph);
            return;
        }

        int centerId = n - 1;
        setNodePosition(graph, centerId, 460, 340);

        List<Integer> rimIds = new ArrayList<>();
        for (int i = 0; i < n - 1; i++) {
            rimIds.add(i);
        }
        layoutNodesOnCircle(graph, rimIds, 460, 340, 250, -Math.PI / 2.0);
    }

    private void applyPrismLayout(Graph graph, int n) {
        List<Integer> top = new ArrayList<>();
        List<Integer> bottom = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            top.add(i);
            bottom.add(i + n);
        }

        layoutNodesOnCircle(graph, top, 410, 280, 170, -Math.PI / 2.0);
        layoutNodesOnCircle(graph, bottom, 530, 400, 170, -Math.PI / 2.0);
    }

    private void applyGeneralizedPetersenLayout(Graph graph, int n) {
        List<Integer> outer = new ArrayList<>();
        List<Integer> inner = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            outer.add(i);
            inner.add(i + n);
        }

        layoutNodesOnCircle(graph, outer, 460, 340, 250, -Math.PI / 2.0);
        layoutNodesOnCircle(graph, inner, 460, 340, 130, -Math.PI / 2.0);
    }

    private void applyCirculantLayout(Graph graph) {
        List<Integer> ids = new ArrayList<>(graph.getNodeIds());
        Collections.sort(ids);
        layoutNodesOnCircle(graph, ids, 460, 340, 250, -Math.PI / 2.0);
    }

    private void applyHypercubeLayout(Graph graph, int dimension) {
        if (dimension <= 0) {
            return;
        }

        if (dimension == 1) {
            setNodePosition(graph, 0, 320, 340);
            setNodePosition(graph, 1, 600, 340);
            return;
        }

        if (dimension == 2) {
            applySquare(graph, 0, 460, 340, 300);
            return;
        }

        if (dimension == 3) {
            applyCube(graph, 0, 450, 350, 230, 75, 55);
            return;
        }

        if (dimension == 4) {
            applyCube(graph, 0, 360, 300, 170, 55, 40);
            applyCube(graph, 8, 560, 430, 170, 55, 40);
            return;
        }

        applyProjectedHypercube(graph, dimension);
    }

    private void applySquare(Graph graph, int baseId, double cx, double cy, double side) {
        double h = side / 2.0;
        setNodePosition(graph, baseId, cx - h, cy - h);
        setNodePosition(graph, baseId + 1, cx - h, cy + h);
        setNodePosition(graph, baseId + 3, cx + h, cy + h);
        setNodePosition(graph, baseId + 2, cx + h, cy - h);
    }

    private void applyCube(Graph graph, int baseId, double cx, double cy, double side, double dx, double dy) {
        applySquare(graph, baseId, cx, cy, side);

        double h = side / 2.0;
        setNodePosition(graph, baseId + 4, cx - h + dx, cy - h - dy);
        setNodePosition(graph, baseId + 5, cx - h + dx, cy + h - dy);
        setNodePosition(graph, baseId + 7, cx + h + dx, cy + h - dy);
        setNodePosition(graph, baseId + 6, cx + h + dx, cy - h - dy);
    }

    private void applyProjectedHypercube(Graph graph, int dimension) {
        int nodeCount = 1 << dimension;
        double[] xs = new double[nodeCount];
        double[] ys = new double[nodeCount];
        double maxRadius = 1e-9;

        for (int id = 0; id < nodeCount; id++) {
            double x = 0;
            double y = 0;
            for (int bit = 0; bit < dimension; bit++) {
                if ((id & (1 << bit)) != 0) {
                    double angle = -Math.PI / 2.0 + 2.0 * Math.PI * bit / dimension;
                    double weight = 1.0 + 0.18 * bit;
                    x += weight * Math.cos(angle);
                    y += weight * Math.sin(angle);
                }
            }
            xs[id] = x;
            ys[id] = y;
            maxRadius = Math.max(maxRadius, Math.max(Math.abs(x), Math.abs(y)));
        }

        double cx = 460;
        double cy = 340;
        double scale = 260.0 / maxRadius;

        for (int id = 0; id < nodeCount; id++) {
            setNodePosition(graph, id, cx + xs[id] * scale, cy + ys[id] * scale);
        }
    }

    private void applyGridLayout(Graph graph, int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            return;
        }

        double startX = 100;
        double startY = 100;
        double stepX = Math.min(130, 720.0 / Math.max(1, cols - 1));
        double stepY = Math.min(130, 520.0 / Math.max(1, rows - 1));

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int id = r * cols + c;
                setNodePosition(graph, id, startX + c * stepX, startY + r * stepY);
            }
        }
    }

    private void layoutNodesOnLine(Graph graph, List<Integer> ids, double minX, double maxX, double y) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        int count = ids.size();
        for (int i = 0; i < count; i++) {
            double x = count == 1
                ? (minX + maxX) / 2.0
                : minX + (maxX - minX) * i / (count - 1.0);
            setNodePosition(graph, ids.get(i), x, y);
        }
    }

    private void layoutNodesOnCircle(
        Graph graph,
        List<Integer> ids,
        double cx,
        double cy,
        double radius,
        double startAngle
    ) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        int count = ids.size();
        for (int i = 0; i < count; i++) {
            double angle = startAngle + 2.0 * Math.PI * i / count;
            setNodePosition(
                graph,
                ids.get(i),
                cx + radius * Math.cos(angle),
                cy + radius * Math.sin(angle)
            );
        }
    }

    private void setNodePosition(Graph graph, int nodeId, double x, double y) {
        GraphNode node = graph.getNode(nodeId);
        if (node == null) {
            return;
        }
        node.setX(x);
        node.setY(y);
        node.setVx(0);
        node.setVy(0);
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
            String label = formatNodeId(startVtx, g);
            startVertexLabel.setText("Start Vertex (dari file): " + label);
            startNodeField.setText(label);
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

        GraphParser.ParseResult result;
        if (isCoordinateInputMode()) {
            result = GraphParser.parseTspCoordinates(text, tspHasLabelsCb.isSelected());
        } else {
            result = GraphParser.parseEdgeListWithStart(
                text,
                directedCb.isSelected(),
                weightedCb.isSelected()
            );
        }

        Graph g = result.getGraph();
        parsedStartVertex = result.getStartVertex();

        GraphLayoutPresetApplier layoutApplier = null;
        if (!result.hasFixedCoordinates() && g.getNodeCount() > 0) {
            layoutApplier = pendingPresetLayout;
            if (layoutApplier == null && !isCoordinateInputMode()) {
                BipartitePartition partition = computeBipartitePartition(g);
                if (partition != null) {
                    layoutApplier = graph -> applyBipartiteLayout(graph, partition);
                }
            }
        }

        canvas.setGraph(g, result.hasFixedCoordinates());

        if (layoutApplier != null) {
            layoutApplier.apply(canvas.getGraph());
            canvas.draw();
        } else if (g.getNodeCount() > 0 && !result.hasFixedCoordinates()) {
            canvas.startLayout();
        }

        pendingPresetLayout = null;

        updateGraphInfo(g, parsedStartVertex);
    }

    private void applyBipartiteLayout(Graph graph, BipartitePartition partition) {
        List<Integer> left = new ArrayList<>(partition.left);
        List<Integer> right = new ArrayList<>(partition.right);
        sortByLabel(left, graph);
        sortByLabel(right, graph);

        layoutNodesOnLine(graph, left, 120, 820, 200);
        layoutNodesOnLine(graph, right, 120, 820, 520);
    }

    private void sortByLabel(List<Integer> nodes, Graph graph) {
        nodes.sort((a, b) -> {
            String la = formatNodeId(a, graph);
            String lb = formatNodeId(b, graph);
            int cmp = la.compareToIgnoreCase(lb);
            if (cmp != 0) return cmp;
            return Integer.compare(a, b);
        });
    }

    private BipartitePartition computeBipartitePartition(Graph graph) {
        Map<Integer, Integer> color = new HashMap<>();
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        List<Integer> ids = new ArrayList<>(graph.getNodeIds());
        Collections.sort(ids);

        for (int start : ids) {
            if (color.containsKey(start)) {
                continue;
            }
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(start);
            color.put(start, 0);
            left.add(start);

            while (!queue.isEmpty()) {
                int u = queue.poll();
                int uColor = color.get(u);
                for (int v : graph.getNeighbors(u)) {
                    if (!color.containsKey(v)) {
                        int vColor = 1 - uColor;
                        color.put(v, vColor);
                        if (vColor == 0) {
                            left.add(v);
                        } else {
                            right.add(v);
                        }
                        queue.add(v);
                    } else if (color.get(v).equals(uColor)) {
                        return null;
                    }
                }
            }
        }

        return new BipartitePartition(left, right);
    }

    private Integer resolveNodeInput(String input, Graph graph) {
        if (graph == null || input.isEmpty()) {
            return null;
        }
        try {
            int numeric = Integer.parseInt(input);
            if (graph.getNode(numeric) != null) {
                return numeric;
            }
        } catch (NumberFormatException ex) {
            // ignore
        }

        GraphNode fallback = null;
        for (GraphNode node : graph.getNodes()) {
            if (input.equals(node.getLabel())) {
                return node.getId();
            }
        }
        for (GraphNode node : graph.getNodes()) {
            if (node.getLabel() != null && node.getLabel().equalsIgnoreCase(input)) {
                if (fallback != null) {
                    return null;
                }
                fallback = node;
            }
        }
        return fallback != null ? fallback.getId() : null;
    }

    private static final class BipartitePartition {
        private final List<Integer> left;
        private final List<Integer> right;

        private BipartitePartition(List<Integer> left, List<Integer> right) {
            this.left = left;
            this.right = right;
        }
    }

    private boolean isCoordinateInputMode() {
        return INPUT_MODE_TSP_COORDINATES.equals(inputModeCombo.getValue());
    }

    private void applyInputModeSettings() {
        boolean coordinateMode = isCoordinateInputMode();

        directedCb.setDisable(coordinateMode);
        weightedCb.setDisable(coordinateMode);

        if (coordinateMode) {
            directedCb.setSelected(false);
            weightedCb.setSelected(true);
            
            if (tspHasLabelsCb != null && tspHasLabelsCb.isSelected()) {
                graphInputArea.setPromptText(
                    "Format koordinat TSP (dengan label):\n"
                        + "5\n"
                        + "Jakarta 1 3\n"
                        + "Bandung 2 4\n"
                        + "Bali 8 9\n"
                        + "Surabaya 2 0\n"
                        + "Papua -2 4"
                );
            } else {
                graphInputArea.setPromptText(
                    "Format koordinat TSP:\n"
                        + "5\n"
                        + "1 3\n"
                        + "2 4\n"
                        + "8 9\n"
                        + "2 0\n"
                        + "-2 4"
                );
            }
            if (tspHasLabelsCb != null) {
                tspHasLabelsCb.setVisible(true);
                tspHasLabelsCb.setManaged(true);
            }
        } else {
            if (tspHasLabelsCb != null) {
                tspHasLabelsCb.setVisible(false);
                tspHasLabelsCb.setManaged(false);
            }
            graphInputArea.setPromptText("Masukkan input");
        }
    }

    private void loadFromFile() {
        String selected = fileCombo.getValue();
        if (selected == null || selected.isEmpty()) return;
        try {
            String content = GraphParser.readFileText("/data/" + selected);

            String normalized = content.replace("\r\n", "\n").replace("\r", "\n");
            if (GraphParser.isTspCoordinateFormat(normalized)) {
                pendingPresetLayout = null;
                inputModeCombo.setValue(INPUT_MODE_TSP_COORDINATES);
                
                // Auto-detect labels
                boolean labelDet = false;
                List<String> validLines = new ArrayList<>();
                for (String l : normalized.split("\n")) {
                    String tl = l.trim();
                    if (!tl.isEmpty() && !tl.startsWith("#") && !tl.startsWith("//")) {
                        validLines.add(tl);
                    }
                }
                if (validLines.size() > 1) {
                    String[] firstParts = validLines.get(0).split("[\\s,;]+");
                    int startIndex = (firstParts.length == 1) ? 1 : 0;
                    if (validLines.size() > startIndex) {
                        String[] dataParts = validLines.get(startIndex).split("[\\s,;]+");
                        if (dataParts.length > 2) {
                            try { Double.parseDouble(dataParts[0]); } catch (NumberFormatException e) { labelDet = true; }
                        }
                    }
                }
                if (tspHasLabelsCb != null) tspHasLabelsCb.setSelected(labelDet);
            } else {
                pendingPresetLayout = null;
                inputModeCombo.setValue(INPUT_MODE_EDGE_LIST);
            }
            applyInputModeSettings();

            programmaticInputChange = true;
            try {
                graphInputArea.setText(normalized);
            } finally {
                programmaticInputChange = false;
            }
            autoLoadDebounce.stop();
            autoLoadGraph();
        } catch (Exception ex) {
            programmaticInputChange = false;
            warn("Gagal membaca file: " + ex.getMessage());
        }
    }

    private void populateFileList() {
        fileCombo.getItems().setAll(SAMPLE_DATA_FILES);
    }

    private void runAlgorithm() {
        if (currentAlgorithm == null) { warn("Pilih algoritma dari sidebar."); return; }
        if (canvas.getGraph() == null || canvas.getGraph().getNodeCount() == 0) {
            warn("Masukkan graf terlebih dahulu."); return;
        }

        Graph graph = canvas.getGraph();

        Map<String, Object> params = new HashMap<>();
        for (ParameterInfo p : currentAlgorithm.getRequiredParameters()) {
            Control ctrl = paramControls.get(p.getKey());
            if (ctrl instanceof TextField) {
                String txt = ((TextField) ctrl).getText().trim();
                if (txt.isEmpty() && p.isRequired()) {
                    warn("Parameter '" + p.getLabel() + "' wajib diisi."); return;
                }
                if (p.getType() == ParameterInfo.Type.NODE_SELECT) {
                    Integer nodeId = resolveNodeInput(txt, graph);
                    if (nodeId == null) {
                        warn("Node '" + txt + "' tidak ditemukan."); return;
                    }
                    params.put(p.getKey(), nodeId);
                } else {
                    try {
                        params.put(p.getKey(), Integer.parseInt(txt));
                    } catch (NumberFormatException ex) {
                        warn("Parameter '" + p.getLabel() + "' harus angka."); return;
                    }
                }
            } else if (ctrl instanceof CheckBox) {
                params.put(p.getKey(), ((CheckBox) ctrl).isSelected());
            }
        }

        canvas.getGraph().resetStates();
        AlgorithmResult result = currentAlgorithm.execute(canvas.getGraph(), params);
        simulation.loadResult(result);

        Map<String, Object> resData = result.getData();
        String summaryText = buildResultSummaryText(result, resData, graph);

        // Tampilkan hasil di bottom result panel
        resultPanel.setSummary(summaryText);
        resultPanel.clearActions();

        // Tampilkan hasil di right-panel result section
        resultActionPane.getChildren().clear();
        if (resData.containsKey("timetable")) {
            resultSummaryLabel.setText(result.getSummary());
            @SuppressWarnings("unchecked")
            List<List<List<Integer>>> timetable = (List<List<List<Integer>>>) resData.get("timetable");
            resultActionPane.getChildren().add(buildTimetableView(timetable, graph));
        } else {
            resultSummaryLabel.setText(summaryText);
        }

        resultPanel.setDetail(buildResultDetailText(resData, graph));

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

        // Bandwidth actions
        if (resData.containsKey("bandwidthOrderAfter")) {
            Runnable showView = () -> showBandwidthPopup(resData, graph);
            resultPanel.setBandwidthActions(showView);
            addBandwidthButtons(resData);
            showBandwidthPopup(resData, graph);
        }

        simulation.play();
    }

    @SuppressWarnings("unchecked")
    private String buildResultDetailText(Map<String, Object> resData, Graph graph) {
        List<Integer> order = (List<Integer>) resData.get("traversalOrder");
        if (order != null && !order.isEmpty()) {
            return "Traversal: " + formatNodePath(order, graph);
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
                    return "Path: " + formatNodePath(path, graph)
                        + " | Total Bobot: " + formatWeight(distance);
                }
            }
            return "Path: " + formatNodePath(path, graph);
        }

        List<List<Integer>> matchingEdges = (List<List<Integer>>) resData.get("matchingEdges");
        if (matchingEdges != null) {
            Object sizeObj = resData.get("matchingSize");
            int size = sizeObj instanceof Number ? ((Number) sizeObj).intValue() : matchingEdges.size();
            String pairs = matchingEdges.isEmpty()
                ? "(kosong)"
                : formatMatchingEdges(matchingEdges, graph);
            return "Matching size: " + size + " | Pairs: " + pairs;
        }

        List<List<List<Integer>>> timetable = (List<List<List<Integer>>>) resData.get("timetable");
        if (timetable != null) {
            Object periodObj = resData.get("periodCount");
            int periods = periodObj instanceof Number ? ((Number) periodObj).intValue() : timetable.size();
            return formatTimetable(timetable, periods, graph);
        }

        List<List<Integer>> mstEdges = (List<List<Integer>>) resData.get("mstEdges");
        if (mstEdges != null && !mstEdges.isEmpty()) {
            Object totalWeightObj = resData.get("mstWeight");
            if (totalWeightObj instanceof Number) {
                double totalWeight = ((Number) totalWeightObj).doubleValue();
                return "MST: " + formatMstEdges(mstEdges, graph)
                    + " | Total Bobot: " + formatWeight(totalWeight);
            }
            return "MST: " + formatMstEdges(mstEdges, graph);
        }

        List<Integer> bwBefore = safeNodeList(resData.get("bandwidthOrderBefore"));
        List<Integer> bwAfter = safeNodeList(resData.get("bandwidthOrderAfter"));
        if (bwBefore != null && bwAfter != null) {
            return "Order before: " + formatNodeList(bwBefore, graph)
                + " | Order after: " + formatNodeList(bwAfter, graph);
        }

        return "";
    }

    private String formatNodePath(List<Integer> nodes, Graph graph) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            if (i > 0) sb.append(" \u2192 ");
            sb.append(formatNodeId(nodes.get(i), graph));
        }
        return sb.toString();
    }

    private String formatNodeId(int nodeId, Graph graph) {
        if (graph == null) {
            return String.valueOf(nodeId);
        }
        GraphNode node = graph.getNode(nodeId);
        if (node != null && node.getLabel() != null) {
            return node.getLabel();
        }
        return String.valueOf(nodeId);
    }

    private String formatWeight(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-9) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatMatchingEdges(List<List<Integer>> edges, Graph graph) {
        StringBuilder sb = new StringBuilder();
        for (List<Integer> edge : edges) {
            if (edge == null || edge.size() < 2) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(formatNodeId(edge.get(0), graph))
                .append("-")
                .append(formatNodeId(edge.get(1), graph));
        }
        return sb.toString();
    }

    private String formatTimetable(List<List<List<Integer>>> timetable, int periods, Graph graph) {
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(periods, timetable.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append("\n");
            sb.append("P").append(i + 1).append(": ");
            sb.append(formatMatchingEdges(timetable.get(i), graph));
        }
        return sb.toString();
    }

    private javafx.scene.Node buildTimetableView(List<List<List<Integer>>> timetable, Graph graph) {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setStyle("-fx-border-color: #BDBDBD; -fx-border-width: 1px; -fx-background-color: white;");
        
        Set<Integer> teachers = new TreeSet<>();
        int periods = timetable.size();
        Map<Integer, Map<Integer, Integer>> teacherToPeriodClass = new HashMap<>();
        
        for (int p = 0; p < periods; p++) {
            for (List<Integer> edge : timetable.get(p)) {
                if (edge == null || edge.size() < 2) continue;
                int teacher = edge.get(0);
                int cls = edge.get(1);
                teachers.add(teacher);
                teacherToPeriodClass.computeIfAbsent(teacher, k -> new HashMap<>()).put(p, cls);
            }
        }
        
        Label corner = new Label("Period");
        corner.setStyle("-fx-font-weight: bold; -fx-text-fill: #212121; -fx-padding: 4 8; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 1 0; -fx-background-color: #F5F5F5; -fx-alignment: center;");
        corner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        grid.add(corner, 0, 0);
        
        for (int p = 0; p < periods; p++) {
            Label lbl = new Label(String.valueOf(p + 1));
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #212121; -fx-padding: 4 8; -fx-alignment: center; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 1 0; -fx-background-color: #F5F5F5;");
            lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            grid.add(lbl, p + 1, 0);
        }
        
        int row = 1;
        for (int teacherId : teachers) {
            Label tLbl = new Label(formatNodeId(teacherId, graph));
            tLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #212121; -fx-padding: 4 8; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 1 0; -fx-background-color: #FAFAFA; -fx-alignment: center;");
            tLbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            grid.add(tLbl, 0, row);
            
            for (int p = 0; p < periods; p++) {
                Map<Integer, Integer> pMap = teacherToPeriodClass.get(teacherId);
                String cName = "";
                if (pMap != null && pMap.containsKey(p)) {
                    cName = formatNodeId(pMap.get(p), graph);
                }
                Label cLbl = new Label(cName);
                cLbl.setStyle("-fx-text-fill: #212121; -fx-padding: 4 8; -fx-alignment: center; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 1 0;");
                cLbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                grid.add(cLbl, p + 1, row);
            }
            row++;
        }
        
        ScrollPane sp = new ScrollPane(grid);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.prefWidthProperty().bind(resultActionPane.widthProperty().subtract(4));
        sp.setMinHeight(ScrollPane.USE_PREF_SIZE);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return sp;
    }

    private void showBandwidthPopup(Map<String, Object> data, Graph graph) {
        List<Integer> beforeOrder = safeNodeList(data.get("bandwidthOrderBefore"));
        List<Integer> afterOrder = safeNodeList(data.get("bandwidthOrderAfter"));
        if (beforeOrder == null || afterOrder == null || graph == null) {
            return;
        }

        int bwBefore = data.get("bandwidthBefore") instanceof Number
            ? ((Number) data.get("bandwidthBefore")).intValue()
            : -1;
        int bwAfter = data.get("bandwidthAfter") instanceof Number
            ? ((Number) data.get("bandwidthAfter")).intValue()
            : -1;
        String method = String.valueOf(data.getOrDefault("bandwidthMethod", "Heuristic"));

        Stage stage = new Stage();
        stage.setTitle("Graph Bandwidth Comparison");
        stage.initModality(Modality.NONE);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        Label headline = new Label(
            "Bandwidth before: " + bwBefore + " | after: " + bwAfter + " (" + method + ")"
        );
        headline.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        headline.setTextFill(Color.web("#212121"));

        HBox columns = new HBox(12);
        Map<Integer, Integer> afterLabelMap = buildOrderLabelMap(afterOrder);
        BandwidthColumn beforeCol = buildBandwidthColumn("Before", graph, beforeOrder, bwBefore, null);
        BandwidthColumn afterCol = buildBandwidthColumn("After", graph, afterOrder, bwAfter, afterLabelMap);
        HBox.setHgrow(beforeCol.container, Priority.ALWAYS);
        HBox.setHgrow(afterCol.container, Priority.ALWAYS);
        columns.getChildren().addAll(beforeCol.container, afterCol.container);

        VBox mappingBox = new VBox(6);
        Label mappingTitle = new Label("Label mapping (original -> new label)");
        mappingTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        mappingTitle.setTextFill(Color.web("#424242"));

        TextArea mappingArea = new TextArea(buildBandwidthMappingText(graph, beforeOrder, afterOrder));
        mappingArea.setEditable(false);
        mappingArea.setWrapText(true);
        mappingArea.setPrefRowCount(4);
        mappingArea.setStyle("-fx-control-inner-background: #FAFAFA;");
        mappingBox.getChildren().addAll(mappingTitle, mappingArea);

        root.setTop(headline);
        BorderPane.setMargin(headline, new Insets(0, 0, 10, 0));
        root.setCenter(columns);
        root.setBottom(mappingBox);
        BorderPane.setMargin(mappingBox, new Insets(10, 0, 0, 0));

        Scene scene = new Scene(root, 1100, 750);
        stage.setScene(scene);
        stage.show();
        Platform.runLater(() -> {
            beforeCol.canvas.startLayout();
            afterCol.canvas.startLayout();
        });
    }

    private BandwidthColumn buildBandwidthColumn(
        String title,
        Graph graph,
        List<Integer> order,
        int bandwidth,
        Map<Integer, Integer> labelMap
    ) {
        VBox col = new VBox(8);
        col.setPadding(new Insets(8));
        col.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 1;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web("#1565C0"));

        Label bwLabel = new Label("Bandwidth: " + bandwidth);
        bwLabel.setFont(Font.font("Segoe UI", 12));
        bwLabel.setTextFill(Color.web("#424242"));

        Graph viewGraph = cloneGraph(graph);
        if (labelMap != null) {
            applyNodeLabels(viewGraph, labelMap);
        }

        GraphCanvas viewCanvas = new GraphCanvas();
        viewCanvas.setPrefSize(480, 260);
        viewCanvas.setMinSize(320, 200);
        viewCanvas.setShowEdgeWeights(false);
        viewCanvas.setGraph(viewGraph, false);

        Label matrixLabel = new Label("Adjacency matrix");
        matrixLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        matrixLabel.setTextFill(Color.web("#424242"));

        javafx.scene.Node matrixView = buildAdjacencyMatrixView(graph, order);

        col.getChildren().addAll(titleLabel, viewCanvas, bwLabel, matrixLabel, matrixView);
        VBox.setVgrow(viewCanvas, Priority.NEVER);
        VBox.setVgrow(matrixView, Priority.ALWAYS);
        return new BandwidthColumn(col, viewCanvas);
    }

    private javafx.scene.Node buildAdjacencyMatrixView(Graph graph, List<Integer> order) {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setStyle("-fx-border-color: #BDBDBD; -fx-border-width: 1px; -fx-background-color: white;");

        String headerStyle = "-fx-font-weight: bold; -fx-text-fill: #212121; -fx-padding: 3 6; "
            + "-fx-border-color: #E0E0E0; -fx-border-width: 0 1 1 0; -fx-background-color: #F5F5F5; "
            + "-fx-alignment: center;";

        Label corner = new Label("");
        corner.setStyle(headerStyle);
        corner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        grid.add(corner, 0, 0);

        for (int j = 0; j < order.size(); j++) {
            Label lbl = new Label(String.valueOf(j));
            lbl.setStyle(headerStyle);
            lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            grid.add(lbl, j + 1, 0);
        }

        for (int i = 0; i < order.size(); i++) {
            int rowId = order.get(i);
            Label rowLbl = new Label(String.valueOf(i));
            rowLbl.setStyle(headerStyle);
            rowLbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            grid.add(rowLbl, 0, i + 1);

            for (int j = 0; j < order.size(); j++) {
                int colId = order.get(j);
                boolean hasEdge = graph.getEdge(rowId, colId) != null;
                String text = (i == j) ? "0" : (hasEdge ? "1" : "");
                Label cell = new Label(text);
                String cellStyle = "-fx-text-fill: #212121; -fx-padding: 3 6; -fx-alignment: center; "
                    + "-fx-border-color: #E0E0E0; -fx-border-width: 0 1 1 0;";
                if (i == j) {
                    cellStyle += "-fx-background-color: #FAFAFA; -fx-text-fill: #757575;";
                } else if (hasEdge) {
                    cellStyle += "-fx-background-color: #E3F2FD; -fx-text-fill: #0D47A1; "
                        + "-fx-font-weight: bold;";
                }
                cell.setStyle(cellStyle);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                grid.add(cell, j + 1, i + 1);
            }
        }

        ScrollPane sp = new ScrollPane(grid);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setPrefViewportHeight(260);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return sp;
    }

    private Map<Integer, Integer> buildOrderLabelMap(List<Integer> order) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            map.put(order.get(i), i);
        }
        return map;
    }

    private String buildBandwidthMappingText(Graph graph, List<Integer> beforeOrder, List<Integer> afterOrder) {
        Map<Integer, Integer> afterLabelMap = buildOrderLabelMap(afterOrder);
        StringBuilder sb = new StringBuilder();
        for (int id : beforeOrder) {
            sb.append(formatNodeId(id, graph))
                .append(" -> ")
                .append(afterLabelMap.getOrDefault(id, -1))
                .append("\n");
        }
        return sb.toString().trim();
    }

    private void applyNodeLabels(Graph graph, Map<Integer, Integer> labelMap) {
        for (GraphNode node : graph.getNodes()) {
            Integer newLabel = labelMap.get(node.getId());
            if (newLabel != null) {
                node.setLabel(String.valueOf(newLabel));
            }
        }
    }

    private Graph cloneGraph(Graph source) {
        Graph copy = new Graph(source.isDirected());
        copy.setWeighted(source.isWeighted());
        for (GraphNode node : source.getNodes()) {
            GraphNode cloned = new GraphNode(node.getId());
            cloned.setLabel(node.getLabel());
            copy.addNode(cloned);
        }
        for (GraphEdge edge : source.getEdges()) {
            if (source.isWeighted()) {
                copy.addEdge(edge.getSource(), edge.getTarget(), edge.getWeight());
            } else {
                copy.addEdge(edge.getSource(), edge.getTarget());
            }
        }
        return copy;
    }

    private static final class BandwidthColumn {
        private final VBox container;
        private final GraphCanvas canvas;

        private BandwidthColumn(VBox container, GraphCanvas canvas) {
            this.container = container;
            this.canvas = canvas;
        }
    }


    private String formatMstEdges(List<List<Integer>> edges, Graph graph) {
        StringBuilder sb = new StringBuilder();
        for (List<Integer> edge : edges) {
            if (edge == null || edge.size() < 2) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(formatNodeId(edge.get(0), graph))
                .append("-")
                .append(formatNodeId(edge.get(1), graph));
        }
        return sb.toString();
    }

    private String formatNodeList(List<Integer> nodes, Graph graph) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < nodes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(formatNodeId(nodes.get(i), graph));
        }
        sb.append("]");
        return sb.toString();
    }

    private String buildResultSummaryText(AlgorithmResult result, Map<String, Object> resData, Graph graph) {
        String summary = result.getSummary();

        List<Integer> order = safeNodeList(resData.get("traversalOrder"));
        if (order != null && !order.isEmpty()) {
            return "Traversal: " + formatNodePath(order, graph);
        }

        List<Integer> path = safeNodeList(resData.get("shortestPath"));
        if (path == null || path.isEmpty()) {
            path = safeNodeList(resData.get("path"));
        }
        if (path != null && !path.isEmpty()) {
            Object distanceObj = resData.containsKey("shortestDistance")
                ? resData.get("shortestDistance")
                : resData.get("distance");
            if (distanceObj instanceof Number) {
                double distance = ((Number) distanceObj).doubleValue();
                if (Double.isFinite(distance)) {
                    return "Path: " + formatNodePath(path, graph)
                        + " | Total Bobot: " + formatWeight(distance);
                }
            }
            return "Path: " + formatNodePath(path, graph);
        }

        List<List<Integer>> matchingEdges = safeEdgeList(resData.get("matchingEdges"));
        if (matchingEdges != null) {
            Object sizeObj = resData.get("matchingSize");
            int size = sizeObj instanceof Number ? ((Number) sizeObj).intValue() : matchingEdges.size();
            String pairs = matchingEdges.isEmpty()
                ? "(kosong)"
                : formatMatchingEdges(matchingEdges, graph);
            return "Matching size: " + size + " | Pairs: " + pairs;
        }

        List<List<List<Integer>>> timetable = safeTimetable(resData.get("timetable"));
        if (timetable != null) {
            Object periodObj = resData.get("periodCount");
            int periods = periodObj instanceof Number ? ((Number) periodObj).intValue() : timetable.size();
            return formatTimetable(timetable, periods, graph);
        }

        List<List<Integer>> mstEdges = safeEdgeList(resData.get("mstEdges"));
        if (mstEdges != null && !mstEdges.isEmpty()) {
            Object totalWeightObj = resData.get("mstWeight");
            if (totalWeightObj instanceof Number) {
                double totalWeight = ((Number) totalWeightObj).doubleValue();
                return "MST: " + formatMstEdges(mstEdges, graph)
                    + " | Total Bobot: " + formatWeight(totalWeight);
            }
            return "MST: " + formatMstEdges(mstEdges, graph);
        }

        Boolean bipartite = (Boolean) resData.get("bipartite");
        if (Boolean.TRUE.equals(bipartite)) {
            List<Integer> setA = safeNodeList(resData.get("setA"));
            List<Integer> setB = safeNodeList(resData.get("setB"));
            if (setA != null && setB != null) {
                return "Graf bipartit. A=" + formatNodeList(setA, graph)
                    + " | B=" + formatNodeList(setB, graph);
            }
        }

        return summary;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> safeNodeList(Object value) {
        if (value instanceof List) {
            return (List<Integer>) value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<List<Integer>> safeEdgeList(Object value) {
        if (value instanceof List) {
            return (List<List<Integer>>) value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<List<List<Integer>>> safeTimetable(Object value) {
        if (value instanceof List) {
            return (List<List<List<Integer>>>) value;
        }
        return null;
    }

    // Right panel results
    @SuppressWarnings("unchecked")
    private void addBipartiteButtons(Map<String, Object> data) {
        boolean bipartite = (Boolean) data.getOrDefault("bipartite", false);
        if (!bipartite) return;

        Graph graph = canvas.getGraph();

        List<Integer> setA = (List<Integer>) data.get("setA");
        List<Integer> setB = (List<Integer>) data.get("setB");

        if (setA != null && !setA.isEmpty()) {
            Button btnA = smallBtn("Highlight A " + formatNodeList(setA, graph), "#1565C0");
            btnA.setOnAction(e -> highlightNodes(setA, NodeState.COMPONENT_1));
            resultActionPane.getChildren().add(btnA);
        }
        if (setB != null && !setB.isEmpty()) {
            Button btnB = smallBtn("Highlight B " + formatNodeList(setB, graph), "#E65100");
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

    private void addBandwidthButtons(Map<String, Object> data) {
        if (!data.containsKey("bandwidthOrderAfter")) {
            return;
        }
        Button btn = smallBtn("Show bandwidth view", "#1565C0");
        btn.setOnAction(e -> showBandwidthPopup(data, canvas.getGraph()));
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
        return formatMstEdges(edges, canvas.getGraph());
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
