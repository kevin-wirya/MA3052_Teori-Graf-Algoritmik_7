import { Graph } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";

export const welshPowellAlgorithm: GraphAlgorithm = {
  name: "Welsh-Powell Graph Coloring",
  category: "Coloring",
  description: "Mewarnai graf sedemikian rupa sehingga tidak ada dua node bertetangga dengan warna sama menggunakan jumlah warna minimal (chromatic number).",
  requiredParameters: [],
  execute(graph: Graph): AlgorithmResult {
    const steps: any[] = [];
    const nodes = graph.getNodes();
    if (nodes.length === 0) {
      return { steps: [], summary: "Graf kosong.", data: {} };
    }

    // 1. Urutkan node berdasarkan derajat menurun
    const sortedNodes = [...nodes].sort((a, b) => {
      const degA = graph.getNeighbors(a.id).length;
      const degB = graph.getNeighbors(b.id).length;
      return degB - degA;
    });

    const degreeInfo = sortedNodes.map(n => `Node ${n.id} (deg: ${graph.getNeighbors(n.id).length})`).join(", ");
    steps.push(Step.log(`Mengurutkan node berdasarkan derajat terbesar: ${degreeInfo}`));

    const coloredNodes = new Map<number, number>(); // Node ID -> Color Index (0 to 6)
    let colorIndex = 0;

    // Loop sampai semua terwarnai
    while (coloredNodes.size < nodes.length) {
      const currentGroup: number[] = [];
      steps.push(Step.log(`Memulai pewarnaan untuk kelompok Warna #${colorIndex + 1}`));

      for (const node of sortedNodes) {
        if (coloredNodes.has(node.id)) continue;

        // Cek apakah ada tetangga yang sudah berwarna warna ini
        const neighbors = graph.getNeighbors(node.id);
        const hasAdjacentWithSameColor = neighbors.some(neighborId => 
          coloredNodes.get(neighborId) === colorIndex
        );

        if (!hasAdjacentWithSameColor) {
          currentGroup.push(node.id);
          coloredNodes.set(node.id, colorIndex);
          steps.push(Step.markComponent(colorIndex, [node.id], `Mewarnai Node ${node.id} dengan kelompok Warna #${colorIndex + 1}`));
        } else {
          steps.push(Step.log(`Node ${node.id} dilewati karena bertetangga dengan node berwarna sama.`));
        }
      }

      steps.push(Step.markComponent(colorIndex, currentGroup, `Selesai mewarnai kelompok Warna #${colorIndex + 1}`));
      colorIndex++;
    }

    const chromaticNumber = colorIndex;
    const summary = `Pewarnaan graf Welsh-Powell selesai. Bilangan kromatik (Chromatic Number) = ${chromaticNumber}.`;

    return {
      steps,
      summary,
      data: { chromaticNumber, colors: Object.fromEntries(coloredNodes) }
    };
  }
};
