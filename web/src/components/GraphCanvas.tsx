"use client";

import React, { forwardRef, useEffect, useImperativeHandle, useRef } from "react";
import {
  EdgeStateKey,
  Graph,
  GraphEdge,
  GraphNode,
  NodeStateKey,
  edgeStateStyles,
  nodeStateStyles
} from "@/lib/graph/graph";
import { ForceDirectedLayout } from "@/lib/graph/layout";
import { formatNumber } from "@/lib/graph/utils";

export type InteractionMode = "select" | "add-node" | "add-edge" | "delete";

export interface GraphCanvasHandle {
  startLayout: () => void;
  stopLayout: () => void;
  resetLayout: () => void;
  draw: () => void;
}

interface Props {
  graphRef: React.MutableRefObject<Graph>;
  graphVersion: number;
  fixedCoordinateMode: boolean;
  mode: InteractionMode;
  showEdgeWeights: boolean;
  onGraphChange: () => void;
}

const GraphCanvas = forwardRef<GraphCanvasHandle, Props>(function GraphCanvas(
  { graphRef, graphVersion, fixedCoordinateMode, mode, showEdgeWeights, onGraphChange },
  ref
) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const layoutRef = useRef<ForceDirectedLayout | null>(null);
  const edgeStartRef = useRef<GraphNode | null>(null);
  const draggingRef = useRef<GraphNode | null>(null);
  const panningRef = useRef(false);
  const lastMouseRef = useRef({ x: 0, y: 0 });
  const zoomRef = useRef(1);
  const panRef = useRef({ x: 0, y: 0 });

  const ensureLayout = () => {
    if (!layoutRef.current || layoutRef.current.getGraph() !== graphRef.current) {
      layoutRef.current?.stop();
      layoutRef.current = new ForceDirectedLayout(graphRef.current);
      layoutRef.current.onTick = () => {
        draw();
      };
    }
  };

  const drawGrid = (ctx: CanvasRenderingContext2D, width: number, height: number) => {
    const zoom = zoomRef.current;
    const gridSize = 40 * zoom;
    if (gridSize < 10) return;
    const { x: panX, y: panY } = panRef.current;
    ctx.strokeStyle = "#edf0f2";
    ctx.lineWidth = 0.5;
    const offX = panX % gridSize;
    const offY = panY % gridSize;
    for (let x = offX; x < width; x += gridSize) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, height);
      ctx.stroke();
    }
    for (let y = offY; y < height; y += gridSize) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(width, y);
      ctx.stroke();
    }
  };

  const drawPlaceholder = (ctx: CanvasRenderingContext2D, width: number, height: number) => {
    ctx.fillStyle = "#bdbdbd";
    ctx.font = "14px var(--font-sans)";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("Masukkan graf di panel kanan untuk memulai visualisasi", width / 2, height / 2 - 8);
    ctx.fillStyle = "#d9d9d9";
    ctx.font = "12px var(--font-sans)";
    ctx.fillText("Atau gunakan mode + Node dan + Edge untuk membuat graf", width / 2, height / 2 + 12);
  };

  const drawArrow = (
    ctx: CanvasRenderingContext2D,
    x1: number,
    y1: number,
    x2: number,
    y2: number,
    color: string,
    nodeRadius: number
  ) => {
    const angle = Math.atan2(y2 - y1, x2 - x1);
    const len = 12;
    const spread = (25 * Math.PI) / 180;
    const dist = Math.hypot(x2 - x1, y2 - y1);
    const ratio = Math.max(0, (dist - nodeRadius - 2) / dist);
    const px = x1 + (x2 - x1) * ratio;
    const py = y1 + (y2 - y1) * ratio;
    const ax1 = px - len * Math.cos(angle - spread);
    const ay1 = py - len * Math.sin(angle - spread);
    const ax2 = px - len * Math.cos(angle + spread);
    const ay2 = py - len * Math.sin(angle + spread);
    ctx.fillStyle = color;
    ctx.beginPath();
    ctx.moveTo(px, py);
    ctx.lineTo(ax1, ay1);
    ctx.lineTo(ax2, ay2);
    ctx.closePath();
    ctx.fill();
  };

  const drawEdge = (
    ctx: CanvasRenderingContext2D,
    graph: Graph,
    edge: GraphEdge,
    nodeRadius: number
  ) => {
    const src = graph.getNode(edge.source);
    const tgt = graph.getNode(edge.target);
    if (!src || !tgt) return;

    const style = edgeStateStyles[edge.state as EdgeStateKey];
    ctx.strokeStyle = style.color;
    ctx.lineWidth = style.width;
    ctx.setLineDash([]);
    ctx.beginPath();
    ctx.moveTo(src.x, src.y);
    ctx.lineTo(tgt.x, tgt.y);
    ctx.stroke();

    if (graph.directed) {
      drawArrow(ctx, src.x, src.y, tgt.x, tgt.y, style.color, nodeRadius);
    }

    if (graph.weighted && showEdgeWeights) {
      const mx = (src.x + tgt.x) / 2;
      const my = (src.y + tgt.y) / 2 - 10;
      const wText = formatNumber(edge.weight);
      const tw = wText.length * 7 + 8;
      ctx.fillStyle = "rgba(255,255,255,0.85)";
      ctx.fillRect(mx - tw / 2, my - 8, tw, 16);
      ctx.strokeStyle = "rgba(211,47,47,0.5)";
      ctx.lineWidth = 1;
      ctx.strokeRect(mx - tw / 2, my - 8, tw, 16);
      ctx.fillStyle = "#d32f2f";
      ctx.font = "bold 11px var(--font-sans)";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText(wText, mx, my);
    }
  };

  const drawNode = (ctx: CanvasRenderingContext2D, node: GraphNode, nodeRadius: number) => {
    const style = nodeStateStyles[node.state as NodeStateKey];

    ctx.fillStyle = "rgba(0,0,0,0.1)";
    ctx.beginPath();
    ctx.ellipse(node.x + 2, node.y + 3, nodeRadius, nodeRadius, 0, 0, Math.PI * 2);
    ctx.fill();

    const gradient = ctx.createRadialGradient(
      node.x - nodeRadius * 0.2,
      node.y - nodeRadius * 0.2,
      nodeRadius * 0.2,
      node.x,
      node.y,
      nodeRadius * 1.2
    );
    gradient.addColorStop(0, style.fillLighter);
    gradient.addColorStop(1, style.fill);
    ctx.fillStyle = gradient;
    ctx.beginPath();
    ctx.ellipse(node.x, node.y, nodeRadius, nodeRadius, 0, 0, Math.PI * 2);
    ctx.fill();

    ctx.strokeStyle = style.stroke;
    ctx.lineWidth = 2.5;
    ctx.stroke();

    ctx.fillStyle = "#212121";
    const fontSize = nodeRadius < 15 ? Math.max(8, nodeRadius * 0.7) : 13;
    ctx.font = `bold ${fontSize}px var(--font-sans)`;
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText(node.label ?? String(node.id), node.x, node.y + 1);

    if (fixedCoordinateMode && node.hasCoordinate) {
      ctx.fillStyle = "#616161";
      ctx.font = "10px var(--font-mono)";
      ctx.textBaseline = "top";
      ctx.fillText(
        `(${formatNumber(node.coordinateX)}, ${formatNumber(node.coordinateY)})`,
        node.x,
        node.y + nodeRadius + 5
      );
    }
  };

  const draw = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const width = canvas.clientWidth;
    const height = canvas.clientHeight;
    const dpr = window.devicePixelRatio || 1;
    canvas.width = width * dpr;
    canvas.height = height * dpr;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    ctx.fillStyle = "#f8f9fa";
    ctx.fillRect(0, 0, width, height);
    drawGrid(ctx, width, height);

    const graph = graphRef.current;
    if (!graph || graph.nodeCount === 0) {
      drawPlaceholder(ctx, width, height);
      return;
    }

    const nodes = graph.getNodes();
    let nodeRadius = 22;
    if (nodes.length > 1 && !fixedCoordinateMode) {
      let minSqDist = Infinity;
      for (let i = 0; i < nodes.length; i++) {
        for (let j = i + 1; j < nodes.length; j++) {
          const dx = nodes[i].x - nodes[j].x;
          const dy = nodes[i].y - nodes[j].y;
          const sqDist = dx * dx + dy * dy;
          if (sqDist < minSqDist) minSqDist = sqDist;
        }
      }
      const minDist = Math.sqrt(minSqDist);
      nodeRadius = Math.max(8, Math.min(22, minDist * 0.4));
    }

    ctx.save();
    ctx.translate(panRef.current.x, panRef.current.y);
    ctx.scale(zoomRef.current, zoomRef.current);

    graph.getEdges().forEach((edge) => drawEdge(ctx, graph, edge, nodeRadius));
    graph.getNodes().forEach((node) => drawNode(ctx, node, nodeRadius));

    if (edgeStartRef.current) {
      const node = edgeStartRef.current;
      ctx.strokeStyle = "#90caf9";
      ctx.lineWidth = 2;
      ctx.setLineDash([8, 4]);
      ctx.beginPath();
      ctx.ellipse(node.x, node.y, nodeRadius + 4, nodeRadius + 4, 0, 0, Math.PI * 2);
      ctx.stroke();
      ctx.setLineDash([]);
    }

    ctx.restore();
  };

  useImperativeHandle(ref, () => ({
    startLayout: () => {
      if (fixedCoordinateMode) return;
      ensureLayout();
      layoutRef.current?.setDimensions(getCanvasSize());
      layoutRef.current?.start();
    },
    stopLayout: () => {
      layoutRef.current?.stop();
    },
    resetLayout: () => {
      if (fixedCoordinateMode) {
        fitCoordinatesToCanvas();
      } else {
        ensureLayout();
        layoutRef.current?.setDimensions(getCanvasSize());
        layoutRef.current?.circularLayout();
      }
      zoomRef.current = 1;
      panRef.current = { x: 0, y: 0 };
      draw();
    },
    draw
  }));

  const getCanvasSize = () => {
    const canvas = canvasRef.current;
    if (!canvas) return { width: 600, height: 400 };
    return { width: canvas.clientWidth, height: canvas.clientHeight };
  };

  const toGraphX = (sx: number) => (sx - panRef.current.x) / zoomRef.current;
  const toGraphY = (sy: number) => (sy - panRef.current.y) / zoomRef.current;

  const findNodeAt = (gx: number, gy: number) => {
    const graph = graphRef.current;
    const radius = 27;
    const r2 = radius * radius;
    for (const node of graph.getNodes()) {
      const dx = node.x - gx;
      const dy = node.y - gy;
      if (dx * dx + dy * dy <= r2) return node;
    }
    return null;
  };

  const fitCoordinatesToCanvas = () => {
    const graph = graphRef.current;
    if (!graph || graph.nodeCount === 0) return;

    const { width, height } = getCanvasSize();
    const margin = 80;
    const drawWidth = Math.max(40, width - 2 * margin);
    const drawHeight = Math.max(40, height - 2 * margin);

    let minX = Infinity;
    let maxX = -Infinity;
    let minY = Infinity;
    let maxY = -Infinity;

    graph.getNodes().forEach((node) => {
      const rawX = node.hasCoordinate ? node.coordinateX : node.x;
      const rawY = node.hasCoordinate ? node.coordinateY : node.y;
      minX = Math.min(minX, rawX);
      maxX = Math.max(maxX, rawX);
      minY = Math.min(minY, rawY);
      maxY = Math.max(maxY, rawY);
    });

    const spanX = maxX - minX;
    const spanY = maxY - minY;

    graph.getNodes().forEach((node) => {
      const rawX = node.hasCoordinate ? node.coordinateX : node.x;
      const rawY = node.hasCoordinate ? node.coordinateY : node.y;
      const normalizedX = spanX < 1e-9 ? 0.5 : (rawX - minX) / spanX;
      const normalizedY = spanY < 1e-9 ? 0.5 : (rawY - minY) / spanY;
      node.x = margin + normalizedX * drawWidth;
      node.y = height - margin - normalizedY * drawHeight;
      node.vx = 0;
      node.vy = 0;
      if (node.hasCoordinate) node.pinned = true;
    });
  };

  useEffect(() => {
    ensureLayout();
    if (fixedCoordinateMode) {
      fitCoordinatesToCanvas();
    } else {
      const graph = graphRef.current;
      const shouldLayout = graph.getNodes().every((node) => node.x === 0 && node.y === 0);
      if (shouldLayout) {
        layoutRef.current?.circularLayout();
      }
    }
    draw();
  }, [graphVersion, fixedCoordinateMode]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const handleMouseDown = (event: MouseEvent) => {
      const rect = canvas.getBoundingClientRect();
      const mx = toGraphX(event.clientX - rect.left);
      const my = toGraphY(event.clientY - rect.top);
      lastMouseRef.current = { x: event.clientX - rect.left, y: event.clientY - rect.top };

      const hit = findNodeAt(mx, my);
      if (mode === "select") {
        if (hit) {
          draggingRef.current = hit;
          hit.pinned = true;
        } else {
          panningRef.current = true;
        }
        return;
      }

      if (mode === "add-node") {
        if (!hit) {
          const graph = graphRef.current;
          const newId = graph.nextAvailableId();
          const node = new GraphNode(newId);
          node.x = mx;
          node.y = my;
          graph.addNode(node);
          onGraphChange();
          draw();
        }
        return;
      }

      if (mode === "add-edge") {
        if (hit) {
          if (!edgeStartRef.current) {
            edgeStartRef.current = hit;
            draw();
          } else if (edgeStartRef.current.id !== hit.id) {
            const graph = graphRef.current;
            if (graph.weighted) {
              const raw = window.prompt(
                `Weight for edge ${edgeStartRef.current.id} -> ${hit.id}:`,
                "1"
              );
              const weight = raw ? Number(raw) : NaN;
              if (Number.isFinite(weight)) {
                graph.addEdge(edgeStartRef.current.id, hit.id, weight);
              } else {
                graph.addEdge(edgeStartRef.current.id, hit.id);
              }
            } else {
              graph.addEdge(edgeStartRef.current.id, hit.id);
            }
            edgeStartRef.current = null;
            onGraphChange();
            draw();
          }
        }
        return;
      }

      if (mode === "delete") {
        if (hit) {
          graphRef.current.removeNode(hit.id);
          onGraphChange();
          draw();
        }
      }
    };

    const handleMouseMove = (event: MouseEvent) => {
      const rect = canvas.getBoundingClientRect();
      const mx = event.clientX - rect.left;
      const my = event.clientY - rect.top;

      if (draggingRef.current) {
        draggingRef.current.x = toGraphX(mx);
        draggingRef.current.y = toGraphY(my);
        draggingRef.current.vx = 0;
        draggingRef.current.vy = 0;
        if (!layoutRef.current?.isRunning()) draw();
        return;
      }

      if (panningRef.current) {
        panRef.current = {
          x: panRef.current.x + (mx - lastMouseRef.current.x),
          y: panRef.current.y + (my - lastMouseRef.current.y)
        };
        lastMouseRef.current = { x: mx, y: my };
        if (!layoutRef.current?.isRunning()) draw();
      }
    };

    const handleMouseUp = () => {
      if (draggingRef.current) {
        const node = draggingRef.current;
        node.pinned = fixedCoordinateMode && node.hasCoordinate;
      }
      draggingRef.current = null;
      panningRef.current = false;
    };

    const handleWheel = (event: WheelEvent) => {
      event.preventDefault();
      const rect = canvas.getBoundingClientRect();
      const x = event.clientX - rect.left;
      const y = event.clientY - rect.top;
      const factor = event.deltaY < 0 ? 1.1 : 1 / 1.1;
      const oldZoom = zoomRef.current;
      zoomRef.current = Math.max(0.1, Math.min(5, zoomRef.current * factor));
      panRef.current = {
        x: x - (x - panRef.current.x) * (zoomRef.current / oldZoom),
        y: y - (y - panRef.current.y) * (zoomRef.current / oldZoom)
      };
      if (!layoutRef.current?.isRunning()) draw();
    };

    canvas.addEventListener("mousedown", handleMouseDown);
    window.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("mouseup", handleMouseUp);
    canvas.addEventListener("wheel", handleWheel, { passive: false });

    return () => {
      canvas.removeEventListener("mousedown", handleMouseDown);
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
      canvas.removeEventListener("wheel", handleWheel);
    };
  }, [mode, fixedCoordinateMode, onGraphChange, showEdgeWeights]);

  useEffect(() => {
    draw();
  }, [graphVersion, showEdgeWeights]);

  return <canvas ref={canvasRef} className="h-full w-full" />;
});

export default GraphCanvas;
