package com.grafapp.model;

/**
 * Node dalam graf dengan properti fisika untuk force-directed layout.
 * Menyimpan posisi (x,y), kecepatan (vx,vy), gaya (fx,fy), dan state visual.
 */
public class GraphNode {
    private final int id;
    private String label;
    private double x, y;
    private double vx, vy;
    private double fx, fy;
    private NodeState state = NodeState.UNVISITED;
    private boolean pinned;

    public GraphNode(int id) {
        this.id = id;
        this.label = String.valueOf(id);
    }

    public GraphNode(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getVx() { return vx; }
    public void setVx(double vx) { this.vx = vx; }
    public double getVy() { return vy; }
    public void setVy(double vy) { this.vy = vy; }

    public double getFx() { return fx; }
    public void setFx(double fx) { this.fx = fx; }
    public double getFy() { return fy; }
    public void setFy(double fy) { this.fy = fy; }

    public NodeState getState() { return state; }
    public void setState(NodeState state) { this.state = state; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public void resetForce() { fx = 0; fy = 0; }
}
