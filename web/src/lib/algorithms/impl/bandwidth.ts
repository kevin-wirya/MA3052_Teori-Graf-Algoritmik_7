import { Graph, GraphEdge } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const bandwidthOptimizationAlgorithm: GraphAlgorithm = {
  name: "Graph Bandwidth (Heuristic)",
  category: "Optimization",
  description:
    "Mengoptimasi penomoran node agar bandwidth graf mengecil menggunakan Cuthill-McKee dan local swap.",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps = [];
    const data: Record<string, unknown> = {};

    if (graph.nodeCount === 0) {
      return { steps, summary: "Graf kosong, bandwidth tidak dapat dihitung.", data };
    }

    const baseOrder = [...graph.getNodeIds()];
    const adj = buildAdjacency(graph);
    const cmOrder = cuthillMcKeeOrder(adj, baseOrder);
    const rcmOrder = [...cmOrder].reverse();

    const baseline = computeBandwidth(baseOrder, graph);
    const maxPasses = Math.max(1, Math.min(30, baseOrder.length));

    let best = evaluateCandidate("Original", baseOrder, graph, maxPasses);
    const cmBest = evaluateCandidate("Cuthill-McKee", cmOrder, graph, maxPasses);
    const rcmBest = evaluateCandidate("Reverse Cuthill-McKee", rcmOrder, graph, maxPasses);

    if (cmBest.bandwidth < best.bandwidth) best = cmBest;
    if (rcmBest.bandwidth < best.bandwidth) best = rcmBest;

    data.bandwidthOrderBefore = baseOrder;
    data.bandwidthOrderAfter = best.order;
    data.bandwidthBefore = baseline;
    data.bandwidthAfter = best.bandwidth;
    data.bandwidthMethod = best.method;

    const summary = `Bandwidth sebelum: ${baseline}, sesudah: ${best.bandwidth} (${best.method}).`;
    steps.push(Step.log(summary));
    steps.push(Step.log(`Order awal: ${formatOrder(baseOrder)}`));
    steps.push(Step.log(`Order hasil: ${formatOrder(best.order)}`));

    return { steps, summary, data };
  }
};

const buildAdjacency = (graph: Graph) => {
  const adj = new Map<number, number[]>();
  for (const id of graph.getNodeIds()) adj.set(id, []);
  for (const edge of graph.getEdges()) {
    adj.get(edge.source)?.push(edge.target);
    adj.get(edge.target)?.push(edge.source);
  }
  return adj;
};

const cuthillMcKeeOrder = (adj: Map<number, number[]>, nodeIds: number[]) => {
  const degree = new Map<number, number>();
  nodeIds.forEach((id) => degree.set(id, adj.get(id)?.length ?? 0));
  const sorted = [...nodeIds].sort((a, b) => compareByDegree(a, b, degree));
  const visited = new Set<number>();
  const order: number[] = [];
  const queue: number[] = [];

  for (const start of sorted) {
    if (visited.has(start)) continue;
    visited.add(start);
    queue.push(start);

    while (queue.length) {
      const u = queue.shift() as number;
      order.push(u);
      const neighbors = [...(adj.get(u) ?? [])].sort((a, b) => compareByDegree(a, b, degree));
      for (const v of neighbors) {
        if (!visited.has(v)) {
          visited.add(v);
          queue.push(v);
        }
      }
    }
  }

  return order;
};

const compareByDegree = (a: number, b: number, degree: Map<number, number>) => {
  const da = degree.get(a) ?? 0;
  const db = degree.get(b) ?? 0;
  if (da !== db) return da - db;
  return a - b;
};

const computeBandwidth = (order: number[], graph: Graph) => {
  const pos = new Map<number, number>();
  order.forEach((id, idx) => pos.set(id, idx));
  let max = 0;
  for (const edge of graph.getEdges()) {
    const p1 = pos.get(edge.source);
    const p2 = pos.get(edge.target);
    if (p1 === undefined || p2 === undefined) continue;
    max = Math.max(max, Math.abs(p1 - p2));
  }
  return max;
};

const improveByAdjacentSwaps = (order: number[], graph: Graph, maxPasses: number) => {
  const best = [...order];
  let bestBandwidth = computeBandwidth(best, graph);

  for (let pass = 0; pass < maxPasses; pass++) {
    let improved = false;
    for (let i = 0; i < best.length - 1; i++) {
      [best[i], best[i + 1]] = [best[i + 1], best[i]];
      const bw = computeBandwidth(best, graph);
      if (bw < bestBandwidth) {
        bestBandwidth = bw;
        improved = true;
      } else {
        [best[i], best[i + 1]] = [best[i + 1], best[i]];
      }
    }
    if (!improved) break;
  }

  return best;
};

const evaluateCandidate = (method: string, order: number[], graph: Graph, maxPasses: number) => {
  const improved = improveByAdjacentSwaps(order, graph, maxPasses);
  const bw = computeBandwidth(improved, graph);
  return { method: `${method} + local swap`, order: improved, bandwidth: bw };
};

const formatOrder = (order: number[]) => `[${order.join(", ")}]`;
