"use client";

import { Graph, GraphEdge, GraphNode, NodeStateKey, nodeStateStyles } from "@/lib/graph/graph";
import { AlgorithmResult } from "@/lib/algorithms/types";

interface Props {
  result: AlgorithmResult | null;
  resultData: Record<string, unknown>;
  graphRef: React.MutableRefObject<Graph>;
  onGraphUpdate: () => void;
  onBandwidthView: () => void;
  onTimetableView: () => void;
}

export default function ResultPanel({ result, resultData, graphRef, onGraphUpdate, onBandwidthView, onTimetableView }: Props) {
  const summary = result?.summary ?? "Jalankan algoritma untuk melihat hasil.";

  const highlightNodes = (nodes: number[], state: NodeStateKey) => {
    const graph = graphRef.current;
    graph.resetStates();
    for (const id of nodes) {
      const node = graph.getNode(id);
      if (node) node.state = state;
    }
    onGraphUpdate();
  };

  const highlightCycle = (cycle: number[], state: NodeStateKey) => {
    const graph = graphRef.current;
    graph.resetStates();
    for (const id of cycle) {
      const node = graph.getNode(id);
      if (node) node.state = state;
    }
    for (let i = 0; i < cycle.length; i++) {
      const u = cycle[i];
      const v = cycle[(i + 1) % cycle.length];
      const edge = graph.getEdge(u, v);
      if (edge) edge.state = "PATH";
    }
    onGraphUpdate();
  };

  const highlightTreeEdges = (edges: number[][]) => {
    const graph = graphRef.current;
    graph.resetStates();
    for (const edgePair of edges) {
      if (edgePair.length < 2) continue;
      const edge = graph.getEdge(edgePair[0], edgePair[1]);
      if (edge) edge.state = "TREE_EDGE";
    }
    onGraphUpdate();
  };

  const renderActions = () => {
    const actions: React.ReactNode[] = [];
    const data = resultData as Record<string, unknown>;

    if (data.bipartite && Array.isArray(data.setA) && Array.isArray(data.setB)) {
      actions.push(
        <button
          key="bipartite-a"
          className="rounded-md bg-accent px-2 py-1 text-xs font-semibold text-white"
          onClick={() => highlightNodes(data.setA as number[], "COMPONENT_1")}
        >
          Highlight set A
        </button>
      );
      actions.push(
        <button
          key="bipartite-b"
          className="rounded-md bg-accentWarm px-2 py-1 text-xs font-semibold text-white"
          onClick={() => highlightNodes(data.setB as number[], "COMPONENT_2")}
        >
          Highlight set B
        </button>
      );
    }

    if (data.hasCycle && Array.isArray(data.allCycles)) {
      (data.allCycles as number[][]).forEach((cycle, idx) => {
        const state = ("COMPONENT_" + ((idx % 7) + 1)) as NodeStateKey;
        const color = nodeStateStyles[state].stroke;
        actions.push(
          <button
            key={`cycle-${idx}`}
            className="rounded-md px-2 py-1 text-xs font-semibold text-white"
            style={{ background: color }}
            onClick={() => highlightCycle(cycle, state)}
          >
            Cycle {idx + 1}
          </button>
        );
      });
    }

    if (Array.isArray(data.mstEdges)) {
      actions.push(
        <button
          key="mst"
          className="rounded-md bg-accent px-2 py-1 text-xs font-semibold text-white"
          onClick={() => highlightTreeEdges(data.mstEdges as number[][])}
        >
          Highlight MST
        </button>
      );
    }

    if (Array.isArray(data.bandwidthOrderAfter)) {
      actions.push(
        <button
          key="bandwidth"
          className="rounded-md bg-accent px-2 py-1 text-xs font-semibold text-white"
          onClick={onBandwidthView}
        >
          Show bandwidth view
        </button>
      );
    }

    if (Array.isArray(data.timetable)) {
      actions.push(
        <button
          key="timetable"
          className="rounded-md bg-accent px-2 py-1 text-xs font-semibold text-white"
          onClick={onTimetableView}
        >
          Show timetable matrix
        </button>
      );
    }

    if (actions.length === 0) {
      return <span className="text-xs text-inkMuted">Tidak ada aksi tambahan.</span>;
    }

    return <div className="flex flex-wrap gap-2">{actions}</div>;
  };

  return (
    <div className="border-t border-border bg-panel-soft px-4 py-3 text-sm">
      <div className="flex flex-col gap-2">
        <div className="text-xs uppercase tracking-wide text-inkMuted">Result</div>
        <div className="text-sm font-semibold text-ink">{summary}</div>
        {renderActions()}
      </div>
    </div>
  );
}
