import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const connectedComponentsAlgorithm: GraphAlgorithm = {
  name: "Connected Components",
  category: "Connectivity",
  description:
    "Menemukan semua komponen terhubung menggunakan DFS dan memberi warna berbeda.",
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
        steps.push(Step.log(`Menemukan komponen baru #${compId + 1} mulai dari ${nodeId}`));
        dfs(nodeId, component);
        components.push(component);
        steps.push(Step.markComponent(compId, component, `Komponen #${compId + 1}: ${JSON.stringify(component)}`));
        compId += 1;
      }
    }

    const summary = `Ditemukan ${components.length} komponen terhubung.`;
    return {
      steps,
      summary,
      data: { componentCount: components.length, components }
    };
  }
};
