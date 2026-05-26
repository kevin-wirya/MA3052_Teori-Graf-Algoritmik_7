import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";
import { buildAdjacency, buildPartition } from "@/lib/algorithms/impl/bipartiteHelper";

export const bipartiteMaximumMatchingAlgorithm: GraphAlgorithm = {
  name: "Maximum Matching (Bipartite)",
  category: "Matching",
  description:
    "Mencari matching maksimal pada graf bipartit dengan augmenting path.",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps: any[] = [];
    const data: Record<string, unknown> = {};

    if (graph.nodeCount === 0) {
      return { steps, summary: "Graf kosong, matching tidak dapat dihitung.", data };
    }

    const adj = buildAdjacency(graph);
    const partition = buildPartition(adj);
    if (!partition.bipartite) {
      return {
        steps,
        summary: `Graf tidak bipartit. Konflik pada edge ${partition.conflictU} - ${partition.conflictV}.`,
        data
      };
    }

    const left = Array.from(partition.left).sort((a, b) => a - b);
    const matchTo = new Map<number, number>();
    adj.forEach((_, id) => matchTo.set(id, -1));

    let hallWitness: { leftSet: Set<number>; rightSet: Set<number> } | null = null;

    while (true) {
      let augmented = false;
      for (const start of left) {
        if ((matchTo.get(start) ?? -1) !== -1) continue;
        const search = findAugmentingPath(start, adj, partition.side, matchTo, steps);
        if (search.found) {
          applyAugmentingPath(search.path, matchTo, steps);
          augmented = true;
          hallWitness = null;
          break;
        }
        hallWitness = { leftSet: search.reachedLeft, rightSet: search.reachedRight };
        steps.push(Step.log(`Tidak ada augmenting path dari ${start}, S=${Array.from(hallWitness.leftSet)}, N(S)=${Array.from(hallWitness.rightSet)}`));
      }
      if (!augmented) break;
    }

    const matchingEdges: number[][] = [];
    for (const x of left) {
      const y = matchTo.get(x) ?? -1;
      if (y !== -1) matchingEdges.push([x, y]);
    }

    matchingEdges.forEach((edge) => {
      steps.push(Step.markPathEdge(edge[0], edge[1], `Matching edge: ${edge[0]} - ${edge[1]}`));
    });
    const matchedNodes = new Set(matchingEdges.flat());
    matchedNodes.forEach((nodeId) => steps.push(Step.markPathNode(nodeId, `Matched node: ${nodeId}`)));

    data.matchingEdges = matchingEdges;
    data.matchingSize = matchingEdges.length;
    if (hallWitness && hallWitness.leftSet.size) {
      data.hallSet = Array.from(hallWitness.leftSet);
      data.hallNeighbors = Array.from(hallWitness.rightSet);
    }

    const summary = `Matching maksimal ditemukan. Ukuran matching = ${matchingEdges.length}.`;
    return { steps, summary, data };
  }
};

const findAugmentingPath = (
  start: number,
  adj: Map<number, number[]>,
  side: Map<number, number>,
  matchTo: Map<number, number>,
  steps: any[]
) => {
  const queue: number[] = [start];
  const parent = new Map<number, number>();
  const reachedLeft = new Set<number>();
  const reachedRight = new Set<number>();

  parent.set(start, -1);
  reachedLeft.add(start);
  steps.push(Step.markStart(start, `Mulai dari node bebas ${start}`));

  while (queue.length) {
    const x = queue.shift() as number;
    steps.push(Step.processNode(x, `Proses node ${x}`));

    for (const y of adj.get(x) ?? []) {
      if ((side.get(y) ?? 0) !== 1) continue;
      if ((matchTo.get(x) ?? -1) === y) continue;
      if (reachedRight.has(y)) continue;

      reachedRight.add(y);
      parent.set(y, x);
      steps.push(Step.traverseEdge(x, y, `Cek edge ${x} - ${y}`));

      const matched = matchTo.get(y) ?? -1;
      if (matched === -1) {
        return { found: true, path: reconstructPath(y, parent), reachedLeft, reachedRight };
      }

      if (!reachedLeft.has(matched)) {
        reachedLeft.add(matched);
        parent.set(matched, y);
        queue.push(matched);
        steps.push(Step.traverseEdge(y, matched, `Ikuti edge matching ${y} - ${matched}`));
      }
    }
  }

  return { found: false, path: [], reachedLeft, reachedRight };
};

const reconstructPath = (end: number, parent: Map<number, number>) => {
  const path: number[] = [];
  let cursor = end;
  while (cursor !== -1) {
    path.push(cursor);
    cursor = parent.get(cursor) ?? -1;
  }
  return path.reverse();
};

const applyAugmentingPath = (path: number[], matchTo: Map<number, number>, steps: any[]) => {
  for (let i = 0; i < path.length - 1; i++) {
    const u = path[i];
    const v = path[i + 1];
    if ((matchTo.get(u) ?? -1) === v) {
      matchTo.set(u, -1);
      matchTo.set(v, -1);
      steps.push(Step.log(`Keluarkan edge ${u} - ${v} dari matching`));
    } else {
      matchTo.set(u, v);
      matchTo.set(v, u);
      steps.push(Step.log(`Masukkan edge ${u} - ${v} ke matching`));
    }
  }
};
