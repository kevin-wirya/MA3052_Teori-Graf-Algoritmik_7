import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const mazeBacktrackingAlgorithm: GraphAlgorithm = {
  name: "Maze Backtracking Solver",
  category: "Pathfinding",
  description: "Menyelesaikan labirin grid dengan pencarian mundur (backtracking DFS). Menampilkan ekspansi jalur hijau dan penyusutan (retract) saat menabrak jalan buntu.",
  requiredParameters: [
    { key: "startNode", label: "Start Node", type: "NODE_SELECT", defaultValue: 0, required: true },
    { key: "endNode", label: "End Node", type: "NODE_SELECT", defaultValue: 24, required: true }
  ],
  execute(graph: Graph, parameters: Record<string, unknown>): AlgorithmResult {
    const steps: any[] = [];
    const visited = new Set<number>();
    const path: number[] = [];

    const nodes = graph.getNodes();
    if (nodes.length === 0) {
      return { steps: [], summary: "Graf kosong.", data: {} };
    }

    const R = (graph as any).gridRows ?? (nodes.length > 0 ? Math.max(...nodes.map((n) => (n as any).gridRow ?? 0)) + 1 : 0);
    const C = (graph as any).gridCols ?? (nodes.length > 0 ? Math.max(...nodes.map((n) => (n as any).gridCol ?? 0)) + 1 : 0);

    const getCellCoords = (id: number) => {
      const node = graph.getNode(id);
      if (node && (node as any).gridRow !== undefined) {
        return { r: (node as any).gridRow, c: (node as any).gridCol };
      }
      return { r: Math.floor(id / C), c: id % C };
    };

    // Cari land cells
    const landCells = nodes.filter(n => (n as any).isLand);
    if (landCells.length === 0) {
      return { steps: [Step.log("Graf tidak memiliki sel jalan (land).")], summary: "Tidak ada jalur yang valid.", data: {} };
    }

    let startId = Number(parameters.startNode ?? 0);
    let endId = Number(parameters.endNode ?? (R * C - 1));

    // Fallback jika id tidak ada atau bukan land
    if (!graph.getNode(startId) || !(graph.getNode(startId) as any).isLand) {
      startId = landCells[0].id;
    }
    if (!graph.getNode(endId) || !(graph.getNode(endId) as any).isLand) {
      endId = landCells[landCells.length - 1].id;
    }

    steps.push(Step.markStart(startId, `Memulai pencarian labirin dari (${getCellCoords(startId).r}, ${getCellCoords(startId).c})`));
    steps.push(Step.markEnd(endId, `Target labirin di (${getCellCoords(endId).r}, ${getCellCoords(endId).c})`));

    let found = false;

    const solve = (curr: number, parent: number): boolean => {
      visited.add(curr);
      path.push(curr);

      const currCoords = getCellCoords(curr);

      if (parent !== -1) {
        steps.push(Step.markPathEdge(parent, curr, `Menelusuri jalan dari (${getCellCoords(parent).r}, ${getCellCoords(parent).c}) ke (${currCoords.r}, ${currCoords.c})`));
      }
      steps.push(Step.markPathNode(curr, `Mengunjungi sel (${currCoords.r}, ${currCoords.c})`));

      if (curr === endId) {
        steps.push(Step.log(`Target di (${currCoords.r}, ${currCoords.c}) berhasil ditemukan!`));
        found = true;
        return true;
      }

      // Prioritas 4-arah (Atas, Bawah, Kiri, Kanan)
      const neighbors = graph.getNeighbors(curr).filter(n => {
        const node = graph.getNode(n);
        return node && (node as any).isLand && !visited.has(n);
      });

      for (const next of neighbors) {
        if (solve(next, curr)) {
          return true;
        }
      }

      // Backtrack (Retract) jika tidak menemukan jalan keluar lewat cabang ini
      path.pop();
      steps.push(Step.finishNode(curr, `Jalan buntu! Menyusut (backtrack) dari (${currCoords.r}, ${currCoords.c})`));
      if (parent !== -1) {
        steps.push(Step.finishEdge(parent, curr, ""));
      }

      return false;
    };

    solve(startId, -1);

    const summary = found
      ? `Labirin berhasil diselesaikan dengan panjang jalur ${path.length} langkah.`
      : "Labirin tidak dapat diselesaikan karena tidak ada jalur yang menghubungkan Start dan End.";

    return {
      steps,
      summary,
      data: { found, path }
    };
  }
};
