import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const islandCountAlgorithm: GraphAlgorithm = {
  name: "Island Count",
  category: "Connectivity",
  description: "Menghitung jumlah pulau dalam matriks biner dengan konektivitas 4-arah.",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps: any[] = [];
    const visited = new Set<number>();
    const islands: number[][] = [];

    const nodes = graph.getNodes();
    const R = (graph as any).gridRows ?? (nodes.length > 0 ? Math.max(...nodes.map((n) => (n as any).gridRow ?? 0)) + 1 : 0);
    const C = (graph as any).gridCols ?? (nodes.length > 0 ? Math.max(...nodes.map((n) => (n as any).gridCol ?? 0)) + 1 : 0);

    const getCellId = (r: number, c: number) => r * C + c;

    const dfs = (r: number, c: number, island: number[]) => {
      const id = getCellId(r, c);
      visited.add(id);
      island.push(id);
      steps.push(Step.visitNode(id, `Mengunjungi sel land (${r}, ${c})`));

      const dr = [-1, 1, 0, 0];
      const dc = [0, 0, -1, 1];

      for (let i = 0; i < 4; i++) {
        const nr = r + dr[i];
        const nc = c + dc[i];

        if (nr >= 0 && nr < R && nc >= 0 && nc < C) {
          const neighborId = getCellId(nr, nc);
          const neighborNode = graph.getNode(neighborId);
          if (neighborNode && (neighborNode as any).isLand && !visited.has(neighborId)) {
            steps.push(Step.traverseEdge(id, neighborId, `Menelusuri dari (${r}, ${c}) ke (${nr}, ${nc})`));
            dfs(nr, nc, island);
          }
        }
      }
      steps.push(Step.finishNode(id, `Selesai memproses sel (${r}, ${c})`));
    };

    let islandIndex = 0;

    for (let r = 0; r < R; r++) {
      for (let c = 0; c < C; c++) {
        const id = getCellId(r, c);
        const node = graph.getNode(id);

        if (node && (node as any).isLand) {
          if (!visited.has(id)) {
            const island: number[] = [];
            steps.push(Step.log(`Menemukan pulau baru #${islandIndex + 1} mulai dari sel (${r}, ${c})`));
            dfs(r, c, island);
            islands.push(island);
            steps.push(Step.markComponent(islandIndex, island, `Pulau #${islandIndex + 1} teridentifikasi.`));
            islandIndex++;
          }
        }
      }
    }

    const summary = `Jumlah pulau yang teridentifikasi adalah ${islands.length}.`;
    return {
      steps,
      summary,
      data: { islandCount: islands.length, islands }
    };
  }
};
