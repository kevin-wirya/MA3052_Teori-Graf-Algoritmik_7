package com.grafapp.layout;

import com.grafapp.model.Graph;
import com.grafapp.model.GraphNode;
import com.grafapp.model.GraphEdge;
import javafx.animation.AnimationTimer;
import java.util.*;

/**
 * Force-Directed Graph Layout menggunakan model Fruchterman-Reingold.
 *   - Gaya tolak (repulsion) antar semua pasangan node ~ 1/d^2 (Coulomb)
 *   - Gaya tarik (attraction) pada edge ~ d (Hooke / spring)
 *   - Gravitasi ke pusat canvas agar graf tidak melayang
 *   - Damping untuk meredam osilasi
 *   - Cooling schedule agar layout konvergen
 */
public class ForceDirectedLayout {

    private static final double REPULSION    = 18000.0;
    private static final double ATTRACTION   = 0.003;
    private static final double GRAVITY      = 0.02;
    private static final double DAMPING      = 0.82;
    private static final double MIN_DISTANCE = 80.0;
    private static final double MAX_SPEED    = 50.0;

    private Graph graph;
    private double width, height;
    private AnimationTimer timer;
    private Runnable onTick;
    private boolean running;
    private double temperature = 1.0;
    private int tickCount;

    public ForceDirectedLayout(Graph graph, double width, double height) {
        this.graph = graph;
        this.width = Math.max(width, 200);
        this.height = Math.max(height, 200);
    }

    public void setOnTick(Runnable onTick) { this.onTick = onTick; }

    public void setDimensions(double width, double height) {
        this.width = Math.max(width, 200);
        this.height = Math.max(height, 200);
    }

    // Posisi awal acak
    public void randomizePositions() {
        Random rand = new Random(42);
        double margin = 80;
        for (GraphNode node : graph.getNodes()) {
            node.setX(margin + rand.nextDouble() * (width - 2 * margin));
            node.setY(margin + rand.nextDouble() * (height - 2 * margin));
            node.setVx(0);
            node.setVy(0);
        }
    }

    // Posisi awal melingkar
    public void circularLayout() {
        List<GraphNode> nodes = new ArrayList<>(graph.getNodes());
        if (nodes.isEmpty()) return;

        double cx = width / 2;
        double cy = height / 2;
        double radius = Math.min(width, height) * 0.42;

        if (nodes.size() == 1) {
            nodes.get(0).setX(cx);
            nodes.get(0).setY(cy);
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            double angle = 2 * Math.PI * i / nodes.size() - Math.PI / 2;
            nodes.get(i).setX(cx + radius * Math.cos(angle));
            nodes.get(i).setY(cy + radius * Math.sin(angle));
            nodes.get(i).setVx(0);
            nodes.get(i).setVy(0);
        }
    }

    public void start() {
        if (running) return;
        running = true;
        temperature = 1.0;
        tickCount = 0;

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick();
                if (onTick != null) onTick.run();
                tickCount++;
                if (tickCount > 300) {
                    temperature = Math.max(0.01, temperature * 0.997);
                }
            }
        };
        timer.start();
    }

    public void stop() {
        running = false;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public boolean isRunning() { return running; }

    public void tick() {
        List<GraphNode> nodes = new ArrayList<>(graph.getNodes());
        if (nodes.size() <= 1) return;

        // Reset forces
        for (GraphNode node : nodes) {
            node.resetForce();
        }

        // 1. Repulsive force (semua pasangan)
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                applyRepulsion(nodes.get(i), nodes.get(j));
            }
        }

        // 2. Attractive force (edges)
        for (GraphEdge edge : graph.getEdges()) {
            GraphNode a = graph.getNode(edge.getSource());
            GraphNode b = graph.getNode(edge.getTarget());
            if (a != null && b != null) {
                applyAttraction(a, b);
            }
        }

        // 3. Gravity ke pusat
        double cx = width / 2;
        double cy = height / 2;
        for (GraphNode node : nodes) {
            double dx = cx - node.getX();
            double dy = cy - node.getY();
            node.setFx(node.getFx() + dx * GRAVITY);
            node.setFy(node.getFy() + dy * GRAVITY);
        }

        // 4. Update velocity & position
        double margin = 40;
        for (GraphNode node : nodes) {
            if (node.isPinned()) continue;

            double vx = (node.getVx() + node.getFx()) * DAMPING * temperature;
            double vy = (node.getVy() + node.getFy()) * DAMPING * temperature;

            // Clamp speed
            double speed = Math.sqrt(vx * vx + vy * vy);
            if (speed > MAX_SPEED) {
                vx = (vx / speed) * MAX_SPEED;
                vy = (vy / speed) * MAX_SPEED;
            }

            node.setVx(vx);
            node.setVy(vy);
            node.setX(node.getX() + vx);
            node.setY(node.getY() + vy);

            // Keep within bounds
            node.setX(Math.max(margin, Math.min(width - margin, node.getX())));
            node.setY(Math.max(margin, Math.min(height - margin, node.getY())));
        }
    }

    private void applyRepulsion(GraphNode a, GraphNode b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < MIN_DISTANCE) {
            dist = MIN_DISTANCE;
            dx += (Math.random() - 0.5) * 2;
            dy += (Math.random() - 0.5) * 2;
        }

        double force = REPULSION / (dist * dist);
        double fx = (dx / dist) * force;
        double fy = (dy / dist) * force;

        a.setFx(a.getFx() + fx);
        a.setFy(a.getFy() + fy);
        b.setFx(b.getFx() - fx);
        b.setFy(b.getFy() - fy);
    }

    private void applyAttraction(GraphNode a, GraphNode b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1) dist = 1;

        double force = dist * ATTRACTION;
        double fx = (dx / dist) * force;
        double fy = (dy / dist) * force;

        a.setFx(a.getFx() + fx);
        a.setFy(a.getFy() + fy);
        b.setFx(b.getFx() - fx);
        b.setFy(b.getFy() - fy);
    }
}
