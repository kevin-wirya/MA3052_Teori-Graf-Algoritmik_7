import { Graph, GraphNode } from "@/lib/graph/graph";

export const generateCompleteGraphKn = (n: number) => {
  const edges: number[][] = [];
  for (let u = 0; u < n; u++) {
    for (let v = u + 1; v < n; v++) {
      edges.push([u, v]);
    }
  }
  return toEdgeListText(n, edges);
};

export const generateCompleteBipartiteKmn = (m: number, n: number) => {
  const edges: number[][] = [];
  const offset = m;
  for (let u = 0; u < m; u++) {
    for (let v = 0; v < n; v++) {
      edges.push([u, offset + v]);
    }
  }
  return toEdgeListText(m + n, edges);
};

export const generateTree = (n: number) => {
  const edges: number[][] = [];
  for (let child = 1; child < n; child++) {
    const parent = Math.floor((child - 1) / 2);
    edges.push([parent, child]);
  }
  return toEdgeListText(n, edges);
};

export const generateCycleCn = (n: number) => {
  const edges: number[][] = [];
  for (let u = 0; u < n; u++) {
    edges.push([u, (u + 1) % n]);
  }
  return toEdgeListText(n, edges);
};

export const generatePathPn = (n: number) => {
  const edges: number[][] = [];
  for (let u = 0; u < n - 1; u++) {
    edges.push([u, u + 1]);
  }
  return toEdgeListText(n, edges);
};

export const generateWheelWn = (n: number) => {
  const edges: number[][] = [];
  const rim = n - 1;
  const center = n - 1;
  for (let u = 0; u < rim; u++) {
    const v = (u + 1) % rim;
    edges.push([u, v]);
    edges.push([center, u]);
  }
  return toEdgeListText(n, edges);
};

export const generatePrismGraph = (n: number) => {
  const edges: number[][] = [];
  const offset = n;
  const total = 2 * n;
  for (let u = 0; u < n; u++) {
    const next = (u + 1) % n;
    edges.push([u, next]);
    edges.push([offset + u, offset + next]);
    edges.push([u, offset + u]);
  }
  return toEdgeListText(total, edges);
};

export const generateGeneralizedPetersen = (n: number, k: number) => {
  const edges: number[][] = [];
  const seen = new Set<string>();
  const offset = n;
  for (let i = 0; i < n; i++) {
    addUndirectedEdge(edges, seen, i, (i + 1) % n);
    addUndirectedEdge(edges, seen, i, offset + i);
    addUndirectedEdge(edges, seen, offset + i, offset + ((i + k) % n));
  }
  return toEdgeListText(2 * n, edges);
};

export const generateCirculantGraph = (n: number, a1: number, a2: number) => {
  const edges: number[][] = [];
  const seen = new Set<string>();
  const jumps = [a1, a2];
  for (let u = 0; u < n; u++) {
    for (const jump of jumps) {
      addUndirectedEdge(edges, seen, u, (u + jump) % n);
    }
  }
  return toEdgeListText(n, edges);
};

export const generateHypercube = (dimension: number) => {
  const edges: number[][] = [];
  const n = 1 << dimension;
  for (let u = 0; u < n; u++) {
    for (let bit = 0; bit < dimension; bit++) {
      const v = u ^ (1 << bit);
      if (u < v) edges.push([u, v]);
    }
  }
  return toEdgeListText(n, edges);
};

export const generateGridGraph = (rows: number, cols: number) => {
  const edges: number[][] = [];
  const n = rows * cols;
  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      const u = r * cols + c;
      if (c + 1 < cols) edges.push([u, u + 1]);
      if (r + 1 < rows) edges.push([u, u + cols]);
    }
  }
  return toEdgeListText(n, edges);
};

export const applyCompleteGraphLayout = (graph: Graph) => {
  applyCycleLayout(graph);
};

export const applyCompleteBipartiteLayout = (graph: Graph, m: number, n: number) => {
  const upper = Array.from({ length: m }, (_, i) => i);
  const lower = Array.from({ length: n }, (_, i) => m + i);
  layoutNodesOnLine(graph, upper, 120, 820, 200);
  layoutNodesOnLine(graph, lower, 120, 820, 520);
};

export const applyTreeLayout = (graph: Graph, nodeCount: number) => {
  if (nodeCount <= 0) return;
  const topY = 100;
  const bottomY = 620;
  const maxDepth = estimateTreeDepth(nodeCount);
  const minX = 80;
  const maxX = 860;
  const totalWidth = maxX - minX;

  for (let id = 0; id < nodeCount; id++) {
    const level = Math.floor(Math.log(id + 1) / Math.log(2));
    const nodesInLevel = 1 << level;
    const posInLevel = id - (nodesInLevel - 1);
    const y = maxDepth <= 1 ? topY : topY + ((bottomY - topY) * level) / (maxDepth - 1);
    const x = minX + (posInLevel + 0.5) * (totalWidth / nodesInLevel);
    setNodePosition(graph, id, x, y);
  }
};

const estimateTreeDepth = (nodeCount: number) => {
  let depth = 0;
  let covered = 0;
  let nodesInLevel = 1;
  while (covered < nodeCount) {
    covered += nodesInLevel;
    nodesInLevel <<= 1;
    depth += 1;
  }
  return Math.max(1, depth);
};

export const applyCycleLayout = (graph: Graph) => {
  const ids = [...graph.getNodeIds()].sort((a, b) => a - b);
  layoutNodesOnCircle(graph, ids, 460, 340, 250, -Math.PI / 2);
};

export const applyPathLayout = (graph: Graph) => {
  const ids = [...graph.getNodeIds()].sort((a, b) => a - b);
  if (ids.length === 0) return;

  const columns = Math.min(10, Math.max(2, ids.length));
  const startX = 100;
  const startY = 140;
  const stepX = 85;
  const stepY = 95;

  ids.forEach((id, i) => {
    const row = Math.floor(i / columns);
    let col = i % columns;
    if (row % 2 === 1) col = columns - 1 - col;
    setNodePosition(graph, id, startX + col * stepX, startY + row * stepY);
  });
};

export const applyWheelLayout = (graph: Graph, n: number) => {
  if (n < 4) {
    applyCycleLayout(graph);
    return;
  }
  const centerId = n - 1;
  setNodePosition(graph, centerId, 460, 340);
  const rimIds = Array.from({ length: n - 1 }, (_, i) => i);
  layoutNodesOnCircle(graph, rimIds, 460, 340, 250, -Math.PI / 2);
};

export const applyPrismLayout = (graph: Graph, n: number) => {
  const top = Array.from({ length: n }, (_, i) => i);
  const bottom = Array.from({ length: n }, (_, i) => i + n);
  layoutNodesOnCircle(graph, top, 410, 280, 170, -Math.PI / 2);
  layoutNodesOnCircle(graph, bottom, 530, 400, 170, -Math.PI / 2);
};

export const applyGeneralizedPetersenLayout = (graph: Graph, n: number) => {
  const outer = Array.from({ length: n }, (_, i) => i);
  const inner = Array.from({ length: n }, (_, i) => i + n);
  layoutNodesOnCircle(graph, outer, 460, 340, 250, -Math.PI / 2);
  layoutNodesOnCircle(graph, inner, 460, 340, 130, -Math.PI / 2);
};

export const applyCirculantLayout = (graph: Graph) => {
  const ids = [...graph.getNodeIds()].sort((a, b) => a - b);
  layoutNodesOnCircle(graph, ids, 460, 340, 250, -Math.PI / 2);
};

export const applyHypercubeLayout = (graph: Graph, dimension: number) => {
  if (dimension <= 0) return;
  if (dimension === 1) {
    setNodePosition(graph, 0, 320, 340);
    setNodePosition(graph, 1, 600, 340);
    return;
  }
  if (dimension === 2) {
    applySquare(graph, 0, 460, 340, 300);
    return;
  }
  if (dimension === 3) {
    applyCube(graph, 0, 450, 350, 230, 75, 55);
    return;
  }
  if (dimension === 4) {
    applyCube(graph, 0, 360, 300, 170, 55, 40);
    applyCube(graph, 8, 560, 430, 170, 55, 40);
    return;
  }
  applyProjectedHypercube(graph, dimension);
};

const applySquare = (graph: Graph, baseId: number, cx: number, cy: number, side: number) => {
  const h = side / 2;
  setNodePosition(graph, baseId, cx - h, cy - h);
  setNodePosition(graph, baseId + 1, cx - h, cy + h);
  setNodePosition(graph, baseId + 3, cx + h, cy + h);
  setNodePosition(graph, baseId + 2, cx + h, cy - h);
};

const applyCube = (graph: Graph, baseId: number, cx: number, cy: number, side: number, dx: number, dy: number) => {
  applySquare(graph, baseId, cx, cy, side);
  const h = side / 2;
  setNodePosition(graph, baseId + 4, cx - h + dx, cy - h - dy);
  setNodePosition(graph, baseId + 5, cx - h + dx, cy + h - dy);
  setNodePosition(graph, baseId + 7, cx + h + dx, cy + h - dy);
  setNodePosition(graph, baseId + 6, cx + h + dx, cy - h - dy);
};

const applyProjectedHypercube = (graph: Graph, dimension: number) => {
  const nodeCount = 1 << dimension;
  const xs = Array(nodeCount).fill(0);
  const ys = Array(nodeCount).fill(0);
  let maxRadius = 1e-9;

  for (let id = 0; id < nodeCount; id++) {
    let x = 0;
    let y = 0;
    for (let bit = 0; bit < dimension; bit++) {
      if ((id & (1 << bit)) !== 0) {
        const angle = -Math.PI / 2 + (2 * Math.PI * bit) / dimension;
        const weight = 1 + 0.18 * bit;
        x += weight * Math.cos(angle);
        y += weight * Math.sin(angle);
      }
    }
    xs[id] = x;
    ys[id] = y;
    maxRadius = Math.max(maxRadius, Math.max(Math.abs(x), Math.abs(y)));
  }

  const cx = 460;
  const cy = 340;
  const scale = 260 / maxRadius;
  for (let id = 0; id < nodeCount; id++) {
    setNodePosition(graph, id, cx + xs[id] * scale, cy + ys[id] * scale);
  }
};

export const applyGridLayout = (graph: Graph, rows: number, cols: number) => {
  if (rows <= 0 || cols <= 0) return;
  const startX = 100;
  const startY = 100;
  const stepX = Math.min(130, 720 / Math.max(1, cols - 1));
  const stepY = Math.min(130, 520 / Math.max(1, rows - 1));
  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      const id = r * cols + c;
      setNodePosition(graph, id, startX + c * stepX, startY + r * stepY);
    }
  }
};

const layoutNodesOnLine = (graph: Graph, ids: number[], minX: number, maxX: number, y: number) => {
  if (!ids.length) return;
  ids.forEach((id, index) => {
    const x = ids.length === 1 ? (minX + maxX) / 2 : minX + ((maxX - minX) * index) / (ids.length - 1);
    setNodePosition(graph, id, x, y);
  });
};

const layoutNodesOnCircle = (
  graph: Graph,
  ids: number[],
  cx: number,
  cy: number,
  radius: number,
  startAngle: number
) => {
  if (!ids.length) return;
  ids.forEach((id, i) => {
    const angle = startAngle + (2 * Math.PI * i) / ids.length;
    setNodePosition(graph, id, cx + radius * Math.cos(angle), cy + radius * Math.sin(angle));
  });
};

const setNodePosition = (graph: Graph, nodeId: number, x: number, y: number) => {
  const node = graph.getNode(nodeId);
  if (!node) return;
  node.x = x;
  node.y = y;
  node.vx = 0;
  node.vy = 0;
};

const addUndirectedEdge = (edges: number[][], seen: Set<string>, u: number, v: number) => {
  const a = Math.min(u, v);
  const b = Math.max(u, v);
  const key = `${a}:${b}`;
  if (seen.has(key)) return;
  seen.add(key);
  edges.push([a, b]);
};

const toEdgeListText = (nodeCount: number, edges: number[][]) => {
  let text = `${nodeCount} ${edges.length}`;
  for (const edge of edges) {
    text += `\n${edge[0]} ${edge[1]}`;
  }
  return text;
};
