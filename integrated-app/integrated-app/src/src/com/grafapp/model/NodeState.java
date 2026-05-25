package com.grafapp.model;

import javafx.scene.paint.Color;

/**
 * Status visual node selama eksekusi algoritma.
 * Setiap state memiliki warna fill dan stroke yang berbeda.
 */
public enum NodeState {
    UNVISITED(Color.WHITE, Color.web("#9E9E9E")),
    PROCESSING(Color.web("#FFF9C4"), Color.web("#F9A825")),
    VISITED(Color.web("#BBDEFB"), Color.web("#1976D2")),
    PATH(Color.web("#C8E6C9"), Color.web("#388E3C")),
    START(Color.web("#90CAF9"), Color.web("#0D47A1")),
    END(Color.web("#FFCDD2"), Color.web("#C62828")),
    ARTICULATION(Color.web("#FFE0B2"), Color.web("#E65100")),
    COMPONENT_1(Color.web("#E3F2FD"), Color.web("#1565C0")),
    COMPONENT_2(Color.web("#FFF3E0"), Color.web("#E65100")),
    COMPONENT_3(Color.web("#E8F5E9"), Color.web("#2E7D32")),
    COMPONENT_4(Color.web("#FCE4EC"), Color.web("#AD1457")),
    COMPONENT_5(Color.web("#F3E5F5"), Color.web("#6A1B9A")),
    COMPONENT_6(Color.web("#FFF8E1"), Color.web("#F57F17")),
    COMPONENT_7(Color.web("#E0F2F1"), Color.web("#00695C"));

    private final Color fill;
    private final Color stroke;

    NodeState(Color fill, Color stroke) {
        this.fill = fill;
        this.stroke = stroke;
    }

    public Color getFill() { return fill; }
    public Color getStroke() { return stroke; }

    private static final NodeState[] COMPONENTS = {
        COMPONENT_1, COMPONENT_2, COMPONENT_3, COMPONENT_4,
        COMPONENT_5, COMPONENT_6, COMPONENT_7
    };

    public static NodeState forComponent(int id) {
        return COMPONENTS[Math.abs(id) % COMPONENTS.length];
    }
}
