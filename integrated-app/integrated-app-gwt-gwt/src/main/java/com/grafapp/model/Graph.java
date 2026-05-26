package com.grafapp.model;

import java.util.*;

/**
 * Model graf inti menggunakan adjacency list.
 * Mendukung graf directed dan undirected, weighted dan unweighted.
 */
public class Graph {
    private final Map<Integer, GraphNode> nodes = new LinkedHashMap<>();
    private final List<GraphEdge> edges = new ArrayList<>();
    private boolean directed;
    private boolean weighted;

    public Graph() { this(false); }

    public Graph(boolean directed) { this.directed = directed; }

    public boolean isWeighted() { return weighted; }
    public void setWeighted(boolean weighted) { this.weighted = weighted; }

    public void addNode(int id) {
        if (!nodes.containsKey(id)) {
            nodes.put(id, new GraphNode(id));
        }
    }

    public void addNode(GraphNode node) {
        nodes.put(node.getId(), node);
    }

    public void addEdge(int source, int target) {
        addNode(source);
        addNode(target);
        for (GraphEdge e : edges) {
            if (e.connects(source, target)) return;
        }
        edges.add(new GraphEdge(source, target));
    }

    public void addEdge(int source, int target, double weight) {
        addNode(source);
        addNode(target);
        for (GraphEdge e : edges) {
            if (e.connects(source, target)) return;
        }
        edges.add(new GraphEdge(source, target, weight));
    }

    public void removeNode(int id) {
        nodes.remove(id);
        edges.removeIf(e -> e.getSource() == id || e.getTarget() == id);
    }

    public void removeEdge(int source, int target) {
        edges.removeIf(e -> e.connects(source, target));
    }

    public GraphNode getNode(int id) { return nodes.get(id); }
    public Collection<GraphNode> getNodes() { return nodes.values(); }
    public List<GraphEdge> getEdges() { return edges; }
    public int getNodeCount() { return nodes.size(); }
    public int getEdgeCount() { return edges.size(); }
    public boolean isDirected() { return directed; }
    public void setDirected(boolean directed) { this.directed = directed; }
    public Set<Integer> getNodeIds() { return nodes.keySet(); }

    public List<Integer> getNeighbors(int nodeId) {
        List<Integer> neighbors = new ArrayList<>();
        for (GraphEdge edge : edges) {
            if (edge.getSource() == nodeId) {
                neighbors.add(edge.getTarget());
            } else if (!directed && edge.getTarget() == nodeId) {
                neighbors.add(edge.getSource());
            }
        }
        Collections.sort(neighbors);
        return neighbors;
    }

    public GraphEdge getEdge(int source, int target) {
        for (GraphEdge e : edges) {
            if (directed) {
                if (e.getSource() == source && e.getTarget() == target) return e;
            } else {
                if (e.connects(source, target)) return e;
            }
        }
        return null;
    }

    public void resetStates() {
        for (GraphNode node : nodes.values()) {
            node.setState(NodeState.UNVISITED);
        }
        for (GraphEdge edge : edges) {
            edge.setState(EdgeState.DEFAULT);
        }
    }

    public int nextAvailableId() {
        int max = -1;
        for (int id : nodes.keySet()) {
            if (id > max) max = id;
        }
        return max + 1;
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }
}
