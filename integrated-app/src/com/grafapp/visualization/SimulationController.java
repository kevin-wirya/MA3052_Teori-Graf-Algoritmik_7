package com.grafapp.visualization;

import com.grafapp.algorithm.AlgorithmResult;
import com.grafapp.algorithm.AlgorithmStep;
import com.grafapp.model.*;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import java.util.List;

public class SimulationController {

    private final GraphCanvas canvas;
    private Graph graph;
    private List<AlgorithmStep> steps;
    private int currentIndex = -1;

    private AnimationTimer animTimer;
    private long lastStepNanos;

    private final DoubleProperty speed = new SimpleDoubleProperty(1.0);
    private final BooleanProperty playing = new SimpleBooleanProperty(false);
    private final IntegerProperty currentStepProp = new SimpleIntegerProperty(-1);
    private final IntegerProperty totalStepsProp = new SimpleIntegerProperty(0);
    private final StringProperty currentMessage = new SimpleStringProperty("");

    private Runnable onStepChanged;

    public SimulationController(GraphCanvas canvas) {
        this.canvas = canvas;
    }

    public void setOnStepChanged(Runnable cb) { this.onStepChanged = cb; }

    // Load hasil algoritma ke dalam animasi
    public void loadResult(AlgorithmResult result) {
        stop();
        this.graph = canvas.getGraph();
        this.steps = result.getSteps();
        this.currentIndex = -1;
        totalStepsProp.set(steps.size());
        currentStepProp.set(-1);
        currentMessage.set("");
        graph.resetStates();
        canvas.draw();
    }

    // Mulai autoplay animasi
    public void play() {
        if (steps == null || steps.isEmpty()) return;
        if (currentIndex >= steps.size() - 1) return;

        playing.set(true);
        lastStepNanos = System.nanoTime();

        if (animTimer != null) animTimer.stop();
        animTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double intervalMs = 500.0 / speed.get();
                long intervalNanos = (long) (intervalMs * 1_000_000);
                if (now - lastStepNanos >= intervalNanos) {
                    if (currentIndex < steps.size() - 1) {
                        stepForward();
                        lastStepNanos = now;
                    } else {
                        pause();
                    }
                }
            }
        };
        animTimer.start();
    }

    // Pause animasi
    public void pause() {
        if (animTimer != null) {
            animTimer.stop();
            animTimer = null;
        }
        playing.set(false);
    }

    // Stop dan reset animasi ke awal
    public void stop() {
        pause();
        currentIndex = -1;
        currentStepProp.set(-1);
        currentMessage.set("");
        if (graph != null) graph.resetStates();
        canvas.draw();
    }

    // Maju satu langkah
    public void stepForward() {
        if (steps == null || currentIndex >= steps.size() - 1) return;
        currentIndex++;
        applyStep(steps.get(currentIndex));
        currentStepProp.set(currentIndex);
        canvas.draw();
        if (onStepChanged != null) onStepChanged.run();
    }

    // Mundur satu langkah
    public void stepBackward() {
        if (steps == null || currentIndex < 0) return;
        graph.resetStates();
        currentIndex--;
        for (int i = 0; i <= currentIndex; i++) {
            applyStep(steps.get(i));
        }
        currentStepProp.set(currentIndex);
        canvas.draw();
        if (onStepChanged != null) onStepChanged.run();
    }

    // Langsung ke step tertentu
    public void goToStep(int index) {
        if (steps == null) return;
        index = Math.max(-1, Math.min(index, steps.size() - 1));
        graph.resetStates();
        for (int i = 0; i <= index; i++) {
            applyStep(steps.get(i));
        }
        currentIndex = index;
        currentStepProp.set(currentIndex);
        canvas.draw();
        if (onStepChanged != null) onStepChanged.run();
    }

    private void applyStep(AlgorithmStep step) {
        if (step.getMessage() != null && !step.getMessage().isEmpty()) {
            currentMessage.set(step.getMessage());
        }
        switch (step.getAction()) {
            case VISIT_NODE:
            case PROCESS_NODE:
                setNodeState(step.getNodeId(), NodeState.PROCESSING);
                break;
            case FINISH_NODE:
                setNodeState(step.getNodeId(), NodeState.VISITED);
                break;
            case TRAVERSE_EDGE:
                setEdgeState(step.getEdgeSource(), step.getEdgeTarget(), EdgeState.TRAVERSING);
                break;
            case FINISH_EDGE:
                setEdgeState(step.getEdgeSource(), step.getEdgeTarget(), EdgeState.VISITED);
                break;
            case MARK_PATH_NODE:
                setNodeState(step.getNodeId(), NodeState.PATH);
                break;
            case MARK_PATH_EDGE:
                setEdgeState(step.getEdgeSource(), step.getEdgeTarget(), EdgeState.PATH);
                break;
            case MARK_BRIDGE:
                setEdgeState(step.getEdgeSource(), step.getEdgeTarget(), EdgeState.BRIDGE);
                break;
            case MARK_ARTICULATION:
                setNodeState(step.getNodeId(), NodeState.ARTICULATION);
                break;
            case MARK_COMPONENT:
                if (step.getNodeGroup() != null) {
                    NodeState cs = NodeState.forComponent(step.getComponentId());
                    for (int id : step.getNodeGroup()) setNodeState(id, cs);
                }
                break;
            case MARK_START:
                setNodeState(step.getNodeId(), NodeState.START);
                break;
            case MARK_END:
                setNodeState(step.getNodeId(), NodeState.END);
                break;
            case LOG:
                break;
        }
    }

    private void setNodeState(int id, NodeState state) {
        if (graph != null) {
            GraphNode n = graph.getNode(id);
            if (n != null) n.setState(state);
        }
    }

    private void setEdgeState(int src, int tgt, EdgeState state) {
        if (graph != null) {
            GraphEdge e = graph.getEdge(src, tgt);
            if (e != null) e.setState(state);
        }
    }

    public DoubleProperty speedProperty() { return speed; }
    public BooleanProperty playingProperty() { return playing; }
    public IntegerProperty currentStepProperty() { return currentStepProp; }
    public IntegerProperty totalStepsProperty() { return totalStepsProp; }
    public StringProperty currentMessageProperty() { return currentMessage; }
    public boolean isPlaying() { return playing.get(); }
}
