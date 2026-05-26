package com.grafapp.ui;

import com.grafapp.visualization.GraphCanvas;
import com.grafapp.visualization.SimulationController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Main layout: Sidebar (kiri) | Canvas (tengah) | ControlPanel (kanan)
 * Result panel di bawah canvas.
 */
public class MainView extends BorderPane {

    private final AlgorithmSidebar sidebar;
    private final GraphCanvas canvas;
    private final ControlPanel controlPanel;
    private final SimulationController simulation;
    private final ResultPanel resultPanel;

    public MainView() {
        canvas = new GraphCanvas();
        simulation = new SimulationController(canvas);
        sidebar = new AlgorithmSidebar();
        resultPanel = new ResultPanel();
        resultPanel.setCanvas(canvas);
        controlPanel = new ControlPanel(canvas, simulation, resultPanel);

        sidebar.setOnAlgorithmSelected(algo -> controlPanel.setAlgorithm(algo));

        // Toolbar di atas canvas
        HBox toolbar = createToolbar();

        // Canvas + result panel di tengah
        VBox centerBox = new VBox();
        VBox.setVgrow(canvas, Priority.ALWAYS);
        centerBox.getChildren().addAll(toolbar, canvas, resultPanel);

        setLeft(sidebar);
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(centerBox, controlPanel);
        splitPane.setDividerPositions(0.75); // Canvas gets 75%, ControlPanel gets 25%
        SplitPane.setResizableWithParent(controlPanel, false);

        setCenter(splitPane);
        setStyle("-fx-background-color: #F5F5F5;");
    }

    private HBox createToolbar() {
        ToggleGroup modeGroup = new ToggleGroup();

        ToggleButton selectBtn = modeBtn("🕹  Select", modeGroup, true);
        ToggleButton addNodeBtn = modeBtn("+  Node", modeGroup, false);
        ToggleButton addEdgeBtn = modeBtn("+  Edge", modeGroup, false);
        ToggleButton deleteBtn = modeBtn("✖  Delete", modeGroup, false);

        Label modeLabel = new Label("Mode: Select");
        modeLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        modeLabel.setTextFill(Color.web("#616161"));

        selectBtn.setOnAction(e -> { canvas.setMode(GraphCanvas.InteractionMode.SELECT); modeLabel.setText("Mode: Select"); });
        addNodeBtn.setOnAction(e -> { canvas.setMode(GraphCanvas.InteractionMode.ADD_NODE); modeLabel.setText("Mode: + Node"); });
        addEdgeBtn.setOnAction(e -> { canvas.setMode(GraphCanvas.InteractionMode.ADD_EDGE); modeLabel.setText("Mode: + Edge"); });
        deleteBtn.setOnAction(e -> { canvas.setMode(GraphCanvas.InteractionMode.DELETE); modeLabel.setText("Mode: Delete"); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button resetBtn = new Button("↺  Reset View");
        resetBtn.setFont(Font.font("Segoe UI", 11));
        resetBtn.setStyle("-fx-background-color: #ECEFF1; -fx-text-fill: #424242; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 5 10;");
        resetBtn.setOnAction(e -> canvas.resetLayout());

        Button relayoutBtn = new Button("⚡  Re-layout");
        relayoutBtn.setFont(Font.font("Segoe UI", 11));
        relayoutBtn.setStyle("-fx-background-color: #ECEFF1; -fx-text-fill: #424242; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 5 10;");
        relayoutBtn.setOnAction(e -> canvas.startLayout());

        HBox toolbar = new HBox(6, selectBtn, addNodeBtn, addEdgeBtn, deleteBtn, modeLabel, spacer, relayoutBtn, resetBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(6, 12, 6, 12));
        toolbar.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");
        return toolbar;
    }

    private ToggleButton modeBtn(String text, ToggleGroup group, boolean selected) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setSelected(selected);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        btn.setStyle("-fx-background-color: #ECEFF1; -fx-text-fill: #424242; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 5 12;");
        btn.selectedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                btn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 5 12;");
            } else {
                btn.setStyle("-fx-background-color: #ECEFF1; -fx-text-fill: #424242; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 5 12;");
            }
        });
        return btn;
    }

    public void startLayout() {
        canvas.startLayout();
    }
}
