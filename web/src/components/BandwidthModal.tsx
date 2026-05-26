"use client";

import { Graph } from "@/lib/graph/graph";
import { formatNodeId } from "@/lib/graph/utils";

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

  if (!open || !beforeOrder || !afterOrder) return null;

  const orderLabelMap = new Map<number, number>();
  afterOrder.forEach((id, index) => orderLabelMap.set(id, index + 1)); // 1-based labels

  const renderMatrix = (title: string, order: number[]) => {
    return (
      <div className="rounded-xl border border-border bg-panel-soft p-4 flex flex-col min-w-0">
        <h3 className="text-xs font-bold text-accent uppercase tracking-wider mb-3">{title} Adjacency Matrix</h3>
        <div className="overflow-auto rounded-lg border border-border bg-white p-2 text-xs">
          <table className="border-collapse mx-auto">
            <thead>
              <tr>
                <th className="border border-border bg-panel-soft px-2 py-1"></th>
                {order.map((id, idx) => (
                  <th key={`h-${idx}`} className="border border-border bg-panel-soft px-2 py-1 text-center font-mono font-bold">
                    {formatNodeId(id, graph)}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {order.map((rowId, i) => (
                <tr key={`r-${rowId}`}>
                  <td className="border border-border bg-panel-soft px-2 py-1 font-bold text-center font-mono">{formatNodeId(rowId, graph)}</td>
                  {order.map((colId, j) => {
                    const hasEdge = graph.getEdge(rowId, colId) != null;
                    const text = i === j ? "0" : hasEdge ? "1" : "";
                    return (
                      <td
                        key={`c-${rowId}-${colId}`}
                        className={`border border-border px-2.5 py-1.5 text-center font-mono ${
                          i === j
                            ? "bg-panel-soft text-inkMuted"
                            : hasEdge
                            ? "bg-emerald-50 text-emerald-800 font-bold"
                            : "text-transparent"
                        }`}
                      >
                        {text || "0"}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <div className="max-h-[90vh] w-full max-w-[95vw] xl:max-w-7xl overflow-auto rounded-2xl bg-panel p-6 shadow-panel border border-border flex flex-col">
        {/* Modal Header */}
        <div className="flex items-center justify-between border-b border-border pb-4 shrink-0">
          <div>
            <h2 className="text-lg font-bold text-ink flex items-center gap-2">
              <span>Bandwidth Optimization Results</span>
              <span className="text-xs bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded-full font-semibold">{method}</span>
            </h2>
            <p className="text-xs text-inkMuted mt-1">
              Adjacency matrix comparison for bandwidth reduction.
            </p>
          </div>
          <button
            className="rounded-xl border border-border bg-white px-3.5 py-1.5 text-xs font-bold text-ink hover:bg-panel-soft transition"
            onClick={onClose}
          >
            Close Window
          </button>
        </div>

        {/* Bandwidth Stats Cards */}
        <div className="mt-4 grid grid-cols-3 gap-4 shrink-0">
          <div className="rounded-xl border border-border p-4 bg-white shadow-sm flex flex-col items-center">
            <span className="text-xs text-inkMuted font-medium uppercase tracking-wider">Before Bandwidth</span>
            <span className="text-3xl font-extrabold text-ink mt-1 font-mono">{bwBefore}</span>
          </div>
          <div className="rounded-xl border border-border p-4 bg-white shadow-sm flex flex-col items-center">
            <span className="text-xs text-inkMuted font-medium uppercase tracking-wider">After Bandwidth</span>
            <span className="text-3xl font-extrabold text-emerald-600 mt-1 font-mono">{bwAfter}</span>
          </div>
          <div className="rounded-xl border border-border p-4 bg-emerald-50/50 border-emerald-100 shadow-sm flex flex-col items-center justify-center">
            <span className="text-xs text-emerald-800 font-medium uppercase tracking-wider">Bandwidth Reduced By</span>
            <span className="text-3xl font-extrabold text-emerald-700 mt-1 font-mono">
              {bwBefore - bwAfter} ({bwBefore > 0 ? Math.round(((bwBefore - bwAfter) / bwBefore) * 100) : 0}%)
            </span>
          </div>
        </div>

        {/* Adjacency Matrix Comparison */}
        <div className="mt-6 grid gap-6 md:grid-cols-2">
          {renderMatrix("Original", beforeOrder)}
          {renderMatrix("Optimized", afterOrder)}
        </div>

        {/* Label Mapping */}
        <div className="mt-6 border-t border-border pt-4 shrink-0">
          <h3 className="text-xs font-bold text-ink uppercase tracking-wider">Recommended Label Re-assignments</h3>
          <p className="text-[10px] text-inkMuted mb-2">Note: Labels have also been added to the main visualization as green badges on each node.</p>
          <div className="mt-2 grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-2">
            {beforeOrder.map((id) => {
              const newLabel = orderLabelMap.get(id) ?? "-";
              return (
                <div key={id} className="flex items-center justify-between border border-border rounded-xl p-2.5 bg-white text-xs shadow-sm">
                  <span className="font-mono font-bold text-inkMuted">{formatNodeId(id, graph)}</span>
                  <span className="text-inkMuted">➔</span>
                  <span className="font-mono font-extrabold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-100">{newLabel}</span>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
