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
  }, [directed, weighted, inputMode, tspHasLabels]);

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

    let parseResult;
    if (mode === INPUT_MODE_TSP) {
      parseResult = GraphParser.parseTspCoordinates(text, effectiveTspLabels);
      setDirected(false);
      setWeighted(true);
    } else {
      parseResult = GraphParser.parseEdgeListWithStart(text, effectiveDirected, effectiveWeighted);
    }

    const graph = parseResult.graph;
    if (!parseResult.fixedCoordinates) {
      if (opts?.presetLayout) {
        opts.presetLayout(graph);
      }
    }

    onSetGraph(graph, parseResult.fixedCoordinates);
    onGraphUpdate();
    if (parseResult.startVertex >= 0) {
      setStartNodeInput(String(parseResult.startVertex));
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

  const promptInteger = (label: string, defaultValue: number, min: number, max?: number) => {
    const range = max === undefined ? `min ${min}` : `min ${min}, max ${max}`;
    const raw = window.prompt(`${label} (${range})`, String(defaultValue));
    if (raw === null) return null;
    const value = Number(raw);
    if (!Number.isFinite(value) || !Number.isInteger(value)) {
      alert("Masukkan bilangan bulat yang valid.");
      return null;
    }
    if (value < min || (max !== undefined && value > max)) {
      alert(`Nilai harus berada pada rentang ${range}.`);
      return null;
    }
    return value;
  };

  const handleCompleteGraph = () => {
    const n = promptInteger("Masukkan n untuk graf lengkap K_n", 6, 1);
    if (n === null) return;
    createPreset(generateCompleteGraphKn(n), applyCompleteGraphLayout);
  };

  const handleCompleteBipartite = () => {
    const m = promptInteger("Masukkan m untuk graf bipartit K_m,n", 3, 1);
    if (m === null) return;
    const n = promptInteger("Masukkan n untuk graf bipartit K_m,n", 4, 1);
    if (n === null) return;
    createPreset(generateCompleteBipartiteKmn(m, n), (g) => applyCompleteBipartiteLayout(g, m, n));
  };

  const handleTree = () => {
    const n = promptInteger("Masukkan jumlah node untuk pohon T_n", 10, 1);
    if (n === null) return;
    createPreset(generateTree(n), (g) => applyTreeLayout(g, n));
  };

  const handleCycle = () => {
    const n = promptInteger("Masukkan n untuk siklus C_n", 8, 3);
    if (n === null) return;
    createPreset(generateCycleCn(n), applyCycleLayout);
  };

  const handlePath = () => {
    const n = promptInteger("Masukkan n untuk lintasan P_n", 8, 2);
    if (n === null) return;
    createPreset(generatePathPn(n), applyPathLayout);
  };

  const handleWheel = () => {
    const n = promptInteger("Masukkan n untuk graf roda W_n", 8, 4);
    if (n === null) return;
    createPreset(generateWheelWn(n), (g) => applyWheelLayout(g, n));
  };

  const handlePrism = () => {
    const n = promptInteger("Masukkan n untuk graf prisma", 6, 3);
    if (n === null) return;
    createPreset(generatePrismGraph(n), (g) => applyPrismLayout(g, n));
  };

  const handlePetersen = () => {
    const n = promptInteger("Masukkan n untuk generalized Petersen", 5, 3);
    if (n === null) return;
    const maxK = Math.floor((n - 1) / 2);
    if (maxK < 1) {
      alert("Nilai n terlalu kecil untuk Petersen.");
      return;
    }
    const k = promptInteger("Masukkan k untuk generalized Petersen", 2, 1, maxK);
    if (k === null) return;
    createPreset(generateGeneralizedPetersen(n, k), (g) => applyGeneralizedPetersenLayout(g, n));
  };

  const handleCirculant = () => {
    const n = promptInteger("Masukkan n untuk circulant C_n(a1,a2)", 10, 3);
    if (n === null) return;
    const a1 = promptInteger("Masukkan a1 (1 <= a1 < n)", 1, 1, n - 1);
    if (a1 === null) return;
    const a2 = promptInteger("Masukkan a2 (a1 < a2 < n)", 2, 1, n - 1);
    if (a2 === null) return;
    if (a2 <= a1) {
      alert("a2 harus lebih besar dari a1.");
      return;
    }
    createPreset(generateCirculantGraph(n, a1, a2), applyCirculantLayout);
  };

  const handleHypercube = () => {
    const dimension = promptInteger("Masukkan n untuk hypercube H(n)", 4, 1);
    if (dimension === null) return;
    createPreset(generateHypercube(dimension), (g) => applyHypercubeLayout(g, dimension));
  };

  const handleGrid = () => {
    const rows = promptInteger("Masukkan m (baris) untuk grid G(m,n)", 4, 1);
    if (rows === null) return;
    const cols = promptInteger("Masukkan n (kolom) untuk grid G(m,n)", 4, 1);
    if (cols === null) return;
    createPreset(generateGridGraph(rows, cols), (g) => applyGridLayout(g, rows, cols));
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
            if (value === "complete") handleCompleteGraph();
            else if (value === "bipartite") handleCompleteBipartite();
            else if (value === "tree") handleTree();
            else if (value === "cycle") handleCycle();
            else if (value === "path") handlePath();
            else if (value === "wheel") handleWheel();
            else if (value === "prism") handlePrism();
            else if (value === "petersen") handlePetersen();
            else if (value === "circulant") handleCirculant();
            else if (value === "hypercube") handleHypercube();
            else if (value === "grid") handleGrid();
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
    </div>
  );
}
