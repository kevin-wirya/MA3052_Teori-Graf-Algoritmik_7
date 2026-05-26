"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { AlgorithmRegistry } from "@/lib/algorithms/registry";
import { GraphAlgorithm, ParameterInfo } from "@/lib/algorithms/types";
import { Graph, GraphNode } from "@/lib/graph/graph";
import {
  GraphParser,
  isTspCoordinateFormat
} from "@/lib/graph/parser";
import {
  generateCompleteBipartiteKmn,
  generateCompleteGraphKn,
  generateCycleCn,
  generateGridGraph,
  generateHypercube,
  generatePathPn,
  generatePrismGraph,
  generateTree,
  generateWheelWn,
  generateGeneralizedPetersen,
  generateCirculantGraph,
  applyCompleteBipartiteLayout,
  applyCycleLayout,
  applyGridLayout,
  applyHypercubeLayout,
  applyPathLayout,
  applyPrismLayout,
  applyTreeLayout,
  applyWheelLayout,
  applyGeneralizedPetersenLayout,
  applyCirculantLayout,
  applyCompleteGraphLayout
} from "@/lib/graph/presets";
import { SimulationController } from "@/lib/simulation/simulationController";

const INPUT_MODE_EDGE_LIST = "Edge List";
const INPUT_MODE_TSP = "TSP Coordinates";

const parseMatrix = (text: string) => {
  const lines = text
    .split("\n")
    .map((line) => line.trim().replace(/[\s,]+/g, ""))
    .filter((line) => line.length > 0 && !line.startsWith("#") && !line.startsWith("//"));

  if (lines.length === 0) return null;
  const R = lines.length;
  const C = lines[0].length;

  for (const line of lines) {
    if (line.length !== C) return null;
    if (!/^[01]+$/.test(line)) return null;
  }

  const graph = new Graph(false);
  (graph as any).isGrid = true;
  (graph as any).gridRows = R;
  (graph as any).gridCols = C;

  for (let r = 0; r < R; r++) {
    for (let c = 0; c < C; c++) {
      const id = r * C + c;
      const isLand = lines[r][c] === "1";
      const node = new GraphNode(id, isLand ? "1" : "0");
      (node as any).gridRow = r;
      (node as any).gridCol = c;
      (node as any).isLand = isLand;
      node.state = isLand ? "UNVISITED" : "COMPONENT_7";
      graph.addNode(node);
    }
  }

  // 4-directional edges between adjacent land cells
  for (let r = 0; r < R; r++) {
    for (let c = 0; c < C; c++) {
      if (lines[r][c] === "1") {
        const u = r * C + c;
        // Right
        if (c + 1 < C && lines[r][c + 1] === "1") {
          const v = r * C + (c + 1);
          graph.addEdge(u, v);
        }
        // Down
        if (r + 1 < R && lines[r + 1][c] === "1") {
          const v = (r + 1) * C + c;
          graph.addEdge(u, v);
        }
      }
    }
  }

  return graph;
};

const SAMPLE_DATA_FILES = [
  "bandwidth_zigzag_path_10.txt",
  "bandwidth_zigzag_path_15.txt",
  "bandwidth_zigzag_path_30.txt",
  "binary_tree.txt",
  "disconnected_graph.txt",
  "djikstra_1.txt",
  "djikstra_2.txt",
  "djikstra_3.txt",
  "graph_1.txt",
  "graph_2.txt",
  "graph_3.txt",
  "graph_4.txt",
  "graph_5.txt",
  "graph_6.txt",
  "graph_7.txt",
  "grid_graph.txt",
  "indonesia.txt",
  "island_5x5.txt",
  "island_10x10.txt",
  "island_15x15.txt",
  "island_grid_1.txt",
  "island_grid_2.txt",
  "k3_3.txt",
  "k5.txt",
  "kota_bandung.txt",
  "line_graph.txt",
  "matching_large.txt",
  "matching_limited.txt",
  "matching_medium.txt",
  "matching_small.txt",
  "path_1.txt",
  "path_2.txt",
  "petersen_graph.txt",
  "star_graph.txt",
  "timetable_large.txt",
  "timetable_medium.txt",
  "timetable_small.txt",
  "tsp_1.txt",
  "tsp_2.txt",
  "tsp_3.txt",
  "tsp_4.txt",
  "tsp_5.txt",
  "tsp_6.txt",
  "tsp_coordinates_1.txt",
  "tsp_coordinates_2.txt",
  "tsp_coordinates_3.txt",
  "tsp_coordinates_4.txt",
  "tsp_coordinates_5.txt"
];

interface Props {
  registry: AlgorithmRegistry;
  selectedAlgorithm: GraphAlgorithm | null;
  graphRef: React.MutableRefObject<Graph>;
  onGraphUpdate: () => void;
  onSetGraph: (graph: Graph, fixedCoordinates: boolean) => void;
  onRunAlgorithm: (algo: GraphAlgorithm, params: Record<string, unknown>) => void;
  showEdgeWeights: boolean;
  onToggleEdgeWeights: (value: boolean) => void;
  simulation: SimulationController | null;
  playing: boolean;
  speed: number;
  onSpeedChange: (value: number) => void;
  currentStep: number;
  totalSteps: number;
  currentMessage: string;
}

export default function ControlPanel({
  selectedAlgorithm,
  graphRef,
  onGraphUpdate,
  onSetGraph,
  onRunAlgorithm,
  showEdgeWeights,
  onToggleEdgeWeights,
  simulation,
  playing,
  speed,
  onSpeedChange,
  currentStep,
  totalSteps,
  currentMessage
}: Props) {
  const [inputMode, setInputMode] = useState(INPUT_MODE_EDGE_LIST);
  const [directed, setDirected] = useState(false);
  const [weighted, setWeighted] = useState(false);
  const [tspHasLabels, setTspHasLabels] = useState(false);
  const [graphText, setGraphText] = useState("");
  const [fileName, setFileName] = useState("");
  const [startNodeInput, setStartNodeInput] = useState("0");
  const skipAutoParseRef = useRef(false);

  const parameterControls = useMemo(() => {
    const params: ParameterInfo[] = selectedAlgorithm?.requiredParameters ?? [];
    return params;
  }, [selectedAlgorithm]);

  useEffect(() => {
    if (skipAutoParseRef.current) {
      skipAutoParseRef.current = false;
      return;
    }
    if (!graphText.trim()) return;
    applyGraph(graphText);
  }, [directed, weighted, inputMode, tspHasLabels, selectedAlgorithm]);

  const applyGraph = (
    text: string,
    opts?: {
      presetLayout?: (graph: Graph) => void;
      inputModeOverride?: string;
      directedOverride?: boolean;
      weightedOverride?: boolean;
      tspHasLabelsOverride?: boolean;
    }
  ) => {
    if (!text.trim()) {
      const emptyGraph = new Graph();
      onSetGraph(emptyGraph, false);
      onGraphUpdate();
      return;
    }

    const mode = opts?.inputModeOverride ?? inputMode;
    const effectiveDirected = opts?.directedOverride ?? directed;
    const effectiveWeighted = opts?.weightedOverride ?? weighted;
    const effectiveTspLabels = opts?.tspHasLabelsOverride ?? tspHasLabels;

    let graph: Graph;
    let fixedCoordinates = false;
    let startVertex = -1;

    const matrixGraph = parseMatrix(text);
    const isIslandCount = selectedAlgorithm?.name === "Island Count";

    if (matrixGraph && (isIslandCount || text.trim().split("\n")[0].trim().replace(/[\s,]+/g, "").match(/^[01]+$/))) {
      graph = matrixGraph;
      fixedCoordinates = true;
    } else {
      let parseResult;
      if (mode === INPUT_MODE_TSP) {
        parseResult = GraphParser.parseTspCoordinates(text, effectiveTspLabels);
        setDirected(false);
        setWeighted(true);
      } else {
        parseResult = GraphParser.parseEdgeListWithStart(text, effectiveDirected, effectiveWeighted);
      }
      graph = parseResult.graph;
      fixedCoordinates = parseResult.fixedCoordinates;
      startVertex = parseResult.startVertex;
    }

    if (!fixedCoordinates) {
      if (opts?.presetLayout) {
        opts.presetLayout(graph);
      }
    }

    onSetGraph(graph, fixedCoordinates);
    onGraphUpdate();
    if (startVertex >= 0) {
      setStartNodeInput(String(startVertex));
    }
  };

  const handleFileLoad = async (name: string) => {
    if (!name) return;
    setFileName(name);
    const response = await fetch(`/data/${name}`);
    if (!response.ok) {
      alert(`Gagal memuat file ${name}. Pastikan data tersedia di web/public/data`);
      return;
    }
    const text = await response.text();
    const normalized = text.replace(/\r\n/g, "\n").replace(/\r/g, "\n");
    setGraphText(normalized);
    const isTsp = isTspCoordinateFormat(normalized);
    if (isTsp) {
      setInputMode(INPUT_MODE_TSP);
      setDirected(false);
      setWeighted(true);
      applyGraph(normalized, { inputModeOverride: INPUT_MODE_TSP });
    } else {
      setInputMode(INPUT_MODE_EDGE_LIST);
      applyGraph(normalized, { inputModeOverride: INPUT_MODE_EDGE_LIST });
    }
  };

  const resolveNodeInput = (input: string, graph: Graph) => {
    const trimmed = input.trim();
    if (!trimmed) return null;
    const numeric = Number(trimmed);
    if (Number.isFinite(numeric) && graph.getNode(numeric)) {
      return numeric;
    }
    let fallback: GraphNode | null = null;
    for (const node of graph.getNodes()) {
      if (node.label === trimmed) return node.id;
    }
    for (const node of graph.getNodes()) {
      if (node.label && node.label.toLowerCase() === trimmed.toLowerCase()) {
        if (fallback) return null;
        fallback = node;
      }
    }
    return fallback?.id ?? null;
  };

  const handleRun = () => {
    if (!selectedAlgorithm) {
      alert("Pilih algoritma dari sidebar.");
      return;
    }

    const graph = graphRef.current;
    if (graph.nodeCount === 0) {
      alert("Masukkan graf terlebih dahulu.");
      return;
    }

    const params: Record<string, unknown> = {};
    for (const param of parameterControls) {
      if (param.type === "NODE_SELECT") {
        const raw =
          param.key === "startNode"
            ? startNodeInput
            : ((document.getElementById(`param-${param.key}`) as HTMLInputElement)?.value ?? "");
        const resolved = resolveNodeInput(raw, graph);
        if (resolved === null && param.required) {
          alert(`Node '${raw}' tidak ditemukan.`);
          return;
        }
        params[param.key] = resolved ?? 0;
        continue;
      }

      if (param.type === "BOOLEAN") {
        params[param.key] = (document.getElementById(`param-${param.key}`) as HTMLInputElement)?.checked ?? false;
        continue;
      }

      const raw = (document.getElementById(`param-${param.key}`) as HTMLInputElement)?.value ?? "";
      if (!raw.trim() && param.required) {
        alert(`Parameter '${param.label}' wajib diisi.`);
        return;
      }
      params[param.key] = Number(raw);
    }

    onRunAlgorithm(selectedAlgorithm, params);
  };

  const createPreset = (text: string, layout?: (graph: Graph) => void) => {
    skipAutoParseRef.current = true;
    setGraphText(text);
    setInputMode(INPUT_MODE_EDGE_LIST);
    setDirected(false);
    setWeighted(false);
    applyGraph(text, { presetLayout: layout });
  };

  const onSpeedChangeLocal = (value: number) => {
    onSpeedChange(value);
    simulation?.setSpeed(value);
  };

  const [presetModal, setPresetModal] = useState<{
    type: string;
    title: string;
    fields: { key: string; label: string; defaultValue: number; min: number; max?: number }[];
  } | null>(null);
  const [presetParams, setPresetParams] = useState<Record<string, number>>({});

  const openPresetModal = (type: string) => {
    let title = "";
    let fields: { key: string; label: string; defaultValue: number; min: number; max?: number }[] = [];

    if (type === "complete") {
      title = "Complete Graph K_n";
      fields = [{ key: "n", label: "Jumlah Node (n)", defaultValue: 6, min: 1 }];
    } else if (type === "bipartite") {
      title = "Complete Bipartite K_m,n";
      fields = [
        { key: "m", label: "Sisi Kiri (m)", defaultValue: 3, min: 1 },
        { key: "n", label: "Sisi Kanan (n)", defaultValue: 4, min: 1 }
      ];
    } else if (type === "tree") {
      title = "Tree Graph T_n";
      fields = [{ key: "n", label: "Jumlah Node (n)", defaultValue: 10, min: 1 }];
    } else if (type === "cycle") {
      title = "Cycle Graph C_n";
      fields = [{ key: "n", label: "Jumlah Node (n)", defaultValue: 8, min: 3 }];
    } else if (type === "path") {
      title = "Path Graph P_n";
      fields = [{ key: "n", label: "Jumlah Node (n)", defaultValue: 8, min: 2 }];
    } else if (type === "wheel") {
      title = "Wheel Graph W_n";
      fields = [{ key: "n", label: "Jumlah Node (n)", defaultValue: 8, min: 4 }];
    } else if (type === "prism") {
      title = "Prism Graph";
      fields = [{ key: "n", label: "Jumlah Node (n)", defaultValue: 6, min: 3 }];
    } else if (type === "petersen") {
      title = "Generalized Petersen Graph G(n, k)";
      fields = [
        { key: "n", label: "Jumlah Node Ring (n)", defaultValue: 5, min: 3 },
        { key: "k", label: "Lompatan (k)", defaultValue: 2, min: 1 }
      ];
    } else if (type === "circulant") {
      title = "Circulant Graph C_n(a1, a2)";
      fields = [
        { key: "n", label: "Jumlah Node (n)", defaultValue: 10, min: 3 },
        { key: "a1", label: "Lompatan Pertama (a1)", defaultValue: 1, min: 1 },
        { key: "a2", label: "Lompatan Kedua (a2)", defaultValue: 2, min: 1 }
      ];
    } else if (type === "hypercube") {
      title = "Hypercube H(n)";
      fields = [{ key: "dimension", label: "Dimensi (n)", defaultValue: 4, min: 1 }];
    } else if (type === "grid") {
      title = "Grid Graph G(m, n)";
      fields = [
        { key: "rows", label: "Baris (m)", defaultValue: 4, min: 1 },
        { key: "cols", label: "Kolom (n)", defaultValue: 4, min: 1 }
      ];
    }

    const initialParams: Record<string, number> = {};
    fields.forEach((f) => {
      initialParams[f.key] = f.defaultValue;
    });

    setPresetParams(initialParams);
    setPresetModal({ type, title, fields });
  };

  return (
    <div className="flex h-full flex-col gap-3 rounded-2xl border border-border bg-panel p-3.5 shadow-panel overflow-y-auto max-h-full">
      {/* 1. Input & Load Graph */}
      <section className="space-y-1.5">
        <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">Input & Load</h3>
        <div className="space-y-1.5">
          <div className="grid grid-cols-2 gap-1.5">
            <select
              className="rounded-lg border border-border bg-white px-2 py-1 text-xs focus:border-accent focus:outline-none"
              value={inputMode}
              onChange={(event) => setInputMode(event.target.value)}
            >
              <option>{INPUT_MODE_EDGE_LIST}</option>
              <option>{INPUT_MODE_TSP}</option>
            </select>

            <select
              className="rounded-lg border border-border bg-white px-2 py-1 text-xs focus:border-accent focus:outline-none"
              value={fileName}
              onChange={(event) => handleFileLoad(event.target.value)}
            >
              <option value="">Sample file...</option>
              {SAMPLE_DATA_FILES.map((file) => (
                <option key={file} value={file}>
                  {file}
                </option>
              ))}
            </select>
          </div>

          {inputMode === INPUT_MODE_TSP && (
            <label className="flex items-center gap-1.5 text-[10px] text-inkMuted font-medium">
              <input
                type="checkbox"
                checked={tspHasLabels}
                onChange={(event) => setTspHasLabels(event.target.checked)}
                className="rounded text-accent focus:ring-accent h-3.5 w-3.5 border-border"
              />
              Koordinat memiliki label
            </label>
          )}

          <textarea
            className="h-14 w-full rounded-lg border border-border bg-panel-soft px-2 py-1 font-mono text-[10px] resize-none focus:border-accent focus:outline-none leading-relaxed"
            placeholder="Format: u v [weight]..."
            value={graphText}
            onChange={(event) => {
              const value = event.target.value;
              setGraphText(value);
              applyGraph(value);
            }}
          />

          <div className="flex flex-wrap gap-x-3 gap-y-0.5 text-[10px] text-inkMuted font-medium">
            <label className="flex items-center gap-1">
              <input
                type="checkbox"
                checked={directed}
                disabled={inputMode === INPUT_MODE_TSP}
                onChange={(event) => setDirected(event.target.checked)}
                className="rounded text-accent focus:ring-accent h-3 w-3 border-border"
              />
              Directed
            </label>
            <label className="flex items-center gap-1">
              <input
                type="checkbox"
                checked={weighted}
                disabled={inputMode === INPUT_MODE_TSP}
                onChange={(event) => setWeighted(event.target.checked)}
                className="rounded text-accent focus:ring-accent h-3 w-3 border-border"
              />
              Weighted
            </label>
            <label className="flex items-center gap-1">
              <input
                type="checkbox"
                checked={showEdgeWeights}
                onChange={(event) => onToggleEdgeWeights(event.target.checked)}
                className="rounded text-accent focus:ring-accent h-3 w-3 border-border"
              />
              Jarak
            </label>
          </div>
        </div>
      </section>

      {/* 2. Graph Presets */}
      <section className="space-y-1">
        <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">Graph Preset</h3>
        <select
          className="w-full rounded-lg border border-border bg-white px-2 py-1 text-xs focus:border-accent focus:outline-none"
          value=""
          onChange={(event) => {
            const value = event.target.value;
            if (!value) return;
            openPresetModal(value);
          }}
        >
          <option value="">-- Generate Preset Graph --</option>
          <option value="complete">Graf Lengkap (Kn)</option>
          <option value="bipartite">Graf Bipartit (Km,n)</option>
          <option value="tree">Pohon (Tn)</option>
          <option value="cycle">Siklus (Cn)</option>
          <option value="path">Lintasan (Pn)</option>
          <option value="wheel">Graf Roda (Wn)</option>
          <option value="prism">Graf Prisma</option>
          <option value="petersen">Petersen Graph</option>
          <option value="circulant">Circulant Graph</option>
          <option value="hypercube">Hypercube H(n)</option>
          <option value="grid">Grid G(m,n)</option>
        </select>
      </section>

      {/* 3. Info & Starting Node */}
      <section className="space-y-1.5 border-t border-border/40 pt-2">
        <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">Graph Info & Start</h3>
        <div className="grid grid-cols-3 gap-1.5 items-center text-xs">
          <div className="bg-panel-soft border border-border/40 px-2 py-1 rounded-lg text-center">
            <div className="text-[9px] text-inkMuted uppercase font-semibold">Nodes</div>
            <div className="font-bold text-accent">{graphRef.current.nodeCount}</div>
          </div>
          <div className="bg-panel-soft border border-border/40 px-2 py-1 rounded-lg text-center">
            <div className="text-[9px] text-inkMuted uppercase font-semibold">Edges</div>
            <div className="font-bold text-accent">{graphRef.current.edgeCount}</div>
          </div>
          <div className="flex flex-col items-center bg-panel-soft border border-border/40 px-1 py-0.5 rounded-lg">
            <span className="text-[9px] text-inkMuted uppercase font-semibold">Start</span>
            <input
              className="w-12 rounded border border-border bg-white px-1 py-0.5 font-mono text-center text-xs focus:border-accent focus:outline-none"
              value={startNodeInput}
              onChange={(event) => setStartNodeInput(event.target.value)}
            />
          </div>
        </div>
      </section>

      {/* 4. Selected Algorithm & Parameters */}
      <section className="space-y-1.5 border-t border-border/40 pt-2">
        <div className="flex items-center justify-between">
          <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">Algorithm</h3>
          <span className="text-[8px] font-bold bg-accent/15 text-accent px-1.5 py-0.5 rounded-full">Selected</span>
        </div>
        <div className="rounded-lg border border-accent/20 bg-accent/5 px-2 py-1">
          <p className="text-[11px] font-bold text-accent truncate">
            {selectedAlgorithm ? selectedAlgorithm.name : "Select from sidebar"}
          </p>
        </div>
        {parameterControls.filter((param) => param.key !== "startNode").length > 0 && (
          <div className="space-y-1 bg-panel-soft p-1.5 rounded-lg border border-border/40">
            {parameterControls
              .filter((param) => param.key !== "startNode")
              .map((param) => (
                <label key={param.key} className="flex items-center justify-between gap-2 text-[10px] text-ink">
                  <span className="text-inkMuted font-medium truncate">{param.label}</span>
                  {param.type === "BOOLEAN" ? (
                    <input
                      id={`param-${param.key}`}
                      type="checkbox"
                      defaultChecked={Boolean(param.defaultValue)}
                      className="rounded text-accent h-3.5 w-3.5 border-border"
                    />
                  ) : (
                    <input
                      id={`param-${param.key}`}
                      type={param.type === "NODE_SELECT" ? "text" : "number"}
                      defaultValue={String(param.defaultValue ?? "")}
                      className="w-16 rounded border border-border bg-white px-1.5 py-0.5 text-center font-mono text-xs focus:border-accent focus:outline-none"
                    />
                  )}
                </label>
              ))}
          </div>
        )}
        <button
          className="w-full rounded-lg bg-accent hover:bg-accent/90 px-3 py-1.5 text-xs font-bold text-white transition shadow-sm"
          onClick={handleRun}
        >
          Run Algorithm
        </button>
      </section>

      {/* 5. Simulation Player */}
      <section className="space-y-1.5 border-t border-border/40 pt-2 pb-1">
        <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">Simulation</h3>
        <div className="flex gap-1 justify-between">
          <button
            className="flex-1 rounded bg-panel-soft hover:bg-border/20 border border-border/60 py-1 text-xs transition"
            onClick={() => simulation?.stepBackward()}
            title="Step Backward"
          >
            ⏮
          </button>
          <button
            className="flex-[2] rounded bg-accent text-white hover:bg-accent/90 py-1 text-xs font-bold transition shadow-sm"
            onClick={() => (playing ? simulation?.pause() : simulation?.play())}
          >
            {playing ? "⏸ Pause" : "▶ Play"}
          </button>
          <button
            className="flex-1 rounded bg-panel-soft hover:bg-border/20 border border-border/60 py-1 text-xs transition"
            onClick={() => simulation?.stepForward()}
            title="Step Forward"
          >
            ⏭
          </button>
          <button
            className="flex-1 rounded bg-accentWarm/10 hover:bg-accentWarm/20 border border-accentWarm/20 py-1 text-xs text-accentWarm font-bold transition"
            onClick={() => simulation?.stop()}
            title="Stop"
          >
            ⏹
          </button>
        </div>
        <div className="space-y-0.5 pt-0.5">
          <div className="flex items-center justify-between text-[10px] text-inkMuted font-medium">
            <span>Speed</span>
            <span className="font-mono">{speed.toFixed(2)}x</span>
          </div>
          <input
            type="range"
            min={0.25}
            max={4}
            step={0.05}
            value={speed}
            className="w-full h-1 bg-panel-soft rounded-lg appearance-none cursor-pointer accent-accent"
            onChange={(event) => onSpeedChangeLocal(Number(event.target.value))}
          />
        </div>
        <div className="flex items-center justify-between text-[10px] text-inkMuted font-medium bg-panel-soft px-2 py-0.5 rounded border border-border/30">
          <span>Step Progress</span>
          <span className="font-mono text-accent font-bold">
            {Math.max(currentStep + 1, 0)} / {totalSteps}
          </span>
        </div>
        {currentMessage && (
          <div className="text-[10px] bg-accent/5 border border-accent/15 rounded p-1.5 text-accent leading-tight font-medium">
            {currentMessage}
          </div>
        )}
      </section>

      {presetModal && (
        <div className="fixed inset-0 z-55 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
          <div className="w-full max-w-sm rounded-2xl bg-panel p-5 shadow-panel border border-border flex flex-col text-left">
            <h3 className="text-sm font-bold text-ink mb-3">{presetModal.title}</h3>
            <div className="space-y-3">
              {presetModal.fields.map((field) => (
                <div key={field.key} className="flex flex-col gap-1">
                  <label className="text-[10px] text-inkMuted font-bold uppercase">{field.label}</label>
                  <input
                    type="number"
                    className="w-full rounded-lg border border-border bg-white px-3 py-1.5 text-xs font-semibold focus:border-accent focus:outline-none text-ink"
                    value={presetParams[field.key] ?? field.defaultValue}
                    min={field.min}
                    max={field.max}
                    onChange={(e) => {
                      const val = Number(e.target.value);
                      setPresetParams((prev) => ({ ...prev, [field.key]: val }));
                    }}
                  />
                </div>
              ))}
            </div>
            <div className="mt-5 flex gap-2 justify-end">
              <button
                className="rounded-xl border border-border bg-white px-4 py-2 text-xs font-bold text-ink hover:bg-panel-soft transition"
                onClick={() => setPresetModal(null)}
              >
                Batal
              </button>
              <button
                className="rounded-xl bg-accent text-white px-4 py-2 text-xs font-bold shadow-sm hover:opacity-90 transition"
                onClick={() => {
                  const type = presetModal.type;
                  const p = presetParams;

                  for (const field of presetModal.fields) {
                    const val = p[field.key];
                    if (!Number.isInteger(val)) {
                      alert("Masukkan bilangan bulat yang valid.");
                      return;
                    }
                    if (val < field.min || (field.max !== undefined && val > field.max)) {
                      alert(`Nilai ${field.label} harus berada dalam rentang ${field.min}${field.max !== undefined ? ` - ${field.max}` : ""}`);
                      return;
                    }
                  }

                  if (type === "complete") {
                    createPreset(generateCompleteGraphKn(p.n), applyCompleteGraphLayout);
                  } else if (type === "bipartite") {
                    createPreset(generateCompleteBipartiteKmn(p.m, p.n), (g) => applyCompleteBipartiteLayout(g, p.m, p.n));
                  } else if (type === "tree") {
                    createPreset(generateTree(p.n), (g) => applyTreeLayout(g, p.n));
                  } else if (type === "cycle") {
                    createPreset(generateCycleCn(p.n), applyCycleLayout);
                  } else if (type === "path") {
                    createPreset(generatePathPn(p.n), applyPathLayout);
                  } else if (type === "wheel") {
                    createPreset(generateWheelWn(p.n), (g) => applyWheelLayout(g, p.n));
                  } else if (type === "prism") {
                    createPreset(generatePrismGraph(p.n), (g) => applyPrismLayout(g, p.n));
                  } else if (type === "petersen") {
                    const maxK = Math.floor((p.n - 1) / 2);
                    if (p.k > maxK) {
                      alert(`k untuk Petersen tidak boleh lebih besar dari ${maxK} (floor((n-1)/2)).`);
                      return;
                    }
                    createPreset(generateGeneralizedPetersen(p.n, p.k), (g) => applyGeneralizedPetersenLayout(g, p.n));
                  } else if (type === "circulant") {
                    if (p.a2 <= p.a1) {
                      alert("a2 harus lebih besar dari a1.");
                      return;
                    }
                    if (p.a1 >= p.n || p.a2 >= p.n) {
                      alert("Lompatan (a1, a2) harus lebih kecil dari n.");
                      return;
                    }
                    createPreset(generateCirculantGraph(p.n, p.a1, p.a2), applyCirculantLayout);
                  } else if (type === "hypercube") {
                    createPreset(generateHypercube(p.dimension), (g) => applyHypercubeLayout(g, p.dimension));
                  } else if (type === "grid") {
                    createPreset(generateGridGraph(p.rows, p.cols), (g) => applyGridLayout(g, p.rows, p.cols));
                  }

                  setPresetModal(null);
                }}
              >
                Generate
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
