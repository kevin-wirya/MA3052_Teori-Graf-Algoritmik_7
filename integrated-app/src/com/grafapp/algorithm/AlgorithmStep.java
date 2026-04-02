package com.grafapp.algorithm;

import java.util.*;

/**
 * Satu langkah dalam eksekusi algoritma.
 * Merepresentasikan perubahan state visual (node/edge) yang akan dianimasikan.
 * Menggunakan factory method pattern untuk type-safe construction.
 */
public class AlgorithmStep {

    public enum Action {
        VISIT_NODE,
        PROCESS_NODE,
        FINISH_NODE,
        TRAVERSE_EDGE,
        FINISH_EDGE,
        MARK_PATH_NODE,
        MARK_PATH_EDGE,
        MARK_TREE_EDGE,
        MARK_BRIDGE,
        MARK_ARTICULATION,
        MARK_COMPONENT,
        MARK_START,
        MARK_END,
        LOG
    }

    private final Action action;
    private int nodeId = -1;
    private int edgeSource = -1;
    private int edgeTarget = -1;
    private int componentId = -1;
    private List<Integer> nodeGroup;
    private String message;

    private AlgorithmStep(Action action) {
        this.action = action;
    }

    // --- Factory Methods ---

    public static AlgorithmStep visitNode(int nodeId, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.VISIT_NODE);
        s.nodeId = nodeId;
        s.message = message;
        return s;
    }

    public static AlgorithmStep processNode(int nodeId, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.PROCESS_NODE);
        s.nodeId = nodeId;
        s.message = message;
        return s;
    }

    public static AlgorithmStep finishNode(int nodeId, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.FINISH_NODE);
        s.nodeId = nodeId;
        s.message = message;
        return s;
    }

    public static AlgorithmStep traverseEdge(int source, int target, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.TRAVERSE_EDGE);
        s.edgeSource = source;
        s.edgeTarget = target;
        s.message = message;
        return s;
    }

    public static AlgorithmStep finishEdge(int source, int target, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.FINISH_EDGE);
        s.edgeSource = source;
        s.edgeTarget = target;
        s.message = message;
        return s;
    }

    public static AlgorithmStep markPathNode(int nodeId, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.MARK_PATH_NODE);
        s.nodeId = nodeId;
        s.message = message;
        return s;
    }

    public static AlgorithmStep markPathEdge(int source, int target, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.MARK_PATH_EDGE);
        s.edgeSource = source;
        s.edgeTarget = target;
        s.message = message;
        return s;
    }

    public static AlgorithmStep markTreeEdge(int source, int target, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.MARK_TREE_EDGE);
        s.edgeSource = source;
        s.edgeTarget = target;
        s.message = message;
        return s;
    }

    public static AlgorithmStep markBridge(int source, int target, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.MARK_BRIDGE);
        s.edgeSource = source;
        s.edgeTarget = target;
        s.message = message;
        return s;
    }

    public static AlgorithmStep markArticulation(int nodeId, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.MARK_ARTICULATION);
        s.nodeId = nodeId;
        s.message = message;
        return s;
    }

    public static AlgorithmStep markComponent(int componentId, List<Integer> nodeIds, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.MARK_COMPONENT);
        s.componentId = componentId;
        s.nodeGroup = new ArrayList<>(nodeIds);
        s.message = message;
        return s;
    }

    public static AlgorithmStep markStart(int nodeId, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.MARK_START);
        s.nodeId = nodeId;
        s.message = message;
        return s;
    }

    public static AlgorithmStep markEnd(int nodeId, String message) {
        AlgorithmStep s = new AlgorithmStep(Action.MARK_END);
        s.nodeId = nodeId;
        s.message = message;
        return s;
    }

    public static AlgorithmStep log(String message) {
        AlgorithmStep s = new AlgorithmStep(Action.LOG);
        s.message = message;
        return s;
    }

    // --- Getters ---

    public Action getAction() { return action; }
    public int getNodeId() { return nodeId; }
    public int getEdgeSource() { return edgeSource; }
    public int getEdgeTarget() { return edgeTarget; }
    public int getComponentId() { return componentId; }
    public List<Integer> getNodeGroup() { return nodeGroup; }
    public String getMessage() { return message; }
}
