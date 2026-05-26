import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

const WHITE = 0;
const GRAY = 1;
const BLACK = 2;

export const cycleDetectionAlgorithm: GraphAlgorithm = {
  name: "Cycle Detection",
  category: "Properties",
  description:
    "Mendeteksi semua siklus dalam graf dan menampilkan siklus yang ditemukan.",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps: any[] = [];
    return graph.directed ? detectDirected(graph, steps) : detectUndirected(graph, steps);
  }
};

const detectUndirected = (graph: Graph, steps: any[]): AlgorithmResult => {
  const color = new Map<number, number>();
  const parent = new Map<number, number>();
  const allCycles: number[][] = [];
  const sortedIds = [...graph.getNodeIds()].sort((a, b) => a - b);
  sortedIds.forEach((id) => color.set(id, WHITE));

  for (const start of sortedIds) {
    if (color.get(start) === WHITE) {
      parent.set(start, -1);
      dfsUndirected(graph, start, color, parent, steps, allCycles);
    }
  }

  return buildResult(allCycles, steps, false);
};

const dfsUndirected = (
  graph: Graph,
  node: number,
  color: Map<number, number>,
  parent: Map<number, number>,
  steps: any[],
  allCycles: number[][]
) => {
  color.set(node, GRAY);
  steps.push(Step.visitNode(node, `Mengunjungi node ${node}`));

  for (const neighbor of graph.getNeighbors(node)) {
    if (color.get(neighbor) === WHITE) {
      parent.set(neighbor, node);
      steps.push(Step.traverseEdge(node, neighbor, `Menelusuri edge ${node} -> ${neighbor}`));
      dfsUndirected(graph, neighbor, color, parent, steps, allCycles);
    } else if (color.get(neighbor) === GRAY && neighbor !== parent.get(node)) {
      steps.push(Step.traverseEdge(node, neighbor, `Back edge: ${node} -> ${neighbor}`));
      const cycle = reconstructCycle(node, neighbor, parent);
      if (cycle && !isDuplicate(allCycles, cycle)) allCycles.push(cycle);
    }
  }

  color.set(node, BLACK);
  steps.push(Step.finishNode(node, `Selesai node ${node}`));
};

const detectDirected = (graph: Graph, steps: any[]): AlgorithmResult => {
  const color = new Map<number, number>();
  const parent = new Map<number, number>();
  const allCycles: number[][] = [];
  const sortedIds = [...graph.getNodeIds()].sort((a, b) => a - b);
  sortedIds.forEach((id) => color.set(id, WHITE));

  for (const start of sortedIds) {
    if (color.get(start) === WHITE) {
      parent.set(start, -1);
      dfsDirected(graph, start, color, parent, steps, allCycles);
    }
  }

  return buildResult(allCycles, steps, true);
};

const dfsDirected = (
  graph: Graph,
  node: number,
  color: Map<number, number>,
  parent: Map<number, number>,
  steps: any[],
  allCycles: number[][]
) => {
  color.set(node, GRAY);
  steps.push(Step.visitNode(node, `Mengunjungi node ${node} (gray)`));

  for (const neighbor of graph.getNeighbors(node)) {
    if (color.get(neighbor) === WHITE) {
      parent.set(neighbor, node);
      steps.push(Step.traverseEdge(node, neighbor, `Menelusuri edge ${node} -> ${neighbor}`));
      dfsDirected(graph, neighbor, color, parent, steps, allCycles);
    } else if (color.get(neighbor) === GRAY) {
      steps.push(Step.traverseEdge(node, neighbor, `Back edge: ${node} -> ${neighbor}`));
      const cycle = reconstructCycle(node, neighbor, parent);
      if (cycle && !isDuplicate(allCycles, cycle)) allCycles.push(cycle);
    }
  }

  color.set(node, BLACK);
  steps.push(Step.finishNode(node, `Selesai node ${node}`));
};

const reconstructCycle = (fromNode: number, toAncestor: number, parent: Map<number, number>) => {
  const cycle: number[] = [toAncestor];
  let cur = fromNode;
  let limit = 1000;
  while (cur !== toAncestor && limit-- > 0) {
    cycle.push(cur);
    const p = parent.get(cur);
    if (p === undefined || p === -1) return null;
    cur = p;
  }
  if (cur !== toAncestor) return null;
  return cycle;
};

const buildResult = (allCycles: number[][], steps: any[], directed: boolean): AlgorithmResult => {
  const data: Record<string, unknown> = {};
  if (allCycles.length) {
    const first = allCycles[0];
    first.forEach((node) => steps.push(Step.markPathNode(node, "")));
    for (let i = 0; i < first.length; i++) {
      const u = first[i];
      const v = first[(i + 1) % first.length];
      steps.push(Step.markPathEdge(u, v, ""));
    }
    steps.push(Step.log(`Ditemukan ${allCycles.length} siklus.`));
    data.hasCycle = true;
    data.allCycles = allCycles;
    const summary = allCycles.map((cycle, i) => `Cycle ${i + 1}: ${cycle.join(" -> ")} -> ${cycle[0]}`).join("\n");
    return { steps, summary, data };
  }

  const msg = directed
    ? "Graf bersifat DAG (Directed Acyclic Graph)."
    : "Graf bersifat acyclic, bisa jadi tree atau forest.";
  steps.push(Step.log(`Tidak ada siklus. ${msg}`));
  data.hasCycle = false;
  data.allCycles = [];
  return { steps, summary: `Tidak ada siklus. ${msg}`, data };
};

const isDuplicate = (existing: number[][], cycle: number[]) => {
  const newSet = new Set(cycle);
  return existing.some((c) => {
    const set = new Set(c);
    if (set.size !== newSet.size) return false;
    for (const val of newSet) {
      if (!set.has(val)) return false;
    }
    return true;
  });
};
