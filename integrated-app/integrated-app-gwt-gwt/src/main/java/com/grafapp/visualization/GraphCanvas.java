package com.grafapp.visualization;

import com.grafapp.model.*;
import com.grafapp.layout.ForceDirectedLayout;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;
import java.util.Locale;
import java.util.List;

public class GraphCanvas extends Pane {

    public enum InteractionMode { SELECT, ADD_NODE, ADD_EDGE, DELETE }

    private final Canvas canvas;
    private Graph graph;
    private ForceDirectedLayout layout;
    private boolean fixedCoordinateMode;

    // Transform (zoom & pan)
    private double zoom = 1.0;
    private double panX = 0, panY = 0;

    // Mouse state
    private double lastMouseX, lastMouseY;
    private GraphNode draggedNode;
    private GraphNode edgeStartNode;
    private boolean panning;

    private InteractionMode mode = InteractionMode.SELECT;

    // Appearance
    private double nodeRadius = 22;
    private static final Font NODE_FONT = Font.font("Segoe UI", FontWeight.BOLD, 13);

    private Runnable onGraphChanged;
    
    // Feature toggles
    private boolean showEdgeWeights = true;

    public GraphCanvas() {
        canvas = new Canvas();
        getChildren().add(canvas);

        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        widthProperty().addListener(e -> {
            if (fixedCoordinateMode) {
                fitCoordinatesToCanvas();
            }
            draw();
        });
        heightProperty().addListener(e -> {
            if (fixedCoordinateMode) {
                fitCoordinatesToCanvas();
            }
            draw();
        });

        setupMouseHandlers();
    }

    // Public API
    public void setGraph(Graph graph) {
        setGraph(graph, false);
    }

    public void setGraph(Graph graph, boolean fixedCoordinateMode) {
        this.graph = graph;
        this.fixedCoordinateMode = fixedCoordinateMode;
        if (layout != null) layout.stop();
        layout = new ForceDirectedLayout(graph, getWidth(), getHeight());
        layout.setOnTick(this::draw);

        if (fixedCoordinateMode) {
            fitCoordinatesToCanvas();
        } else {
            layout.circularLayout();
        }

        zoom = 1.0;
        panX = 0;
        panY = 0;
        draw();
    }

    public Graph getGraph() { return graph; }
    public ForceDirectedLayout getLayout() { return layout; }

    public void setMode(InteractionMode mode) { this.mode = mode; }
    public InteractionMode getMode() { return mode; }

    public void setOnGraphChanged(Runnable cb) { this.onGraphChanged = cb; }

    public void startLayout() {
        if (fixedCoordinateMode) {
            return;
        }
        if (layout != null) {
            layout.setDimensions(getWidth(), getHeight());
            layout.start();
        }
    }

    public void stopLayout() {
        if (layout != null) layout.stop();
    }

    public void resetLayout() {
        if (fixedCoordinateMode) {
            fitCoordinatesToCanvas();
        } else if (layout != null) {
            layout.setDimensions(getWidth(), getHeight());
            layout.circularLayout();
        }
        zoom = 1.0;
        panX = 0;
        panY = 0;
        draw();
    }

    // Mouse handlers
    private void setupMouseHandlers() {
        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseReleased(this::handleMouseReleased);
        setOnScroll(this::handleScroll);
    }

    private void handleMousePressed(MouseEvent e) {
        double mx = toGraphX(e.getX());
        double my = toGraphY(e.getY());
        lastMouseX = e.getX();
        lastMouseY = e.getY();

        GraphNode hit = findNodeAt(mx, my);

        switch (mode) {
            case SELECT:
                if (hit != null) {
                    draggedNode = hit;
                    draggedNode.setPinned(true);
                } else {
                    panning = true;
                }
                break;

            case ADD_NODE:
                if (graph != null && hit == null) {
                    int newId = graph.nextAvailableId();
                    GraphNode node = new GraphNode(newId);
                    node.setX(mx);
                    node.setY(my);
                    graph.addNode(node);
                    fireGraphChanged();
                    draw();
                }
                break;

            case ADD_EDGE:
                if (hit != null) {
                    if (edgeStartNode == null) {
                        edgeStartNode = hit;
                        draw();
                    } else {
                        if (edgeStartNode.getId() != hit.getId()) {
                            if (graph.isWeighted()) {
                                TextInputDialog dlg = new TextInputDialog("1");
                                dlg.setTitle("Edge Weight");
                                dlg.setHeaderText("Edge: " + edgeStartNode.getId() + " \u2192 " + hit.getId());
                                dlg.setContentText("Weight:");
                                dlg.showAndWait().ifPresent(val -> {
                                    try {
                                        double w = Double.parseDouble(val);
                                        graph.addEdge(edgeStartNode.getId(), hit.getId(), w);
                                    } catch (NumberFormatException ex) {
                                        graph.addEdge(edgeStartNode.getId(), hit.getId());
                                    }
                                    fireGraphChanged();
                                });
                            } else {
                                graph.addEdge(edgeStartNode.getId(), hit.getId());
                                fireGraphChanged();
                            }
                        }
                        edgeStartNode = null;
                        draw();
                    }
                }
                break;

            case DELETE:
                if (hit != null && graph != null) {
                    graph.removeNode(hit.getId());
                    fireGraphChanged();
                    draw();
                }
                break;
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (draggedNode != null) {
            draggedNode.setX(toGraphX(e.getX()));
            draggedNode.setY(toGraphY(e.getY()));
            draggedNode.setVx(0);
            draggedNode.setVy(0);
            if (layout == null || !layout.isRunning()) draw();
        } else if (panning) {
            panX += e.getX() - lastMouseX;
            panY += e.getY() - lastMouseY;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            if (layout == null || !layout.isRunning()) draw();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (draggedNode != null) {
            draggedNode.setPinned(fixedCoordinateMode && draggedNode.hasCoordinate());
            draggedNode = null;
        }
        panning = false;
    }

    private void fitCoordinatesToCanvas() {
        if (graph == null || graph.getNodeCount() == 0) {
            return;
        }

        double width = getWidth() > 1 ? getWidth() : 600;
        double height = getHeight() > 1 ? getHeight() : 400;
        double margin = 80;
        double drawWidth = Math.max(40, width - 2 * margin);
        double drawHeight = Math.max(40, height - 2 * margin);

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (GraphNode node : graph.getNodes()) {
            double rawX = node.hasCoordinate() ? node.getCoordinateX() : node.getX();
            double rawY = node.hasCoordinate() ? node.getCoordinateY() : node.getY();

            minX = Math.min(minX, rawX);
            maxX = Math.max(maxX, rawX);
            minY = Math.min(minY, rawY);
            maxY = Math.max(maxY, rawY);
        }

        double spanX = maxX - minX;
        double spanY = maxY - minY;

        for (GraphNode node : graph.getNodes()) {
            double rawX = node.hasCoordinate() ? node.getCoordinateX() : node.getX();
            double rawY = node.hasCoordinate() ? node.getCoordinateY() : node.getY();

            double normalizedX = spanX < 1e-9 ? 0.5 : (rawX - minX) / spanX;
            double normalizedY = spanY < 1e-9 ? 0.5 : (rawY - minY) / spanY;

            node.setX(margin + normalizedX * drawWidth);
            // Inversi Y agar sesuai sumbu kartesius (nilai Y lebih besar tampil lebih atas).
            node.setY(height - margin - normalizedY * drawHeight);
            node.setVx(0);
            node.setVy(0);
            if (node.hasCoordinate()) {
                node.setPinned(true);
            }
        }
    }

    private void handleScroll(ScrollEvent e) {
        double factor = e.getDeltaY() > 0 ? 1.1 : 1.0 / 1.1;
        double oldZoom = zoom;
        zoom = Math.max(0.1, Math.min(5.0, zoom * factor));

        // Zoom terhadap posisi kursor
        panX = e.getX() - (e.getX() - panX) * (zoom / oldZoom);
        panY = e.getY() - (e.getY() - panY) * (zoom / oldZoom);

        if (layout == null || !layout.isRunning()) draw();
    }

    // Transformasi koordinat
    private double toGraphX(double sx) { return (sx - panX) / zoom; }
    private double toGraphY(double sy) { return (sy - panY) / zoom; }

    private GraphNode findNodeAt(double gx, double gy) {
        if (graph == null) return null;
        double r2 = (nodeRadius + 5) * (nodeRadius + 5);
        for (GraphNode node : graph.getNodes()) {
            double dx = node.getX() - gx;
            double dy = node.getY() - gy;
            if (dx * dx + dy * dy <= r2) return node;
        }
        return null;
    }

    private void fireGraphChanged() {
        if (onGraphChanged != null) onGraphChanged.run();
    }

    // Menggambar graf
    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        // Background
        gc.setFill(Color.web("#F8F9FA"));
        gc.fillRect(0, 0, w, h);
        drawGrid(gc, w, h);

        if (graph == null || graph.getNodeCount() == 0) {
            drawPlaceholder(gc, w, h);
            return;
        }

        // Calculate dynamic node radius
        if (graph.getNodeCount() > 1 && !fixedCoordinateMode) {
            double minSqDist = Double.POSITIVE_INFINITY;
            GraphNode[] nodes = graph.getNodes().toArray(new GraphNode[0]);
            int n = nodes.length;
            for (int i = 0; i < n; i++) {
                GraphNode n1 = nodes[i];
                for (int j = i + 1; j < n; j++) {
                    GraphNode n2 = nodes[j];
                    double dx = n1.getX() - n2.getX();
                    double dy = n1.getY() - n2.getY();
                    double sqDist = dx * dx + dy * dy;
                    if (sqDist < minSqDist) {
                        minSqDist = sqDist;
                    }
                }
            }
            double minDist = Math.sqrt(minSqDist);
            nodeRadius = Math.max(8, Math.min(22, minDist * 0.4));
        } else {
            nodeRadius = 22;
        }

        gc.save();
        gc.translate(panX, panY);
        gc.scale(zoom, zoom);

        // Edges
        for (GraphEdge edge : graph.getEdges()) {
            drawEdge(gc, edge);
        }

        // Nodes
        for (GraphNode node : graph.getNodes()) {
            drawNode(gc, node);
        }

        // Edge-in-progress
        if (edgeStartNode != null) {
            gc.setStroke(Color.web("#90CAF9"));
            gc.setLineWidth(2);
            gc.setLineDashes(8, 4);
            double sx = edgeStartNode.getX();
            double sy = edgeStartNode.getY();
            gc.strokeOval(sx - nodeRadius - 4, sy - nodeRadius - 4,
                (nodeRadius + 4) * 2, (nodeRadius + 4) * 2);
            gc.setLineDashes();
        }

        gc.restore();
    }

    private void drawGrid(GraphicsContext gc, double w, double h) {
        gc.setStroke(Color.web("#ECEFF1"));
        gc.setLineWidth(0.5);
        double gridSize = 40 * zoom;
        if (gridSize < 10) return;

        double offX = panX % gridSize;
        double offY = panY % gridSize;
        for (double x = offX; x < w; x += gridSize) gc.strokeLine(x, 0, x, h);
        for (double y = offY; y < h; y += gridSize) gc.strokeLine(0, y, w, y);
    }

    private void drawPlaceholder(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.web("#BDBDBD"));
        gc.setFont(Font.font("Segoe UI", 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Masukkan graf di panel kanan untuk memulai visualisasi", w / 2, h / 2 - 10);
        gc.setFont(Font.font("Segoe UI", 12));
        gc.setFill(Color.web("#E0E0E0"));
        gc.fillText("Atau gunakan mode '+ Node' dan '+ Edge' untuk membuat graf secara interaktif", w / 2, h / 2 + 15);
    }

    private void drawEdge(GraphicsContext gc, GraphEdge edge) {
        GraphNode src = graph.getNode(edge.getSource());
        GraphNode tgt = graph.getNode(edge.getTarget());
        if (src == null || tgt == null) return;

        EdgeState state = edge.getState();
        gc.setStroke(state.getColor());
        gc.setLineWidth(state.getWidth());
        gc.setLineDashes();

        gc.strokeLine(src.getX(), src.getY(), tgt.getX(), tgt.getY());

        // Arrowhead untuk directed graph
        if (graph.isDirected()) {
            drawArrow(gc, src.getX(), src.getY(), tgt.getX(), tgt.getY(), state.getColor());
        }

        // Weight label (tampilkan semua weight jika graf weighted dan opsi aktif)
        if (graph.isWeighted() && showEdgeWeights) {
            double mx = (src.getX() + tgt.getX()) / 2;
            double my = (src.getY() + tgt.getY()) / 2 - 10;
            // Background pill
            gc.setFill(Color.web("#FFFFFF", 0.85));
            String wText = formatNumber(edge.getWeight());
            double tw = wText.length() * 7 + 8;
            gc.fillRoundRect(mx - tw / 2, my - 8, tw, 16, 8, 8);
            gc.setStroke(Color.web("#D32F2F", 0.5));
            gc.setLineWidth(1);
            gc.strokeRoundRect(mx - tw / 2, my - 8, tw, 16, 8, 8);
            gc.setFill(Color.web("#D32F2F"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(wText, mx, my);
        }
    }

    private void drawArrow(GraphicsContext gc, double x1, double y1,
                           double x2, double y2, Color color) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double len = 12;
        double spread = Math.toRadians(25);

        double dist = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        double ratio = Math.max(0, (dist - nodeRadius - 2) / dist);
        double px = x1 + (x2 - x1) * ratio;
        double py = y1 + (y2 - y1) * ratio;

        double ax1 = px - len * Math.cos(angle - spread);
        double ay1 = py - len * Math.sin(angle - spread);
        double ax2 = px - len * Math.cos(angle + spread);
        double ay2 = py - len * Math.sin(angle + spread);

        gc.setFill(color);
        gc.fillPolygon(new double[]{px, ax1, ax2}, new double[]{py, ay1, ay2}, 3);
    }

    private void drawNode(GraphicsContext gc, GraphNode node) {
        double x = node.getX();
        double y = node.getY();
        double r = nodeRadius;
        NodeState state = node.getState();

        // Drop shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.10));
        gc.fillOval(x - r + 2, y - r + 3, r * 2, r * 2);

        // Fill
        Color fill = state.getFill();
        Color lighter = fill.interpolate(Color.WHITE, 0.3);
        RadialGradient gradient = new RadialGradient(
            0, 0, x - r * 0.2, y - r * 0.2, r * 1.2, false,
            CycleMethod.NO_CYCLE,
            new Stop(0, lighter),
            new Stop(1, fill)
        );
        gc.setFill(gradient);
        gc.fillOval(x - r, y - r, r * 2, r * 2);

        // Border
        gc.setStroke(state.getStroke());
        gc.setLineWidth(2.5);
        gc.strokeOval(x - r, y - r, r * 2, r * 2);

        // Label
        gc.setFill(Color.web("#212121"));
        Font currentFont = NODE_FONT;
        if (nodeRadius < 15) {
            currentFont = Font.font("Segoe UI", FontWeight.BOLD, Math.max(6, nodeRadius * 0.6));
        }
        gc.setFont(currentFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(node.getLabel(), x, y + 1);

        if (fixedCoordinateMode && node.hasCoordinate()) {
            gc.setFill(Color.web("#616161"));
            gc.setFont(Font.font("Consolas", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.TOP);
            gc.fillText(
                "(" + formatNumber(node.getCoordinateX()) + ", " + formatNumber(node.getCoordinateY()) + ")",
                x,
                y + r + 5
            );
        }
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-9) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.2f", value);
    }

    public boolean isShowEdgeWeights() {
        return showEdgeWeights;
    }

    public void setShowEdgeWeights(boolean showEdgeWeights) {
        this.showEdgeWeights = showEdgeWeights;
        draw();
    }
}
