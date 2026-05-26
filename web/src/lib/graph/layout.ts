import { Graph, GraphEdge, GraphNode } from "@/lib/graph/graph";

export class ForceDirectedLayout {
  private static readonly REPULSION = 18000;
  private static readonly ATTRACTION = 0.003;
  private static readonly GRAVITY = 0.02;
  private static readonly DAMPING = 0.82;
  private static readonly MIN_DISTANCE = 80;
  private static readonly MAX_SPEED = 50;

  private graph: Graph;
  private width = 600;
  private height = 400;
  private running = false;
  private temperature = 1;
  private tickCount = 0;
  private rafId: number | null = null;

  onTick?: () => void;

  constructor(graph: Graph, width = 600, height = 400) {
    this.graph = graph;
    this.setDimensions({ width, height });
  }

  getGraph() {
    return this.graph;
  }

  setDimensions({ width, height }: { width: number; height: number }) {
    this.width = Math.max(width, 200);
    this.height = Math.max(height, 200);
  }

  randomizePositions() {
    const rand = mulberry32(42);
    const margin = 80;
    for (const node of this.graph.getNodes()) {
      node.x = margin + rand() * (this.width - 2 * margin);
      node.y = margin + rand() * (this.height - 2 * margin);
      node.vx = 0;
      node.vy = 0;
    }
  }

  circularLayout() {
    const nodes = this.graph.getNodes();
    if (nodes.length === 0) return;

    const cx = this.width / 2;
    const cy = this.height / 2;
    const radius = Math.min(this.width, this.height) * 0.42;

    if (nodes.length === 1) {
      nodes[0].x = cx;
      nodes[0].y = cy;
      return;
    }

    for (let i = 0; i < nodes.length; i++) {
      const angle = (2 * Math.PI * i) / nodes.length - Math.PI / 2;
      nodes[i].x = cx + radius * Math.cos(angle);
      nodes[i].y = cy + radius * Math.sin(angle);
      nodes[i].vx = 0;
      nodes[i].vy = 0;
    }
  }

  start() {
    if (this.running) return;
    this.running = true;
    this.temperature = 1;
    this.tickCount = 0;

    const tick = () => {
      this.tick();
      this.onTick?.();
      this.tickCount += 1;
      if (this.tickCount > 300) {
        this.temperature = Math.max(0.01, this.temperature * 0.997);
      }
      if (this.running) {
        this.rafId = requestAnimationFrame(tick);
      }
    };

    this.rafId = requestAnimationFrame(tick);
  }

  stop() {
    this.running = false;
    if (this.rafId !== null) {
      cancelAnimationFrame(this.rafId);
      this.rafId = null;
    }
  }

  isRunning() {
    return this.running;
  }

  tick() {
    const nodes = this.graph.getNodes();
    if (nodes.length <= 1) return;

    for (const node of nodes) node.resetForce();

    for (let i = 0; i < nodes.length; i++) {
      for (let j = i + 1; j < nodes.length; j++) {
        this.applyRepulsion(nodes[i], nodes[j]);
      }
    }

    for (const edge of this.graph.getEdges()) {
      const a = this.graph.getNode(edge.source);
      const b = this.graph.getNode(edge.target);
      if (a && b) this.applyAttraction(a, b);
    }

    const cx = this.width / 2;
    const cy = this.height / 2;
    for (const node of nodes) {
      const dx = cx - node.x;
      const dy = cy - node.y;
      node.fx += dx * ForceDirectedLayout.GRAVITY;
      node.fy += dy * ForceDirectedLayout.GRAVITY;
    }

    const margin = 40;
    for (const node of nodes) {
      if (node.pinned) continue;
      let vx = (node.vx + node.fx) * ForceDirectedLayout.DAMPING * this.temperature;
      let vy = (node.vy + node.fy) * ForceDirectedLayout.DAMPING * this.temperature;

      const speed = Math.hypot(vx, vy);
      if (speed > ForceDirectedLayout.MAX_SPEED) {
        vx = (vx / speed) * ForceDirectedLayout.MAX_SPEED;
        vy = (vy / speed) * ForceDirectedLayout.MAX_SPEED;
      }

      node.vx = vx;
      node.vy = vy;
      node.x += vx;
      node.y += vy;

      node.x = Math.max(margin, Math.min(this.width - margin, node.x));
      node.y = Math.max(margin, Math.min(this.height - margin, node.y));
    }
  }

  private applyRepulsion(a: GraphNode, b: GraphNode) {
    let dx = a.x - b.x;
    let dy = a.y - b.y;
    let dist = Math.hypot(dx, dy);

    if (dist < ForceDirectedLayout.MIN_DISTANCE) {
      dist = ForceDirectedLayout.MIN_DISTANCE;
      dx += (Math.random() - 0.5) * 2;
      dy += (Math.random() - 0.5) * 2;
    }

    const force = ForceDirectedLayout.REPULSION / (dist * dist);
    const fx = (dx / dist) * force;
    const fy = (dy / dist) * force;

    a.fx += fx;
    a.fy += fy;
    b.fx -= fx;
    b.fy -= fy;
  }

  private applyAttraction(a: GraphNode, b: GraphNode) {
    const dx = b.x - a.x;
    const dy = b.y - a.y;
    let dist = Math.hypot(dx, dy);
    if (dist < 1) dist = 1;

    const force = dist * ForceDirectedLayout.ATTRACTION;
    const fx = (dx / dist) * force;
    const fy = (dy / dist) * force;

    a.fx += fx;
    a.fy += fy;
    b.fx -= fx;
    b.fy -= fy;
  }
}

function mulberry32(seed: number) {
  let t = seed;
  return () => {
    t += 0x6d2b79f5;
    let r = Math.imul(t ^ (t >>> 15), 1 | t);
    r ^= r + Math.imul(r ^ (r >>> 7), 61 | r);
    return ((r ^ (r >>> 14)) >>> 0) / 4294967296;
  };
}
