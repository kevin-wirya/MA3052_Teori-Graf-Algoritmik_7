import { Graph, GraphEdge } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";
import { formatWeight } from "@/lib/graph/utils";

export const minimumSpanningTreeAlgorithm: GraphAlgorithm = {
  name: "Minimum Spanning Tree (Kruskal)",
  category: "Spanning Tree",
  description:
    "Membangun pohon pembangun minimal menggunakan Kruskal. Jika graf tidak terhubung, hasilnya forest.",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
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
    const edges = graph.getEdges().map((edge) => ({
      source: edge.source,
      target: edge.target,
      weight: edge.weight
    }));
    edges.sort((a, b) => a.weight - b.weight || a.source - b.source || a.target - b.target);

    const uf = new UnionFind(nodeIds);
    const mstEdges: number[][] = [];
    let totalWeight = 0;

    for (const edge of edges) {
      steps.push(
        Step.traverseEdge(
          edge.source,
          edge.target,
          `Mengevaluasi edge ${edge.source} -> ${edge.target} (w=${formatWeight(edge.weight)})`
        )
      );

      if (uf.union(edge.source, edge.target)) {
        mstEdges.push([edge.source, edge.target]);
        totalWeight += edge.weight;
        steps.push(
          Step.markTreeEdge(
            edge.source,
            edge.target,
            `Edge dipilih ke MST: ${edge.source} - ${edge.target}`
          )
        );
        steps.push(Step.log(`Edge diterima, total bobot = ${formatWeight(totalWeight)}`));
      } else {
        steps.push(Step.log("Edge ditolak karena membentuk siklus."));
        steps.push(Step.finishEdge(edge.source, edge.target, ""));
      }
    }

    const connected = uf.componentCount() <= 1;
    const summary = connected
      ? `MST ditemukan dengan total bobot = ${formatWeight(totalWeight)} (${mstEdges.length} edge).`
      : `Minimum spanning forest ditemukan dengan total bobot = ${formatWeight(totalWeight)} (${mstEdges.length} edge).`;

    data.found = true;
    data.mstEdges = mstEdges;
    data.mstWeight = totalWeight;
    data.connected = connected;
    data.componentCount = uf.componentCount();

    return { steps, summary, data };
  }
};

class UnionFind {
  private parent = new Map<number, number>();
  private rank = new Map<number, number>();

  constructor(nodes: number[]) {
    nodes.forEach((node) => {
      this.parent.set(node, node);
      this.rank.set(node, 0);
    });
  }

  private find(x: number): number {
    const root = this.parent.get(x) ?? x;
    if (root !== x) {
      const newRoot = this.find(root);
      this.parent.set(x, newRoot);
      return newRoot;
    }
    return root;
  }

  union(a: number, b: number): boolean {
    let rootA = this.find(a);
    let rootB = this.find(b);
    if (rootA === rootB) return false;

    const rankA = this.rank.get(rootA) ?? 0;
    const rankB = this.rank.get(rootB) ?? 0;

    if (rankA < rankB) {
      this.parent.set(rootA, rootB);
    } else if (rankA > rankB) {
      this.parent.set(rootB, rootA);
    } else {
      this.parent.set(rootB, rootA);
      this.rank.set(rootA, rankA + 1);
    }
    return true;
  }

  componentCount(): number {
    const roots = new Set<number>();
    for (const node of this.parent.keys()) {
      roots.add(this.find(node));
    }
    return roots.size;
  }
}
