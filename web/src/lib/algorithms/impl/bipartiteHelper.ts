import { Graph, GraphEdge } from "@/lib/graph/graph";

export interface PartitionResult {
  bipartite: boolean;
  side: Map<number, number>;
  left: Set<number>;
  right: Set<number>;
  conflictU: number;
  conflictV: number;
}

export const buildAdjacency = (graph: Graph) => {
  const adj = new Map<number, number[]>();
  for (const id of graph.getNodeIds()) {
    adj.set(id, []);
  }
  for (const edge of graph.getEdges()) {
    if (!adj.has(edge.source)) adj.set(edge.source, []);
    if (!adj.has(edge.target)) adj.set(edge.target, []);
    adj.get(edge.source)?.push(edge.target);
    adj.get(edge.target)?.push(edge.source);
  }
  for (const neighbors of adj.values()) {
    neighbors.sort((a, b) => a - b);
  }
  return adj;
};

export const buildPartition = (adj: Map<number, number[]>): PartitionResult => {
  const side = new Map<number, number>();
  const left = new Set<number>();
  const right = new Set<number>();
  let conflictU = -1;
  let conflictV = -1;
  let bipartite = true;

  for (const start of adj.keys()) {
    if (side.has(start)) continue;
    const queue: number[] = [start];
    side.set(start, 0);
    left.add(start);

    while (queue.length && bipartite) {
      const u = queue.shift() as number;
      const uSide = side.get(u) ?? 0;
      for (const v of adj.get(u) ?? []) {
        if (!side.has(v)) {
          const vSide = 1 - uSide;
          side.set(v, vSide);
          if (vSide === 0) left.add(v);
          else right.add(v);
          queue.push(v);
        } else if (side.get(v) === uSide) {
          bipartite = false;
          conflictU = u;
          conflictV = v;
          break;
        }
      }
    }
    if (!bipartite) break;
  }

  return { bipartite, side, left, right, conflictU, conflictV };
};
