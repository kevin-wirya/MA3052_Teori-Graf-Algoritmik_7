import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const girthAlgorithm: GraphAlgorithm = {
  name: "Graph Girth",
  category: "Properties",
  description:
    "Menentukan girth graf, yaitu panjang siklus terpendek.",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps: any[] = [];
    const sortedIds = [...graph.getNodeIds()].sort((a, b) => a - b);
    if (!sortedIds.length) {
      steps.push(Step.log("Graf kosong."));
      return { steps, summary: "Graf kosong, girth tidak terdefinisi.", data: {} };
    }

    let girth = Number.MAX_SAFE_INTEGER;
    let girthCycle: number[] | null = null;

    for (const source of sortedIds) {
      steps.push(Step.visitNode(source, `Mulai BFS dari node ${source}`));
      const dist = new Map<number, number>();
      const parent = new Map<number, number>();
      const queue: number[] = [source];
      dist.set(source, 0);
      parent.set(source, -1);
      let foundCycle = false;

      while (queue.length && !foundCycle) {
        const current = queue.shift() as number;
        for (const neighbor of graph.getNeighbors(current)) {
          if (!dist.has(neighbor)) {
            dist.set(neighbor, (dist.get(current) ?? 0) + 1);
            parent.set(neighbor, current);
            queue.push(neighbor);
            steps.push(
              Step.traverseEdge(current, neighbor, `Edge ${current} -> ${neighbor}, jarak ${dist.get(neighbor)}`)
            );
          } else if (neighbor !== parent.get(current)) {
            const cycleLength = (dist.get(current) ?? 0) + (dist.get(neighbor) ?? 0) + 1;
            steps.push(
              Step.traverseEdge(current, neighbor, `Siklus ditemukan via edge ${current} -> ${neighbor}, panjang ${cycleLength}`)
            );
            if (cycleLength < girth) {
              girth = cycleLength;
              girthCycle = reconstructCycle(parent, current, neighbor, source);
            }
            foundCycle = true;
            break;
          }
        }
      }

      steps.push(
        Step.finishNode(source, foundCycle ? `Siklus terpendek via node ${source} ditemukan.` : `Tidak ada siklus baru via node ${source}.`)
      );
    }

    if (girth !== Number.MAX_SAFE_INTEGER && girthCycle) {
      steps.push(Step.log(`Girth cycle: ${JSON.stringify(girthCycle)} (panjang ${girth})`));
      girthCycle.forEach((node) => steps.push(Step.markPathNode(node, "Node pada girth cycle")));
      for (let i = 0; i < girthCycle.length; i++) {
        const a = girthCycle[i];
        const b = girthCycle[(i + 1) % girthCycle.length];
        steps.push(Step.markPathEdge(a, b, "Edge pada girth cycle"));
      }
    }

    const data: Record<string, unknown> = {};
    let summary = "";
    if (girth === Number.MAX_SAFE_INTEGER || !girthCycle) {
      summary = "Graf tidak memiliki siklus. Girth = INF.";
      data.girth = "INF";
      data.hasCycle = false;
    } else {
      summary = `Girth graf = ${girth} (siklus terpendek: ${JSON.stringify(girthCycle)})`;
      data.girth = girth;
      data.hasCycle = true;
      data.girthCycle = girthCycle;
    }

    return { steps, summary, data };
  }
};

const reconstructCycle = (
  parent: Map<number, number>,
  current: number,
  neighbor: number,
  source: number
) => {
  const pathToCurrent: number[] = [];
  for (let v = current; v !== -1; v = parent.get(v) ?? -1) {
    pathToCurrent.push(v);
  }
  pathToCurrent.reverse();

  const pathToNeighbor: number[] = [];
  for (let v = neighbor; v !== -1; v = parent.get(v) ?? -1) {
    pathToNeighbor.push(v);
  }
  pathToNeighbor.reverse();

  const ancestors = new Set(pathToCurrent);
  let lca = source;
  for (const v of pathToNeighbor) {
    if (ancestors.has(v)) lca = v;
  }

  const cycle: number[] = [];
  let adding = false;
  for (const v of pathToCurrent) {
    if (v === lca) adding = true;
    if (adding) cycle.push(v);
  }

  const reversePart: number[] = [];
  adding = false;
  for (const v of pathToNeighbor) {
    if (v === lca) adding = true;
    if (adding) reversePart.push(v);
  }
  reversePart.reverse();

  for (let i = 0; i < reversePart.length - 1; i++) {
    cycle.push(reversePart[i]);
  }

  return cycle;
};
