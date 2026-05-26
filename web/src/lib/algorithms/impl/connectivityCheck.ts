import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const connectivityCheckAlgorithm: GraphAlgorithm = {
  name: "Connectivity Check",
  category: "Connectivity",
  description:
    "Memeriksa apakah graf terhubung menggunakan DFS dari node pertama.",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps = [];
    const visited = new Set<number>();

    const nodeIds = graph.getNodeIds();
    if (!nodeIds.length) {
      return { steps, summary: "Graf kosong.", data: {} };
    }

    const startNode = nodeIds[0];
    steps.push(Step.markStart(startNode, `Memulai pengecekan dari node ${startNode}`));

    const dfs = (node: number) => {
      visited.add(node);
      steps.push(Step.visitNode(node, `Mengunjungi node ${node}`));
      for (const neighbor of graph.getNeighbors(node)) {
        if (!visited.has(neighbor)) {
          steps.push(Step.traverseEdge(node, neighbor, ""));
          dfs(neighbor);
        }
      }
      steps.push(Step.finishNode(node, `Selesai node ${node}`));
    };

    dfs(startNode);

    const connected = visited.size === graph.nodeCount;
    const summary = connected
      ? `Graf terhubung. Semua ${graph.nodeCount} node dapat dijangkau.`
      : `Graf tidak terhubung. Dijangkau ${visited.size}/${graph.nodeCount} node.`;

    return {
      steps,
      summary,
      data: { connected, visitedCount: visited.size }
    };
  }
};
