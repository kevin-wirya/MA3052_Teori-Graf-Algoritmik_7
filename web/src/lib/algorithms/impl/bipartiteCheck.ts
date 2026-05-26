import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const bipartiteCheckAlgorithm: GraphAlgorithm = {
  name: "Bipartite Check",
  category: "Properties",
  description:
    "Mengecek apakah graf bipartit menggunakan BFS 2-coloring.",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps = [];
    const color = new Map<number, number>();
    const setA: number[] = [];
    const setB: number[] = [];
    let bipartite = true;
    let conflictU = -1;
    let conflictV = -1;

    const sortedIds = [...graph.getNodeIds()].sort((a, b) => a - b);

    for (const startNode of sortedIds) {
      if (color.has(startNode)) continue;
      const queue: number[] = [startNode];
      color.set(startNode, 0);
      setA.push(startNode);
      steps.push(Step.visitNode(startNode, `Mewarnai node ${startNode} dengan warna A`));

      while (queue.length && bipartite) {
        const u = queue.shift() as number;
        steps.push(Step.processNode(u, `Memproses node ${u}`));

        for (const v of graph.getNeighbors(u)) {
          if (!color.has(v)) {
            const nextColor = 1 - (color.get(u) ?? 0);
            color.set(v, nextColor);
            if (nextColor === 0) setA.push(v);
            else setB.push(v);
            queue.push(v);
            steps.push(Step.traverseEdge(u, v, `Mewarnai node ${v} dengan warna ${nextColor === 0 ? "A" : "B"}`));
            steps.push(Step.visitNode(v, `Node ${v} masuk himpunan ${nextColor === 0 ? "A" : "B"}`));
          } else if (color.get(v) === color.get(u)) {
            bipartite = false;
            conflictU = u;
            conflictV = v;
            steps.push(Step.traverseEdge(u, v, `Konflik: node ${u} dan ${v} warna sama`));
            steps.push(Step.markPathNode(u, `Node konflik ${u}`));
            steps.push(Step.markPathNode(v, `Node konflik ${v}`));
            break;
          }
        }
      }
      if (!bipartite) break;
    }

    if (bipartite) {
      if (setA.length) steps.push(Step.markComponent(0, setA, `Himpunan A: ${JSON.stringify(setA)}`));
      if (setB.length) steps.push(Step.markComponent(1, setB, `Himpunan B: ${JSON.stringify(setB)}`));
    }

    const data: Record<string, unknown> = { bipartite };
    let summary = "";
    if (bipartite) {
      data.setA = setA;
      data.setB = setB;
      summary = `Graf bipartit. A(${setA.length})=${JSON.stringify(setA)}, B(${setB.length})=${JSON.stringify(setB)}`;
    } else {
      data.conflictEdge = [conflictU, conflictV];
      summary = `Graf tidak bipartit. Konflik pada edge ${conflictU} - ${conflictV}`;
    }

    return { steps, summary, data };
  }
};
