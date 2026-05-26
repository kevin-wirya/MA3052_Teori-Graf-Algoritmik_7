import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const largestComponentAlgorithm: GraphAlgorithm = {
  name: "Largest Component",
  category: "Connectivity",
  description:
    "Menemukan komponen terhubung terbesar dalam graf menggunakan DFS.",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps: any[] = [];
    const visited = new Set<number>();
    const components: number[][] = [];
    let compId = 0;

    const sortedIds = [...graph.getNodeIds()].sort((a, b) => a - b);

    const dfs = (node: number, component: number[]) => {
      visited.add(node);
      component.push(node);
      steps.push(Step.visitNode(node, `Mengunjungi node ${node}`));
      for (const neighbor of graph.getNeighbors(node)) {
        if (!visited.has(neighbor)) {
          steps.push(Step.traverseEdge(node, neighbor, ""));
          dfs(neighbor, component);
        }
      }
      steps.push(Step.finishNode(node, ""));
    };

    for (const nodeId of sortedIds) {
      if (!visited.has(nodeId)) {
        const component: number[] = [];
        steps.push(Step.log(`Menelusuri komponen #${compId + 1} mulai dari node ${nodeId}`));
        dfs(nodeId, component);
        components.push(component);
        steps.push(Step.markComponent(compId, component, `Komponen #${compId + 1}`));
        compId += 1;
      }
    }

    let largestIdx = 0;
    for (let i = 1; i < components.length; i++) {
      if (components[i].length > components[largestIdx].length) largestIdx = i;
    }

    const largest = components[largestIdx] ?? [];
    for (const nodeId of largest) {
      steps.push(Step.markPathNode(nodeId, ""));
    }
    steps.push(Step.log(`Komponen terbesar adalah #${largestIdx + 1} (${largest.length} node)`));

    const summary = `Komponen terbesar #${largestIdx + 1} (${largest.length} node)`;
    return {
      steps,
      summary,
      data: {
        componentCount: components.length,
        components,
        largestIndex: largestIdx,
        largestComponent: largest
      }
    };
  }
};
