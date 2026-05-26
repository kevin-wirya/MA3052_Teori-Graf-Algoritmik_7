import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const pathFinderAlgorithm: GraphAlgorithm = {
  name: "Path Finder (A -> B)",
  category: "Path Finding",
  description:
    "Mencari lintasan dari node A ke node B menggunakan DFS dengan backtracking.",
  requiredParameters: [
    { key: "startNode", label: "Start Node (A)", type: "NODE_SELECT", defaultValue: 0, required: true },
    { key: "endNode", label: "End Node (B)", type: "NODE_SELECT", defaultValue: 0, required: true }
  ],
  execute(graph: Graph, parameters: Record<string, unknown>): AlgorithmResult {
    const startNode = Number(parameters.startNode ?? 0);
    const endNode = Number(parameters.endNode ?? 0);
    const steps: any[] = [];
    const visited = new Set<number>();
    const path: number[] = [];

    steps.push(Step.markStart(startNode, `Node awal: ${startNode}`));
    steps.push(Step.markEnd(endNode, `Node tujuan: ${endNode}`));

    const dfs = (current: number): boolean => {
      visited.add(current);
      path.push(current);
      steps.push(Step.visitNode(current, `Mengunjungi node ${current}`));
      if (current === endNode) {
        steps.push(Step.log(`Node tujuan ${endNode} ditemukan`));
        return true;
      }

      for (const neighbor of graph.getNeighbors(current)) {
        if (!visited.has(neighbor)) {
          steps.push(Step.traverseEdge(current, neighbor, `Mencoba ${current} -> ${neighbor}`));
          if (dfs(neighbor)) return true;
          steps.push(Step.log(`Backtrack dari ${neighbor}`));
        }
      }

      path.pop();
      steps.push(Step.finishNode(current, `Backtrack dari node ${current}`));
      return false;
    };

    const found = dfs(startNode);
    if (found) {
      for (let i = 0; i < path.length; i++) {
        steps.push(Step.markPathNode(path[i], ""));
        if (i > 0) steps.push(Step.markPathEdge(path[i - 1], path[i], ""));
      }
    }

    const summary = found
      ? `Lintasan ditemukan: ${JSON.stringify(path)}`
      : `Tidak ada lintasan dari ${startNode} ke ${endNode}`;

    return {
      steps,
      summary,
      data: { found, path: found ? [...path] : [] }
    };
  }
};
