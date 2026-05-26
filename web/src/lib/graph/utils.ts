import { Graph, GraphEdge, GraphNode } from "@/lib/graph/graph";

export const formatNumber = (value: number) => {
  if (Math.abs(value - Math.round(value)) < 1e-9) return String(Math.round(value));
  return value.toFixed(2);
};

export const formatWeight = (value: number) => {
  if (!Number.isFinite(value)) return "INF";
  if (Math.abs(value - Math.round(value)) < 1e-9) return String(Math.round(value));
  return value.toFixed(2);
};

export const formatNodeId = (nodeId: number, graph: Graph) => {
  const node = graph.getNode(nodeId);
  return node?.label ?? String(nodeId);
};

export const cloneGraph = (source: Graph) => {
  const copy = new Graph(source.directed);
  copy.weighted = source.weighted;
  source.getNodes().forEach((node) => {
    const cloned = new GraphNode(node.id, node.label);
    copy.addNode(cloned);
  });
  source.getEdges().forEach((edge) => {
    if (source.weighted) copy.addEdge(edge.source, edge.target, edge.weight);
    else copy.addEdge(edge.source, edge.target);
  });
  return copy;
};

export const safeNodeList = (value: unknown): number[] | null => {
  return Array.isArray(value) ? (value as number[]) : null;
};

export const safeEdgeList = (value: unknown): number[][] | null => {
  return Array.isArray(value) ? (value as number[][]) : null;
};

export const safeTimetable = (value: unknown): number[][][] | null => {
  return Array.isArray(value) ? (value as number[][][]) : null;
};
