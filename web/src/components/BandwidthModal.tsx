"use client";

import { Graph } from "@/lib/graph/graph";
import GraphCanvas, { GraphCanvasHandle } from "@/components/GraphCanvas";
import { useEffect, useMemo, useRef } from "react";
import { cloneGraph, formatNodeId, formatWeight } from "@/lib/graph/utils";

interface Props {
  open: boolean;
  onClose: () => void;
  data: Record<string, unknown>;
  graph: Graph;
}

export default function BandwidthModal({ open, onClose, data, graph }: Props) {
  const beforeOrder = data.bandwidthOrderBefore as number[] | undefined;
  const afterOrder = data.bandwidthOrderAfter as number[] | undefined;
  const bwBefore = typeof data.bandwidthBefore === "number" ? data.bandwidthBefore : -1;
  const bwAfter = typeof data.bandwidthAfter === "number" ? data.bandwidthAfter : -1;
  const method = typeof data.bandwidthMethod === "string" ? data.bandwidthMethod : "Heuristic";

  const beforeCanvasRef = useRef<GraphCanvasHandle | null>(null);
  const afterCanvasRef = useRef<GraphCanvasHandle | null>(null);

  const beforeGraph = useMemo(() => (graph ? cloneGraph(graph) : new Graph()), [graph]);
  const afterGraph = useMemo(() => (graph ? cloneGraph(graph) : new Graph()), [graph]);

  useEffect(() => {
    if (!open) return;
    beforeCanvasRef.current?.startLayout();
    afterCanvasRef.current?.startLayout();
  }, [open]);

  if (!open || !beforeOrder || !afterOrder) return null;

  const orderLabelMap = new Map<number, number>();
  afterOrder.forEach((id, index) => orderLabelMap.set(id, index));
  afterGraph.getNodes().forEach((node) => {
    const label = orderLabelMap.get(node.id);
    if (label !== undefined) node.label = String(label);
  });

  const mappingText = beforeOrder
    .map((id) => `${formatNodeId(id, graph)} -> ${orderLabelMap.get(id) ?? "-"}`)
    .join("\n");

  const renderMatrix = (order: number[]) => {
    return (
      <div className="overflow-auto rounded-lg border border-border bg-white p-2 text-xs">
        <table className="border-collapse">
          <thead>
            <tr>
              <th className="border border-border bg-panel-soft px-2 py-1"></th>
              {order.map((_, idx) => (
                <th key={`h-${idx}`} className="border border-border bg-panel-soft px-2 py-1">
                  {idx}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {order.map((rowId, i) => (
              <tr key={`r-${rowId}`}>
                <td className="border border-border bg-panel-soft px-2 py-1 font-semibold">{i}</td>
                {order.map((colId, j) => {
                  const hasEdge = graph.getEdge(rowId, colId) != null;
                  const text = i === j ? "0" : hasEdge ? "1" : "";
                  return (
                    <td
                      key={`c-${rowId}-${colId}`}
                      className={`border border-border px-2 py-1 text-center ${
                        i === j
                          ? "bg-panel-soft text-inkMuted"
                          : hasEdge
                          ? "bg-blue-50 text-blue-900 font-semibold"
                          : ""
                      }`}
                    >
                      {text}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="max-h-[90vh] w-full max-w-5xl overflow-auto rounded-2xl bg-panel p-6 shadow-panel">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-semibold text-ink">Graph Bandwidth Comparison</h2>
            <p className="text-xs text-inkMuted">
              Bandwidth before: {bwBefore} | after: {bwAfter} ({method})
            </p>
          </div>
          <button className="rounded-md border border-border px-3 py-1 text-xs" onClick={onClose}>
            Close
          </button>
        </div>

        <div className="mt-4 grid gap-4 md:grid-cols-2">
          <div className="rounded-xl border border-border bg-panel-soft p-3">
            <p className="text-sm font-semibold text-accent">Before</p>
            <div className="h-52 overflow-hidden rounded-lg border border-border bg-white">
              <GraphCanvas
                ref={beforeCanvasRef}
                graphRef={{ current: beforeGraph }}
                graphVersion={0}
                fixedCoordinateMode={false}
                mode="select"
                showEdgeWeights={false}
                onGraphChange={() => {}}
              />
            </div>
            <p className="mt-2 text-xs text-inkMuted">Bandwidth: {bwBefore}</p>
            {renderMatrix(beforeOrder)}
          </div>

          <div className="rounded-xl border border-border bg-panel-soft p-3">
            <p className="text-sm font-semibold text-accent">After</p>
            <div className="h-52 overflow-hidden rounded-lg border border-border bg-white">
              <GraphCanvas
                ref={afterCanvasRef}
                graphRef={{ current: afterGraph }}
                graphVersion={0}
                fixedCoordinateMode={false}
                mode="select"
                showEdgeWeights={false}
                onGraphChange={() => {}}
              />
            </div>
            <p className="mt-2 text-xs text-inkMuted">Bandwidth: {bwAfter}</p>
            {renderMatrix(afterOrder)}
          </div>
        </div>

        <div className="mt-4">
          <p className="text-xs font-semibold uppercase text-inkMuted">Label mapping</p>
          <textarea
            className="mt-2 w-full rounded-lg border border-border bg-panel-soft p-2 font-mono text-xs"
            rows={4}
            value={mappingText}
            readOnly
          />
        </div>
      </div>
    </div>
  );
}
