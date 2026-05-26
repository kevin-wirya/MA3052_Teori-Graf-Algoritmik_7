import { Graph, GraphEdge } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";
import { formatWeight } from "@/lib/graph/utils";

const EPS = 1e-9;
const INF = Number.POSITIVE_INFINITY;
const NO_PARENT = -1;
const MAX_EXACT_NODES = 20;

export const tspExactAlgorithm: GraphAlgorithm = {
  name: "Traveling Salesman (Exact DP)",
  category: "Optimization",
  description:
    "Tur TSP optimal global dengan Held-Karp (dynamic programming).",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps: any[] = [];
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

    if (graph.nodeCount > MAX_EXACT_NODES) {
      steps.push(Step.log(`Jumlah node ${graph.nodeCount} melebihi batas exact solver (${MAX_EXACT_NODES}).`));
      putFailure(data, []);
      return {
        steps,
        summary: "Graf terlalu besar untuk TSP exact Held-Karp.",
        data
      };
    }

    steps.push(Step.log("Menggunakan Held-Karp dynamic programming untuk semua start node."));

    let bestTour: number[] | null = null;
    let bestCost = INF;
    let bestStart = -1;

    for (const startNode of nodeIds) {
      const ordered = buildOrderedNodeList(nodeIds, startNode);
      const weights = buildWeightMatrix(graph, ordered);
      const result = solveHeldKarp(weights, ordered);
      if (!result) continue;
      const tour = result.tourNodeIndices.map((idx) => ordered[idx]);
      if (
        result.totalCost + EPS < bestCost ||
        (Math.abs(result.totalCost - bestCost) < EPS && startNode < bestStart)
      ) {
        bestCost = result.totalCost;
        bestTour = tour;
        bestStart = startNode;
      }
    }

    if (!bestTour) {
      putFailure(data, []);
      return { steps, summary: "Tidak ada tur TSP valid.", data };
    }

    steps.push(Step.log(`Start terbaik: ${bestStart} (total bobot = ${formatWeight(bestCost)})`));
    steps.push(Step.markStart(bestStart, `Mulai tur dari node ${bestStart}`));
    steps.push(Step.markPathNode(bestStart, "Node awal tur"));

    for (let i = 1; i < bestTour.length; i++) {
      const fromNode = bestTour[i - 1];
      const toNode = bestTour[i];
      const edge = graph.getEdge(fromNode, toNode) as GraphEdge | undefined;
      const w = edge?.weight ?? INF;
      steps.push(Step.traverseEdge(fromNode, toNode, `Pilih edge tur ${fromNode} -> ${toNode} (w=${formatWeight(w)})`));
      steps.push(Step.markPathEdge(fromNode, toNode, "Masuk tur optimal"));
      if (i < bestTour.length - 1) {
        steps.push(Step.markPathNode(toNode, `Node dalam tur: ${toNode}`));
      }
    }
    steps.push(Step.markEnd(bestStart, "Tur selesai di node awal"));

    putSuccess(data, bestTour, bestCost);

    const summary = `Tur TSP optimal terbaik dari start ${bestStart}: ${bestTour.join(" -> ")} (total bobot = ${formatWeight(bestCost)})`;
    return { steps, summary, data };
  }
};

const solveHeldKarp = (weights: number[][], orderedNodeIds: number[]) => {
  const n = orderedNodeIds.length;
  const otherCount = n - 1;
  const stateCount = 1 << otherCount;

  const dp = Array(stateCount * otherCount).fill(INF);
  const parent = Array(stateCount * otherCount).fill(NO_PARENT);

  for (let j = 0; j < otherCount; j++) {
    const direct = weights[0][j + 1];
    if (Number.isFinite(direct)) {
      const index = flatIndex(1 << j, j, otherCount);
      dp[index] = direct;
    }
  }

  for (let mask = 1; mask < stateCount; mask++) {
    for (let j = 0; j < otherCount; j++) {
      if ((mask & (1 << j)) === 0) continue;
      const prevMask = mask ^ (1 << j);
      if (prevMask === 0) continue;

      const currentIndex = flatIndex(mask, j, otherCount);
      let bestCost = dp[currentIndex];
      let bestPrev = parent[currentIndex];

      for (let k = 0; k < otherCount; k++) {
        if ((prevMask & (1 << k)) === 0) continue;
        const prevCost = dp[flatIndex(prevMask, k, otherCount)];
        const edgeCost = weights[k + 1][j + 1];
        if (!Number.isFinite(prevCost) || !Number.isFinite(edgeCost)) continue;

        const candidate = prevCost + edgeCost;
        const candidateTieNodeId = orderedNodeIds[k + 1];
        const incumbentTieNodeId = bestPrev === NO_PARENT ? Number.MAX_SAFE_INTEGER : orderedNodeIds[bestPrev + 1];

        if (isBetterCandidate(candidate, candidateTieNodeId, bestCost, incumbentTieNodeId)) {
          bestCost = candidate;
          bestPrev = k;
        }
      }

      if (Number.isFinite(bestCost)) {
        dp[currentIndex] = bestCost;
        parent[currentIndex] = bestPrev;
      }
    }
  }

  const fullMask = stateCount - 1;
  let bestTourCost = INF;
  let bestLast = NO_PARENT;

  for (let j = 0; j < otherCount; j++) {
    const pathCost = dp[flatIndex(fullMask, j, otherCount)];
    const closingCost = weights[j + 1][0];
    if (!Number.isFinite(pathCost) || !Number.isFinite(closingCost)) continue;

    const candidate = pathCost + closingCost;
    const candidateTieNodeId = orderedNodeIds[j + 1];
    const incumbentTieNodeId = bestLast === NO_PARENT ? Number.MAX_SAFE_INTEGER : orderedNodeIds[bestLast + 1];

    if (isBetterCandidate(candidate, candidateTieNodeId, bestTourCost, incumbentTieNodeId)) {
      bestTourCost = candidate;
      bestLast = j;
    }
  }

  if (!Number.isFinite(bestTourCost)) return null;

  const reversePath: number[] = [];
  let mask = fullMask;
  let cursor = bestLast;
  while (cursor !== NO_PARENT) {
    reversePath.push(cursor + 1);
    const parentIndex = parent[flatIndex(mask, cursor, otherCount)];
    mask ^= 1 << cursor;
    cursor = parentIndex;
  }
  reversePath.reverse();

  const tourNodeIndices = [0, ...reversePath, 0];

  return { tourNodeIndices, totalCost: bestTourCost };
};

const isBetterCandidate = (
  candidateCost: number,
  candidateTieNodeId: number,
  incumbentCost: number,
  incumbentTieNodeId: number
) => {
  if (candidateCost + EPS < incumbentCost) return true;
  return Math.abs(candidateCost - incumbentCost) < EPS && candidateTieNodeId < incumbentTieNodeId;
};

const flatIndex = (mask: number, endpoint: number, width: number) => mask * width + endpoint;

const buildOrderedNodeList = (sortedNodeIds: number[], startNode: number) => {
  const ordered = [startNode];
  sortedNodeIds.forEach((nodeId) => {
    if (nodeId !== startNode) ordered.push(nodeId);
  });
  return ordered;
};

const buildWeightMatrix = (graph: Graph, orderedNodeIds: number[]) => {
  const n = orderedNodeIds.length;
  const weights = Array.from({ length: n }, () => Array(n).fill(INF));
  for (let i = 0; i < n; i++) {
    weights[i][i] = 0;
    for (let j = 0; j < n; j++) {
      if (i === j) continue;
      const edge = graph.getEdge(orderedNodeIds[i], orderedNodeIds[j]);
      if (edge && Number.isFinite(edge.weight)) weights[i][j] = edge.weight;
    }
  }
  return weights;
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
