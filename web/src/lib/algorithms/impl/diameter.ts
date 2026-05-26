import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const diameterAlgorithm: GraphAlgorithm = {
  name: "Graph Diameter",
  category: "Properties",
  description:
    "Menghitung diameter graf (jarak terpanjang antar semua pasangan shortest path).",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps: any[] = [];
    const sortedIds = [...graph.getNodeIds()].sort((a, b) => a - b);
    if (!sortedIds.length) {
      steps.push(Step.log("Graf kosong."));
      return { steps, summary: "Graf kosong, diameter tidak terdefinisi.", data: {} };
    }

    const reachable = new Set<number>();
    const queue: number[] = [sortedIds[0]];
    reachable.add(sortedIds[0]);
    while (queue.length) {
      const cur = queue.shift() as number;
      for (const nb of graph.getNeighbors(cur)) {
        if (!reachable.has(nb)) {
          reachable.add(nb);
          queue.push(nb);
        }
      }
    }

    if (reachable.size !== sortedIds.length) {
      steps.push(Step.log("Graf tidak terhubung."));
      return { steps, summary: "Graf tidak terhubung. Diameter tidak terdefinisi.", data: {} };
    }

    let diameter = 0;
    let diameterU = -1;
    let diameterV = -1;
    const eccentricities: Record<number, number> = {};

    for (const source of sortedIds) {
      steps.push(Step.visitNode(source, `Mulai BFS dari node ${source} untuk eccentricity.`));
      const dist = new Map<number, number>();
      const q: number[] = [source];
      dist.set(source, 0);
      let farthestNode = source;
      let maxDist = 0;

      while (q.length) {
        const current = q.shift() as number;
        for (const neighbor of graph.getNeighbors(current)) {
          if (!dist.has(neighbor)) {
            const d = (dist.get(current) ?? 0) + 1;
            dist.set(neighbor, d);
            q.push(neighbor);
            steps.push(Step.traverseEdge(current, neighbor, `Edge ${current} -> ${neighbor}, jarak ${d}`));
            if (d > maxDist) {
              maxDist = d;
              farthestNode = neighbor;
            }
          }
        }
      }

      eccentricities[source] = maxDist;
      steps.push(Step.finishNode(source, `Eccentricity(${source}) = ${maxDist}`));
      if (maxDist > diameter) {
        diameter = maxDist;
        diameterU = source;
        diameterV = farthestNode;
      }
    }

    if (diameterU !== -1 && diameterV !== -1) {
      const path = bfsPath(graph, diameterU, diameterV);
      if (path) {
        steps.push(Step.log(`Diameter path: ${JSON.stringify(path)}`));
        path.forEach((node) => steps.push(Step.markPathNode(node, "Node pada diameter path")));
        for (let i = 0; i < path.length - 1; i++) {
          steps.push(Step.markPathEdge(path[i], path[i + 1], "Edge pada diameter path"));
        }
      }
    }

    const summary = `Diameter graf = ${diameter} (antara node ${diameterU} dan ${diameterV}).`;
    return {
      steps,
      summary,
      data: { diameter, endpointU: diameterU, endpointV: diameterV, eccentricities }
    };
  }
};

const bfsPath = (graph: Graph, source: number, target: number) => {
  const parent = new Map<number, number>();
  const queue: number[] = [source];
  parent.set(source, -1);
  while (queue.length) {
    const cur = queue.shift() as number;
    if (cur === target) break;
    for (const nb of graph.getNeighbors(cur)) {
      if (!parent.has(nb)) {
        parent.set(nb, cur);
        queue.push(nb);
      }
    }
  }
  if (!parent.has(target)) return null;
  const path: number[] = [];
  for (let v = target; v !== -1; v = parent.get(v) ?? -1) path.push(v);
  return path.reverse();
};
