import { Graph, GraphNode } from "@/lib/graph/graph";

export interface ParseResult {
  graph: Graph;
  startVertex: number;
  fixedCoordinates: boolean;
}

export class GraphParser {
  static parseEdgeListWithStart(text: string, directed = false, weighted = false): ParseResult {
    const graph = new Graph(directed);
    graph.weighted = weighted;
    let startVertex = -1;

    const dataLines = collectDataLines(text);
    if (dataLines.length === 0) {
      return { graph, startVertex, fixedCoordinates: false };
    }

    const header = dataLines[0].split(/[\s,;]+/);
    if (header.length === 2) {
      const n = Number(header[0]);
      const m = Number(header[1]);
      if (Number.isFinite(n) && Number.isFinite(m)) {
        const remaining = dataLines.length - 1;
        if (remaining === m || remaining === m + 1) {
          for (let i = 0; i < n; i++) graph.addNode(i);
          for (let i = 1; i <= m && i < dataLines.length; i++) {
            parseEdgeLine(graph, dataLines[i], weighted);
          }
          if (remaining === m + 1) {
            const lastLine = dataLines[dataLines.length - 1];
            const lastParts = lastLine.split(/[\s,;]+/);
            if (lastParts.length === 1) {
              const maybeStart = Number(lastParts[0]);
              if (Number.isFinite(maybeStart)) startVertex = maybeStart;
            }
          }
          return { graph, startVertex, fixedCoordinates: false };
        }
      }
    }

    const labelMode = requiresLabelMapping(dataLines);
    if (labelMode) {
      const labelToId = new Map<string, number>();
      for (const line of dataLines) {
        const parts = line.split(/[\s,;]+/);
        if (parts.length >= 2) {
          if (isTimetableMetadata(parts)) continue;
          const u = getOrCreateLabelId(parts[0], labelToId, graph);
          const v = getOrCreateLabelId(parts[1], labelToId, graph);
          if (weighted && parts.length >= 3) {
            const w = Number(parts[2]);
            if (Number.isFinite(w)) graph.addEdge(u, v, w);
            else graph.addEdge(u, v);
          } else {
            graph.addEdge(u, v);
          }
        } else if (parts.length === 1) {
          if (isTimetableMetadata(parts)) continue;
          getOrCreateLabelId(parts[0], labelToId, graph);
        }
      }
      return { graph, startVertex: -1, fixedCoordinates: false };
    }

    for (const line of dataLines) {
      const parts = line.split(/[\s,;]+/);
      if (parts.length >= 2) {
        parseEdgeLine(graph, line, weighted);
      } else if (parts.length === 1) {
        const id = Number(parts[0]);
        if (Number.isFinite(id)) graph.addNode(id);
      }
    }

    return { graph, startVertex: -1, fixedCoordinates: false };
  }

  static parseTspCoordinates(text: string, hasLabels: boolean): ParseResult {
    const graph = new Graph(false);
    graph.weighted = true;

    const dataLines = collectDataLines(text);
    if (dataLines.length === 0) {
      return { graph, startVertex: -1, fixedCoordinates: true };
    }

    let startIndex = 0;
    const firstLine = dataLines[0].split(/[\s,;]+/);
    if (firstLine.length === 1 && Number.isFinite(Number(firstLine[0]))) {
      startIndex = 1;
    }

    const nodeCount = dataLines.length - startIndex;
    if (nodeCount <= 0) {
      return { graph, startVertex: -1, fixedCoordinates: true };
    }

    const orderedNodes: GraphNode[] = [];
    for (let i = 0; i < nodeCount; i++) {
      const parts = dataLines[startIndex + i].split(/[\s,;]+/);
      const minParts = hasLabels ? 3 : 2;
      if (parts.length < minParts) {
        return { graph: new Graph(false), startVertex: -1, fixedCoordinates: true };
      }

      const x = Number(parts[parts.length - 2]);
      const y = Number(parts[parts.length - 1]);
      if (!Number.isFinite(x) || !Number.isFinite(y)) {
        return { graph: new Graph(false), startVertex: -1, fixedCoordinates: true };
      }

      let label = "";
      if (hasLabels) {
        label = parts.slice(0, parts.length - 2).join(" ");
      }

      const node = new GraphNode(i, label || undefined);
      node.coordinateX = x;
      node.coordinateY = y;
      node.hasCoordinate = true;
      node.x = x;
      node.y = y;
      node.pinned = true;
      graph.addNode(node);
      orderedNodes.push(node);
    }

    for (let i = 0; i < orderedNodes.length; i++) {
      for (let j = i + 1; j < orderedNodes.length; j++) {
        const a = orderedNodes[i];
        const b = orderedNodes[j];
        const distance = Math.hypot(a.coordinateX - b.coordinateX, a.coordinateY - b.coordinateY);
        graph.addEdge(a.id, b.id, distance);
      }
    }

    return { graph, startVertex: 0, fixedCoordinates: true };
  }
}

export const isTspCoordinateFormat = (text: string) => {
  const dataLines = collectDataLines(text);
  if (dataLines.length === 0) return false;
  const firstLine = dataLines[0].split(/[\s,;]+/);
  if (firstLine.length !== 1) return false;
  const n = Number(firstLine[0]);
  if (!Number.isFinite(n) || n <= 0) return false;
  if (dataLines.length - 1 !== n) return false;

  for (let i = 1; i < dataLines.length; i++) {
    const parts = dataLines[i].split(/[\s,;]+/);
    if (parts.length < 2) return false;
    const x = Number(parts[parts.length - 2]);
    const y = Number(parts[parts.length - 1]);
    if (!Number.isFinite(x) || !Number.isFinite(y)) return false;
  }
  return true;
};

const parseEdgeLine = (graph: Graph, line: string, weighted: boolean) => {
  const parts = line.trim().split(/[\s,;]+/);
  if (parts.length < 2) return;
  const u = Number(parts[0]);
  const v = Number(parts[1]);
  if (!Number.isFinite(u) || !Number.isFinite(v)) return;
  if (weighted && parts.length >= 3) {
    const w = Number(parts[2]);
    if (Number.isFinite(w)) graph.addEdge(u, v, w);
    else graph.addEdge(u, v);
  } else {
    graph.addEdge(u, v);
  }
};

const collectDataLines = (text: string) => {
  return text
    .split("\n")
    .map((line) => line.trim())
    .filter((line) => line.length > 0 && !line.startsWith("#") && !line.startsWith("//"));
};

const requiresLabelMapping = (dataLines: string[]) => {
  for (const line of dataLines) {
    const parts = line.split(/[\s,;]+/);
    if (parts.length === 0) continue;
    if (isTimetableMetadata(parts)) continue;
    const limit = Math.min(parts.length, 2);
    for (let i = 0; i < limit; i++) {
      if (!Number.isFinite(Number(parts[i]))) {
        return true;
      }
    }
  }
  return false;
};

const isTimetableMetadata = (parts: string[]) => {
  return parts.length >= 2 && parts[0].toLowerCase() === "k" && Number.isFinite(Number(parts[1]));
};

const getOrCreateLabelId = (label: string, map: Map<string, number>, graph: Graph) => {
  if (map.has(label)) return map.get(label) ?? 0;
  const id = map.size;
  map.set(label, id);
  graph.addNode(new GraphNode(id, label));
  return id;
};
