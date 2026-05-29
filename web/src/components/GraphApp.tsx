"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import AlgorithmSidebar from "@/components/AlgorithmSidebar";
import ControlPanel from "@/components/ControlPanel";
import GraphCanvas, { GraphCanvasHandle, InteractionMode } from "@/components/GraphCanvas";
import ResultPanel from "@/components/ResultPanel";
import BandwidthModal from "@/components/BandwidthModal";
import TimetableModal from "@/components/TimetableModal";
import { AlgorithmRegistry } from "@/lib/algorithms/registry";
import { AlgorithmResult, GraphAlgorithm } from "@/lib/algorithms/types";
import { Graph } from "@/lib/graph/graph";
import { SimulationController } from "@/lib/simulation/simulationController";
import GeoTSPVisualizer from "@/components/GeoTSPVisualizer";
import Homepage from "@/components/Homepage";
import FlightCaseStudy from "@/components/FlightCaseStudy";


export default function GraphApp() {
  const graphRef = useRef<Graph>(new Graph());
  const canvasRef = useRef<GraphCanvasHandle | null>(null);
  const simRef = useRef<SimulationController | null>(null);

  const [graphVersion, setGraphVersion] = useState(0);
  const [fixedCoordinateMode, setFixedCoordinateMode] = useState(false);
  const [mode, setMode] = useState<InteractionMode>("select");
  const [showEdgeWeights, setShowEdgeWeights] = useState(true);
  const [selectedAlgorithm, setSelectedAlgorithm] = useState<GraphAlgorithm | null>(null);
  const [result, setResult] = useState<AlgorithmResult | null>(null);
  const [resultData, setResultData] = useState<Record<string, unknown>>({});
  const [currentStep, setCurrentStep] = useState(-1);
  const [totalSteps, setTotalSteps] = useState(0);
  const [currentMessage, setCurrentMessage] = useState("");
  const [playing, setPlaying] = useState(false);
  const [speed, setSpeed] = useState(1);
  const [bandwidthOpen, setBandwidthOpen] = useState(false);
  const [timetableOpen, setTimetableOpen] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [activeTab, setActiveTab] = useState<"home" | "graph" | "geotsp" | "flights">("home");

  const registry = useMemo(() => AlgorithmRegistry.create(), []);

  const touchGraph = useCallback(() => {
    setGraphVersion((prev) => prev + 1);
  }, []);

  const setGraph = useCallback(
    (graph: Graph, fixedCoordinates: boolean) => {
      // Clear bandwidth labels from all nodes
      graph.getNodes().forEach((node) => {
        (node as any).bandwidthLabel = undefined;
      });

      graphRef.current = graph;
      setFixedCoordinateMode(fixedCoordinates);
      setResult(null);
      setResultData({});
      if (simRef.current) {
        simRef.current.stop();
      } else {
        touchGraph();
      }
      if (fixedCoordinates) {
        canvasRef.current?.stopLayout();
      }
    },
    [touchGraph]
  );

  useEffect(() => {
    if (!simRef.current) {
      simRef.current = new SimulationController({
        getGraph: () => graphRef.current,
        onGraphUpdate: () => touchGraph(),
        onStepChange: (index, total) => {
          setCurrentStep(index);
          setTotalSteps(total);
        },
        onMessageChange: (message) => setCurrentMessage(message),
        onPlayingChange: (value) => setPlaying(value)
      });
    }
  }, [touchGraph]);

  const handleRun = useCallback(
    (algo: GraphAlgorithm, params: Record<string, unknown>) => {
      const graph = graphRef.current;
      graph.resetStates();

      // Clear bandwidth labels from all nodes
      graph.getNodes().forEach((node) => {
        (node as any).bandwidthLabel = undefined;
      });

      const algoResult = algo.execute(graph, params);

      // Check if it is the bandwidth algorithm, and set labels if so
      if (
        algo.name.toLowerCase().includes("bandwidth") &&
        algoResult.data &&
        Array.isArray(algoResult.data.bandwidthOrderAfter)
      ) {
        const orderAfter = algoResult.data.bandwidthOrderAfter as number[];
        orderAfter.forEach((nodeId, index) => {
          const node = graph.getNode(nodeId);
          if (node) {
            (node as any).bandwidthLabel = index + 1; // 1-based index
          }
        });
      }

      setResult(algoResult);
      setResultData(algoResult.data ?? {});
      simRef.current?.loadResult(algoResult);
      simRef.current?.play();
    },
    []
  );

  const handleBandwidthView = useCallback(() => {
    setBandwidthOpen(true);
  }, []);

  const handleTimetableView = useCallback(() => {
    setTimetableOpen(true);
  }, []);

  return (
    <div className="min-h-screen lg:h-screen w-screen flex flex-col p-4 gap-4 overflow-y-auto lg:overflow-hidden bg-transparent">
      {/* Page Header */}
      <header className="flex flex-col md:flex-row items-center justify-between gap-4 px-5 py-3 bg-panel border border-border rounded-2xl shadow-panel shrink-0">
        <div className="cursor-pointer text-center md:text-left flex flex-col items-center md:items-start" onClick={() => setActiveTab("home")}>
          <h1 className="text-base font-bold text-accent tracking-tight flex items-center gap-2">
            <img src="/logo.png" alt="Logo" className="h-6 w-6 object-contain" />
            <span>Graph Algorithm Visualizer</span>
            <span className="text-[10px] bg-accent/10 text-accent font-semibold px-2 py-0.5 rounded-full uppercase tracking-wider">v1.1</span>
          </h1>
          <p className="text-xs text-inkMuted leading-none mt-1">Interactive platform for graph analysis & visualization</p>
        </div>

        {/* Tab switcher */}
        <div className="flex bg-panel-soft border border-border/80 p-1 rounded-xl gap-1 shrink-0">
          <button
            className={`rounded-lg px-3 py-1.5 text-xs font-bold transition ${
              activeTab === "home"
                ? "bg-accent text-white shadow-sm"
                : "text-inkMuted hover:bg-white/50"
            }`}
            onClick={() => setActiveTab("home")}
          >
            🏠 Home
          </button>
          <button
            className={`rounded-lg px-3 py-1.5 text-xs font-bold transition ${
              activeTab === "graph"
                ? "bg-accent text-white shadow-sm"
                : "text-inkMuted hover:bg-white/50"
            }`}
            onClick={() => setActiveTab("graph")}
          >
            📊 Visualisasi Graf Umum
          </button>
          <button
            className={`rounded-lg px-3 py-1.5 text-xs font-bold transition ${
              activeTab === "geotsp"
                ? "bg-accent text-white shadow-sm"
                : "text-inkMuted hover:bg-white/50"
            }`}
            onClick={() => setActiveTab("geotsp")}
          >
            🌍 WorldTSP
          </button>
          <button
            className={`rounded-lg px-3 py-1.5 text-xs font-bold transition ${
              activeTab === "flights"
                ? "bg-accent text-white shadow-sm"
                : "text-inkMuted hover:bg-white/50"
            }`}
            onClick={() => setActiveTab("flights")}
          >
            ✈️ Penerbangan Nusantara
          </button>
        </div>

        <div className="text-center md:text-right flex flex-col items-center md:items-end shrink-0">
          <span className="text-xs font-bold text-ink tracking-tight font-sans">MA3052 Teori Graf Algoritmik</span>
          <span className="text-[10px] text-inkMuted font-mono mt-0.5">Kelompok 7</span>
        </div>
      </header>

      {/* Main Workspace Layout */}
      {activeTab === "home" ? (
        <div className="flex-1 overflow-y-auto min-h-0 w-full pr-1">
          <Homepage onNavigate={setActiveTab} />
        </div>
      ) : activeTab === "graph" ? (
        <div className="flex flex-col lg:flex-row flex-1 gap-4 min-h-0 w-full overflow-y-auto lg:overflow-hidden pb-4 lg:pb-0">
          {/* Left Collapsible Dynamic Sidebar */}
          <div className={`transition-all duration-300 ease-in-out shrink-0 ${sidebarOpen ? 'w-full lg:w-72 h-64 lg:h-full' : 'w-full lg:w-16 h-12 lg:h-full'}`}>
            <AlgorithmSidebar
              registry={registry}
              selected={selectedAlgorithm}
              onSelect={setSelectedAlgorithm}
              isOpen={sidebarOpen}
              onToggle={() => setSidebarOpen(!sidebarOpen)}
            />
          </div>

          {/* Center Canvas & Visualizer Panel */}
          <section className="flex-1 min-h-[400px] lg:min-h-0 flex flex-col border border-border bg-panel rounded-2xl shadow-panel overflow-hidden">
            {/* Interaction Toolbar */}
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border bg-panel-soft px-4 py-2.5 text-sm shrink-0">
              <div className="flex flex-wrap items-center gap-1.5">
                {selectedAlgorithm?.name === "Island Count" ? (
                  <div className="text-xs font-semibold text-accent bg-accent/10 px-3 py-1.5 rounded-xl flex items-center gap-1.5">
                    <span>🧭</span>
                    <span>Grid Navigation Mode (Drag to pan, Scroll to zoom)</span>
                  </div>
                ) : (
                  ([
                    { label: "Select Mode", value: "select", icon: "🖱️" },
                    { label: "+ Node", value: "add-node", icon: "⚪" },
                    { label: "+ Edge", value: "add-edge", icon: "➖" },
                    { label: "Delete", value: "delete", icon: "🗑️" }
                  ] as const).map((item) => (
                    <button
                      key={item.value}
                      className={`rounded-xl px-3 py-1.5 text-xs font-bold transition flex items-center gap-1.5 ${
                        mode === item.value
                          ? "bg-accent text-white shadow-sm"
                          : "bg-white text-ink border border-border/85 hover:bg-panel-soft"
                      }`}
                      onClick={() => setMode(item.value)}
                    >
                      <span>{item.icon}</span>
                      <span>{item.label}</span>
                    </button>
                  ))
                )}
              </div>
              <div className="flex items-center gap-2">
                {selectedAlgorithm?.name !== "Island Count" && (
                  <button
                    className="rounded-xl border border-border bg-white px-3 py-1.5 text-xs font-bold text-ink hover:bg-panel-soft transition"
                    onClick={() => canvasRef.current?.startLayout()}
                  >
                    🔄 Re-layout
                  </button>
                )}
                <button
                  className="rounded-xl border border-border bg-white px-3 py-1.5 text-xs font-bold text-ink hover:bg-panel-soft transition"
                  onClick={() => canvasRef.current?.resetLayout()}
                >
                  🎯 Reset view
                </button>
              </div>
            </div>

            {/* Canvas Wrapper */}
            <div className="flex-1 min-h-0 relative bg-white">
              <GraphCanvas
                ref={canvasRef}
                graphRef={graphRef}
                graphVersion={graphVersion}
                fixedCoordinateMode={fixedCoordinateMode}
                mode={mode}
                showEdgeWeights={showEdgeWeights}
                onGraphChange={touchGraph}
              />
            </div>

            {/* Result Output Panel */}
            <ResultPanel
              result={result}
              resultData={resultData}
              graphRef={graphRef}
              onGraphUpdate={touchGraph}
              onBandwidthView={handleBandwidthView}
              onTimetableView={handleTimetableView}
            />
          </section>

          {/* Right Parameters & Controls Panel */}
          <aside className="w-full lg:w-72 shrink-0 h-auto lg:h-full min-h-0">
            <ControlPanel
              registry={registry}
              selectedAlgorithm={selectedAlgorithm}
              graphRef={graphRef}
              onGraphUpdate={touchGraph}
              onSetGraph={setGraph}
              onRunAlgorithm={handleRun}
              showEdgeWeights={showEdgeWeights}
              onToggleEdgeWeights={setShowEdgeWeights}
              simulation={simRef.current}
              playing={playing}
              speed={speed}
              onSpeedChange={setSpeed}
              currentStep={currentStep}
              totalSteps={totalSteps}
              currentMessage={currentMessage}
            />
          </aside>
        </div>
      ) : activeTab === "geotsp" ? (
        <GeoTSPVisualizer />
      ) : (
        <FlightCaseStudy />
      )}

      <BandwidthModal
        open={bandwidthOpen}
        onClose={() => setBandwidthOpen(false)}
        data={resultData}
        graph={graphRef.current}
      />

      <TimetableModal
        open={timetableOpen}
        onClose={() => setTimetableOpen(false)}
        data={resultData}
        graph={graphRef.current}
      />
    </div>
  );
}
