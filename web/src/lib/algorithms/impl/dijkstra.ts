import { Graph, GraphEdge } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";
import { formatWeight } from "@/lib/graph/utils";

const EPS = 1e-9;

export const dijkstraAlgorithm: GraphAlgorithm = {
  name: "Shortest Path (Dijkstra)",
  category: "Path Finding",
  description:
    "Menentukan lintasan terpendek dari node A ke node B menggunakan Dijkstra.",
  requiredParameters: [
    { key: "startNode", label: "Start Node (A)", type: "NODE_SELECT", defaultValue: 0, required: true },
    { key: "endNode", label: "End Node (B)", type: "NODE_SELECT", defaultValue: 0, required: true }
  ],
  execute(graph: Graph, parameters: Record<string, unknown>): AlgorithmResult {
    const startNode = Number(parameters.startNode ?? 0);
    const endNode = Number(parameters.endNode ?? 0);
    const steps: any[] = [];
    const data: Record<string, unknown> = {};

    if (!graph.getNode(startNode) || !graph.getNode(endNode)) {
      data.found = false;
      return { steps, summary: "Node start/end tidak valid.", data };
    }

    if (graph.getEdges().some((edge) => edge.weight < 0)) {
      data.found = false;
      return { steps, summary: "Dijkstra tidak mendukung bobot negatif.", data };
    }

    steps.push(Step.markStart(startNode, `Node awal: ${startNode}`));
    steps.push(Step.markEnd(endNode, `Node tujuan: ${endNode}`));

    if (startNode === endNode) {
      const path = [startNode];
      data.found = true;
      data.path = path;
      data.shortestPath = path;
      data.distance = 0;
      data.shortestDistance = 0;
      return { steps, summary: `Lintasan terpendek: ${path} (total bobot = 0)`, data };
    }

    const dist = new Map<number, number>();
    const parent = new Map<number, number>();
    graph.getNodeIds().forEach((id) => {
      dist.set(id, Number.POSITIVE_INFINITY);
      parent.set(id, -1);
    });
    dist.set(startNode, 0);

    const pq: { nodeId: number; distance: number }[] = [{ nodeId: startNode, distance: 0 }];

    const popMin = () => {
      pq.sort((a, b) => a.distance - b.distance);
      return pq.shift();
    };

    while (pq.length) {
      const current = popMin();
      if (!current) break;
      const u = current.nodeId;
      const known = dist.get(u) ?? Number.POSITIVE_INFINITY;
      if (current.distance > known + EPS) continue;

      steps.push(Step.processNode(u, `Memproses node ${u} (jarak ${formatWeight(known)})`));
      if (u === endNode) {
        steps.push(Step.log(`Node tujuan ${endNode} dipilih dari priority queue.`));
        steps.push(Step.finishNode(u, `Selesai memproses node ${u}`));
        break;
      }

      for (const v of graph.getNeighbors(u)) {
        const edge: GraphEdge | undefined = graph.getEdge(u, v);
        if (!edge) continue;
        const candidate = known + edge.weight;
        steps.push(Step.traverseEdge(u, v, `Relaksasi edge ${u} -> ${v} (w=${formatWeight(edge.weight)})`));
        if (candidate + EPS < (dist.get(v) ?? Number.POSITIVE_INFINITY)) {
          dist.set(v, candidate);
          parent.set(v, u);
          pq.push({ nodeId: v, distance: candidate });
          steps.push(Step.log(`Update jarak node ${v} = ${formatWeight(candidate)} lewat ${u}`));
        } else {
          steps.push(Step.log(`Tidak ada update untuk node ${v}.`));
        }
        steps.push(Step.finishEdge(u, v, ""));
      }

      steps.push(Step.finishNode(u, `Selesai memproses node ${u}`));
    }

    const shortestDistance = dist.get(endNode) ?? Number.POSITIVE_INFINITY;
    if (!Number.isFinite(shortestDistance)) {
      data.found = false;
      data.distance = Number.POSITIVE_INFINITY;
      data.shortestDistance = Number.POSITIVE_INFINITY;
      return { steps, summary: `Tidak ada lintasan dari ${startNode} ke ${endNode}.`, data };
    }

    const path: number[] = [];
    let cursor = endNode;
    while (cursor !== -1) {
      path.push(cursor);
      if (cursor === startNode) break;
      cursor = parent.get(cursor) ?? -1;
    }
    path.reverse();

    path.forEach((node, index) => {
      steps.push(Step.markPathNode(node, `Node lintasan: ${node}`));
      if (index > 0) steps.push(Step.markPathEdge(path[index - 1], node, ""));
    });

    data.found = true;
    data.path = [...path];
    data.shortestPath = [...path];
    data.distance = shortestDistance;
    data.shortestDistance = shortestDistance;
    data.distanceFromStart = Object.fromEntries([...dist.entries()].sort(([a], [b]) => a - b));

    const summary = `Lintasan terpendek: ${path.join(" -> ")} (Total bobot = ${formatWeight(shortestDistance)})`;
    return { steps, summary, data };
  }
};
