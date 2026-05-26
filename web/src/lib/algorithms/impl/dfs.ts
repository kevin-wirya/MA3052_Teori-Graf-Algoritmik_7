import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const dfsAlgorithm: GraphAlgorithm = {
  name: "Depth-First Search (DFS)",
  category: "Traversal",
  description:
    "Menelusuri graf secara mendalam menggunakan rekursi. Mengunjungi cabang sedalam mungkin sebelum backtrack.",
  requiredParameters: [
    { key: "startNode", label: "Start Node", type: "NODE_SELECT", defaultValue: 0, required: true }
  ],
  execute(graph: Graph, parameters: Record<string, unknown>): AlgorithmResult {
    const startNode = Number(parameters.startNode ?? 0);
    const steps = [];
    const visited = new Set<number>();
    const order: number[] = [];

    steps.push(Step.markStart(startNode, `Memulai DFS dari node ${startNode}`));
    const dfs = (node: number, parent: number) => {
      visited.add(node);
      order.push(node);
      if (parent !== -1) {
        steps.push(Step.traverseEdge(parent, node, `Menelusuri edge ${parent} -> ${node}`));
      }
      steps.push(Step.visitNode(node, `Mengunjungi node ${node}`));

      for (const neighbor of graph.getNeighbors(node)) {
        if (!visited.has(neighbor)) {
          dfs(neighbor, node);
        }
      }

      steps.push(Step.finishNode(node, `Selesai node ${node}`));
      if (parent !== -1) steps.push(Step.finishEdge(parent, node, ""));
    };

    dfs(startNode, -1);

    const summary = `DFS selesai. Urutan: ${JSON.stringify(order)} (${order.length} node)`;
    return {
      steps,
      summary,
      data: { traversalOrder: order }
    };
  }
};
