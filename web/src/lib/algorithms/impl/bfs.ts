import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const bfsAlgorithm: GraphAlgorithm = {
  name: "Breadth-First Search (BFS)",
  category: "Traversal",
  description:
    "Menelusuri graf secara melebar (breadth-first) menggunakan queue. Mengunjungi semua tetangga dulu.",
  requiredParameters: [
    { key: "startNode", label: "Start Node", type: "NODE_SELECT", defaultValue: 0, required: true }
  ],
  execute(graph: Graph, parameters: Record<string, unknown>): AlgorithmResult {
    const startNode = Number(parameters.startNode ?? 0);
    const steps = [];
    const visited = new Set<number>();
    const queue: number[] = [];
    const order: number[] = [];

    visited.add(startNode);
    queue.push(startNode);
    steps.push(Step.markStart(startNode, `Memulai BFS dari node ${startNode}`));
    steps.push(Step.visitNode(startNode, `Enqueue node ${startNode}`));

    while (queue.length) {
      const current = queue.shift() as number;
      order.push(current);
      steps.push(Step.processNode(current, `Dequeue dan proses node ${current}`));

      for (const neighbor of graph.getNeighbors(current)) {
        if (!visited.has(neighbor)) {
          visited.add(neighbor);
          queue.push(neighbor);
          steps.push(Step.traverseEdge(current, neighbor, `Menelusuri edge ${current} -> ${neighbor}`));
          steps.push(Step.visitNode(neighbor, `Enqueue node ${neighbor}`));
        }
      }

      steps.push(Step.finishNode(current, `Selesai memproses node ${current}`));
    }

    const summary = `BFS selesai. Urutan: ${JSON.stringify(order)} (${order.length} node)`;
    return { steps, summary, data: { traversalOrder: order } };
  }
};
