package com.grafapp.ui;

import com.grafapp.algorithm.AlgorithmRegistry;
import com.grafapp.algorithm.GraphAlgorithm;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.function.Consumer;

// Left sidebar menampilkan daftar algoritma yang tersedia
public class AlgorithmSidebar extends VBox {

    private Consumer<GraphAlgorithm> onAlgorithmSelected;
    private final ToggleGroup algorithmGroup = new ToggleGroup();

    public AlgorithmSidebar() {
        setPrefWidth(260);
        setMinWidth(240);
        setMaxWidth(280);
        setSpacing(0);
        setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 0 0;");

        // Header
        Label header = new Label("Graph Algorithm Visualizer");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        header.setTextFill(Color.web("#1565C0"));
        header.setPadding(new Insets(20, 20, 4, 20));

        Label subtitle = new Label("Integrated Visualization Platform");
        subtitle.setFont(Font.font("Segoe UI", 11));
        subtitle.setTextFill(Color.web("#9E9E9E"));
        subtitle.setPadding(new Insets(0, 20, 15, 20));

        getChildren().addAll(header, subtitle, new Separator());

        // Kategori & algoritma
        AlgorithmRegistry registry = AlgorithmRegistry.getInstance();
        for (String category : registry.getCategories()) {
            addCategory(category, registry.getByCategory(category));
        }

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        // Legend warna
        getChildren().add(new Separator());
        getChildren().add(createLegend());

        // Footer
        Label footer = new Label("MA3052 Teori Graf Algoritmik \u2014 ITB");
        footer.setFont(Font.font("Segoe UI", 10));
        footer.setTextFill(Color.web("#BDBDBD"));
        footer.setPadding(new Insets(8, 20, 12, 20));
        getChildren().add(footer);
    }

    public void setOnAlgorithmSelected(Consumer<GraphAlgorithm> cb) {
        this.onAlgorithmSelected = cb;
    }

    private void addCategory(String category, List<GraphAlgorithm> algorithms) {
        Label catLabel = new Label(category.toUpperCase());
        catLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        catLabel.setTextFill(Color.web("#9E9E9E"));
        catLabel.setPadding(new Insets(14, 20, 4, 20));
        getChildren().add(catLabel);

        for (GraphAlgorithm algo : algorithms) {
            ToggleButton btn = createAlgoButton(algo);
            getChildren().add(btn);
        }
    }

    private ToggleButton createAlgoButton(GraphAlgorithm algo) {
        ToggleButton btn = new ToggleButton(algo.getName());
        btn.setToggleGroup(algorithmGroup);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(9, 20, 9, 25));
        btn.setFont(Font.font("Segoe UI", 13));
        applyStyle(btn, false);

        btn.setOnMouseEntered(e -> { if (!btn.isSelected()) applyHoverStyle(btn); });
        btn.setOnMouseExited(e -> { if (!btn.isSelected()) applyStyle(btn, false); });

        btn.selectedProperty().addListener((obs, o, selected) -> {
            applyStyle(btn, selected);
            if (selected && onAlgorithmSelected != null) {
                onAlgorithmSelected.accept(algo);
            }
        });

        Tooltip tip = new Tooltip(algo.getDescription());
        tip.setWrapText(true);
        tip.setMaxWidth(280);
        btn.setTooltip(tip);

        return btn;
    }

    private void applyStyle(ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; "
                + "-fx-font-weight: bold; -fx-background-radius: 0; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #424242; "
                + "-fx-background-radius: 0; -fx-cursor: hand;");
        }
    }

    private void applyHoverStyle(ToggleButton btn) {
        btn.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #212121; "
            + "-fx-background-radius: 0; -fx-cursor: hand;");
    }

    private VBox createLegend() {
        VBox legend = new VBox(3);
        legend.setPadding(new Insets(10, 20, 6, 20));

        Label title = new Label("LEGENDA WARNA NODE");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        title.setTextFill(Color.web("#BDBDBD"));
        legend.getChildren().add(title);

        legend.getChildren().add(legendItem("#FFFFFF", "#9E9E9E", "Unvisited"));
        legend.getChildren().add(legendItem("#FFF9C4", "#F9A825", "Processing"));
        legend.getChildren().add(legendItem("#BBDEFB", "#1976D2", "Visited"));
        legend.getChildren().add(legendItem("#C8E6C9", "#388E3C", "Path"));
        legend.getChildren().add(legendItem("#E0F2F1", "#00897B", "Tree Edge"));
        legend.getChildren().add(legendItem("#FFE0B2", "#E65100", "Articulation Point"));

        return legend;
    }

    private HBox legendItem(String fill, String stroke, String text) {
        Region dot = new Region();
        dot.setMinSize(12, 12);
        dot.setMaxSize(12, 12);
        dot.setStyle("-fx-background-color: " + fill + "; -fx-border-color: " + stroke
            + "; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", 10));
        label.setTextFill(Color.web("#757575"));

        HBox row = new HBox(6, dot, label);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
