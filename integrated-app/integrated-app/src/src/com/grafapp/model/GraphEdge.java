package com.grafapp.model;

/**
 * Edge dalam graf dengan state visual dan weight opsional.
 */
public class GraphEdge {
    private final int source;
    private final int target;
    private double weight;
    private EdgeState state = EdgeState.DEFAULT;

    public GraphEdge(int source, int target) {
        this.source = source;
        this.target = target;
        this.weight = 1.0;
    }

    public GraphEdge(int source, int target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public int getSource() { return source; }
    public int getTarget() { return target; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public EdgeState getState() { return state; }
    public void setState(EdgeState state) { this.state = state; }

    public boolean connects(int a, int b) {
        return (source == a && target == b) || (source == b && target == a);
    }
}
