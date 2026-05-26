package com.grafapp.model;

import javafx.scene.paint.Color;

/**
 * Status visual edge selama eksekusi algoritma.
 */
public enum EdgeState {
    DEFAULT(Color.web("#BDBDBD"), 1.5),
    TRAVERSING(Color.web("#FFC107"), 3.0),
    VISITED(Color.web("#64B5F6"), 2.5),
    BRIDGE(Color.web("#F44336"), 3.5),
    PATH(Color.web("#4CAF50"), 3.0),
    TREE_EDGE(Color.web("#00897B"), 3.5);

    private final Color color;
    private final double width;

    EdgeState(Color color, double width) {
        this.color = color;
        this.width = width;
    }

    public Color getColor() { return color; }
    public double getWidth() { return width; }
}
