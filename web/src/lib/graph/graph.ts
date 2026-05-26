export type NodeStateKey =
  | "UNVISITED"
  | "PROCESSING"
  | "VISITED"
  | "PATH"
  | "START"
  | "END"
  | "ARTICULATION"
  | "COMPONENT_1"
  | "COMPONENT_2"
  | "COMPONENT_3"
  | "COMPONENT_4"
  | "COMPONENT_5"
  | "COMPONENT_6"
  | "COMPONENT_7";

export type EdgeStateKey =
  | "DEFAULT"
  | "TRAVERSING"
  | "VISITED"
  | "BRIDGE"
  | "PATH"
  | "TREE_EDGE";

export const nodeStateStyles: Record<NodeStateKey, { fill: string; fillLighter: string; stroke: string }> = {
  UNVISITED: { fill: "#ffffff", fillLighter: "#f7f7f7", stroke: "#9e9e9e" },
  PROCESSING: { fill: "#fff9c4", fillLighter: "#fffde7", stroke: "#f9a825" },
  VISITED: { fill: "#bbdefb", fillLighter: "#e3f2fd", stroke: "#1976d2" },
  PATH: { fill: "#c8e6c9", fillLighter: "#e8f5e9", stroke: "#388e3c" },
  START: { fill: "#90caf9", fillLighter: "#e3f2fd", stroke: "#0d47a1" },
  END: { fill: "#ffcdd2", fillLighter: "#ffebee", stroke: "#c62828" },
  ARTICULATION: { fill: "#ffe0b2", fillLighter: "#fff3e0", stroke: "#e65100" },
  COMPONENT_1: { fill: "#e3f2fd", fillLighter: "#f4f9ff", stroke: "#1565c0" },
  COMPONENT_2: { fill: "#fff3e0", fillLighter: "#fff8ef", stroke: "#e65100" },
  COMPONENT_3: { fill: "#e8f5e9", fillLighter: "#f4fbf5", stroke: "#2e7d32" },
  COMPONENT_4: { fill: "#fce4ec", fillLighter: "#fff1f6", stroke: "#ad1457" },
  COMPONENT_5: { fill: "#f3e5f5", fillLighter: "#fbf4fc", stroke: "#6a1b9a" },
  COMPONENT_6: { fill: "#fff8e1", fillLighter: "#fffdf2", stroke: "#f57f17" },
  COMPONENT_7: { fill: "#e0f2f1", fillLighter: "#f3fbfa", stroke: "#00695c" }
};

export const edgeStateStyles: Record<EdgeStateKey, { color: string; width: number }> = {
  DEFAULT: { color: "#bdbdbd", width: 1.5 },
  TRAVERSING: { color: "#ffc107", width: 3.0 },
  VISITED: { color: "#64b5f6", width: 2.5 },
  BRIDGE: { color: "#f44336", width: 3.5 },
  PATH: { color: "#4caf50", width: 3.0 },
  TREE_EDGE: { color: "#00897b", width: 3.5 }
};

const componentStates: NodeStateKey[] = [
  "COMPONENT_1",
  "COMPONENT_2",
  "COMPONENT_3",
  "COMPONENT_4",
  "COMPONENT_5",
  "COMPONENT_6",
  "COMPONENT_7"
];

export const componentStateFor = (id: number): NodeStateKey => {
  const index = Math.abs(id) % componentStates.length;
  return componentStates[index];
};

export class GraphNode {
  id: number;
  label: string;
  x = 0;
  y = 0;
  coordinateX = 0;
  coordinateY = 0;
  hasCoordinate = false;
  vx = 0;
  vy = 0;
  fx = 0;
  fy = 0;
  state: NodeStateKey = "UNVISITED";
  pinned = false;

  constructor(id: number, label?: string) {
    this.id = id;
    this.label = label ?? String(id);
  }

  resetForce() {
    this.fx = 0;
    this.fy = 0;
  }
}

export class GraphEdge {
  source: number;
  target: number;
  weight: number;
  state: EdgeStateKey = "DEFAULT";

  constructor(source: number, target: number, weight = 1) {
    this.source = source;
    this.target = target;
    this.weight = weight;
  }

  connects(a: number, b: number) {
    return (this.source === a && this.target === b) || (this.source === b && this.target === a);
  }
}

export class Graph {
  private nodes = new Map<number, GraphNode>();
  private edges: GraphEdge[] = [];
  directed = false;
  weighted = false;

  constructor(directed = false) {
    this.directed = directed;
  }

  get nodeCount() {
    return this.nodes.size;
  }

  get edgeCount() {
    return this.edges.length;
  }

  getNode(id: number) {
    return this.nodes.get(id);
  }

  getNodes() {
    return Array.from(this.nodes.values());
  }

  getEdges() {
    return this.edges;
  }

  getNodeIds() {
    return Array.from(this.nodes.keys());
  }

  addNode(idOrNode: number | GraphNode) {
    if (typeof idOrNode === "number") {
      if (!this.nodes.has(idOrNode)) {
        this.nodes.set(idOrNode, new GraphNode(idOrNode));
      }
      return;
    }
    this.nodes.set(idOrNode.id, idOrNode);
  }

  addEdge(source: number, target: number, weight?: number) {
    this.addNode(source);
    this.addNode(target);
    const hasDuplicate = this.edges.some((edge) =>
      this.directed ? edge.source === source && edge.target === target : edge.connects(source, target)
    );
    if (hasDuplicate) return;
    this.edges.push(new GraphEdge(source, target, weight ?? 1));
  }

  removeNode(id: number) {
    this.nodes.delete(id);
    this.edges = this.edges.filter((edge) => edge.source !== id && edge.target !== id);
  }

  removeEdge(source: number, target: number) {
    this.edges = this.edges.filter((edge) =>
      this.directed ? !(edge.source === source && edge.target === target) : !edge.connects(source, target)
    );
  }

  getNeighbors(nodeId: number) {
    const neighbors: number[] = [];
    for (const edge of this.edges) {
      if (edge.source === nodeId) {
        neighbors.push(edge.target);
      } else if (!this.directed && edge.target === nodeId) {
        neighbors.push(edge.source);
      }
    }
    neighbors.sort((a, b) => a - b);
    return neighbors;
  }

  getEdge(source: number, target: number) {
    for (const edge of this.edges) {
      if (this.directed) {
        if (edge.source === source && edge.target === target) return edge;
      } else if (edge.connects(source, target)) {
        return edge;
      }
    }
    return undefined;
  }

  resetStates() {
    for (const node of this.nodes.values()) {
      node.state = "UNVISITED";
    }
    for (const edge of this.edges) {
      edge.state = "DEFAULT";
    }
  }

  nextAvailableId() {
    let max = -1;
    for (const id of this.nodes.keys()) {
      if (id > max) max = id;
    }
    return max + 1;
  }

  clear() {
    this.nodes.clear();
    this.edges = [];
  }
}
