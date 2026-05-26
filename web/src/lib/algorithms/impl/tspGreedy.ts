import { Graph, GraphEdge } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";
import { formatWeight } from "@/lib/graph/utils";

const EPS = 1e-9;
const INF = Number.POSITIVE_INFINITY;

export const tspGreedyAlgorithm: GraphAlgorithm = {
  name: "Traveling Salesman (Greedy)",
  category: "Optimization",
  description:
    "Mencari pendekatan tur TSP menggunakan greedy nearest neighbor untuk semua start node.",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps = [];
    const data: Record<string, unknown> = {};

    if (graph.nodeCount === 0) {
      putFailure(data, []);
      return { steps, summary: "Graf kosong, tur TSP tidak dapat dibangun.", data };
    }

    const nodeIds = [...graph.getNodeIds()].sort((a, b) => a - b);
    if (graph.nodeCount === 1) {
      const start = nodeIds[0];
      const tour = [start, start];
      putSuccess(data, tour, 0);
      return { steps, summary: `Tur TSP trivial: ${tour.join(" -> ")}`, data };
    }

    steps.push(Step.log("Menggunakan greedy nearest neighbor untuk semua start node."));

    let best: { tour: number[]; totalCost: number } | null = null;
    let bestStart = -1;

    for (const start of nodeIds) {
      const candidate = computeGreedy(graph, start);
      if (!candidate) continue;
      if (
        !best ||
        candidate.totalCost + EPS < best.totalCost ||
        (Math.abs(candidate.totalCost - best.totalCost) < EPS && start < bestStart)
      ) {
        best = candidate;
        bestStart = start;
      }
    }

    if (!best) {
      putFailure(data, []);
      return { steps, summary: "Tidak ada tur TSP valid.", data };
    }

    steps.push(Step.log(`Start terbaik: ${bestStart} (total bobot = ${formatWeight(best.totalCost)})`));
    steps.push(Step.markStart(bestStart, `Mulai dari node ${bestStart}`));
    steps.push(Step.markPathNode(bestStart, "Node awal tur"));

    for (let i = 1; i < best.tour.length; i++) {
      const from = best.tour[i - 1];
      const to = best.tour[i];
      const edge = graph.getEdge(from, to) as GraphEdge | undefined;
      const w = edge?.weight ?? INF;
      steps.push(Step.traverseEdge(from, to, `Greedy pilih edge ${from} -> ${to} (w=${formatWeight(w)})`));
      steps.push(Step.markPathEdge(from, to, "Masuk tur"));
      if (i < best.tour.length - 1) {
        steps.push(Step.markPathNode(to, `Terkunjungi: ${to}`));
      }
    }
    steps.push(Step.markEnd(bestStart, "Tur selesai"));

    putSuccess(data, best.tour, best.totalCost);

    const summary = `Tur TSP (Greedy) terbaik dari start ${bestStart}: ${best.tour.join(" -> ")} (total bobot = ${formatWeight(best.totalCost)})`;
    return { steps, summary, data };
  }
};

const computeGreedy = (graph: Graph, startNode: number) => {
  const visited = new Set<number>();
  const tour = [startNode];
  visited.add(startNode);

  let current = startNode;
  let totalCost = 0;

  while (visited.size < graph.nodeCount) {
    let bestWeight = INF;
    let bestNext = -1;
    for (const neighbor of graph.getNeighbors(current)) {
      if (visited.has(neighbor)) continue;
      const edge = graph.getEdge(current, neighbor);
      if (edge && Number.isFinite(edge.weight)) {
        if (edge.weight < bestWeight || (Math.abs(edge.weight - bestWeight) < EPS && neighbor < bestNext)) {
          bestWeight = edge.weight;
          bestNext = neighbor;
        }
      }
    }
    if (bestNext === -1) return null;
    tour.push(bestNext);
    visited.add(bestNext);
    totalCost += bestWeight;
    current = bestNext;
  }

  const closing = graph.getEdge(current, startNode);
  if (!closing || !Number.isFinite(closing.weight)) return null;
  tour.push(startNode);
  totalCost += closing.weight;

  return { tour, totalCost };
};

const putSuccess = (data: Record<string, unknown>, tour: number[], cost: number) => {
  data.found = true;
  data.path = [...tour];
  data.distance = cost;
  data.tspTour = [...tour];
  data.tspCost = cost;
};

const putFailure = (data: Record<string, unknown>, tour: number[]) => {
  data.found = false;
  data.path = [...tour];
  data.distance = INF;
  data.tspTour = [...tour];
  data.tspCost = INF;
};
