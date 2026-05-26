import { Graph, GraphEdge } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";
import { formatWeight } from "@/lib/graph/utils";

export const primMinimumSpanningTreeAlgorithm: GraphAlgorithm = {
  name: "Minimum Spanning Tree (Prim)",
  category: "Spanning Tree",
  description:
    "Membangun pohon pembangun minimal menggunakan algoritma Prim. Jika graf tidak terhubung, hasilnya forest.",
  requiredParameters: [
    { key: "startNode", label: "Start Node", type: "NODE_SELECT", defaultValue: 0, required: true }
  ],
  execute(graph: Graph, parameters: Record<string, unknown>): AlgorithmResult {
    const steps: any[] = [];
    const data: Record<string, unknown> = {};

    if (graph.nodeCount === 0) {
      data.found = false;
      data.mstEdges = [];
      data.mstWeight = 0;
      data.connected = false;
      return { steps, summary: "Graf kosong, MST tidak terdefinisi.", data };
    }

    if (graph.directed) {
      data.found = false;
      data.mstEdges = [];
      data.mstWeight = 0;
      data.connected = false;
      return { steps, summary: "MST hanya untuk graf undirected.", data };
    }

    const nodeIds = [...graph.getNodeIds()].sort((a, b) => a - b);
    const requestedStart = Number(parameters.startNode ?? nodeIds[0]);
    const firstStart = nodeIds.includes(requestedStart) ? requestedStart : nodeIds[0];

    const visited = new Set<number>();
    const mstEdges: number[][] = [];
    let totalWeight = 0;

    const pq: CandidateEdge[] = [];

    const addEdges = (node: number) => {
      visited.add(node);
      steps.push(Step.visitNode(node, `Mengunjungi node ${node}`));
      for (const neighbor of graph.getNeighbors(node)) {
        if (visited.has(neighbor)) continue;
        const edge = graph.getEdge(node, neighbor) as GraphEdge | undefined;
        if (!edge) continue;
        pq.push({ from: node, to: neighbor, weight: edge.weight });
        steps.push(
          Step.traverseEdge(node, neighbor, `Menambahkan kandidat edge ${node} -> ${neighbor} (w=${formatWeight(edge.weight)})`)
        );
      }
      steps.push(Step.finishNode(node, `Selesai node ${node}`));
    };

    for (const startNode of nodeIds) {
      if (visited.has(startNode)) continue;
      const actualStart = startNode === nodeIds[0] ? firstStart : startNode;
      steps.push(Step.markStart(actualStart, `Memulai Prim dari node ${actualStart}`));
      addEdges(actualStart);

      while (pq.length) {
        pq.sort((a, b) => a.weight - b.weight || a.from - b.from || a.to - b.to);
        const candidate = pq.shift() as CandidateEdge;
        if (visited.has(candidate.to)) continue;
        steps.push(
          Step.traverseEdge(candidate.from, candidate.to, `Memeriksa kandidat edge ${candidate.from} -> ${candidate.to} (w=${formatWeight(candidate.weight)})`)
        );
        mstEdges.push([candidate.from, candidate.to]);
        totalWeight += candidate.weight;
        steps.push(
          Step.markTreeEdge(candidate.from, candidate.to, `Edge dipilih ke MST: ${candidate.from} - ${candidate.to}`)
        );
        steps.push(Step.log(`Edge diterima, total bobot = ${formatWeight(totalWeight)}`));
        addEdges(candidate.to);
      }
    }

    const connected = visited.size === nodeIds.length;
    const summary = connected
      ? `MST ditemukan dengan total bobot = ${formatWeight(totalWeight)} (${mstEdges.length} edge).`
      : `Minimum spanning forest ditemukan dengan total bobot = ${formatWeight(totalWeight)} (${mstEdges.length} edge).`;

    data.found = true;
    data.mstEdges = mstEdges;
    data.mstWeight = totalWeight;
    data.connected = connected;
    data.componentCount = countComponents(graph);

    return { steps, summary, data };
  }
};

interface CandidateEdge {
  from: number;
  to: number;
  weight: number;
}

const countComponents = (graph: Graph) => {
  const visited = new Set<number>();
  const ids = [...graph.getNodeIds()].sort((a, b) => a - b);
  let components = 0;
  for (const node of ids) {
    if (visited.has(node)) continue;
    components += 1;
    const stack = [node];
    visited.add(node);
    while (stack.length) {
      const current = stack.pop() as number;
      for (const neighbor of graph.getNeighbors(current)) {
        if (!visited.has(neighbor)) {
          visited.add(neighbor);
          stack.push(neighbor);
        }
      }
    }
  }
  return components;
};
