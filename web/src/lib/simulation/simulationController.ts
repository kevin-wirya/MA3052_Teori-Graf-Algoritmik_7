import { AlgorithmResult, AlgorithmStep } from "@/lib/algorithms/types";
import { Graph, EdgeStateKey, NodeStateKey, componentStateFor } from "@/lib/graph/graph";

interface SimulationOptions {
  getGraph: () => Graph;
  onGraphUpdate: () => void;
  onStepChange?: (index: number, total: number) => void;
  onMessageChange?: (message: string) => void;
  onPlayingChange?: (playing: boolean) => void;
}

export class SimulationController {
  private getGraph: () => Graph;
  private onGraphUpdate: () => void;
  private onStepChange?: (index: number, total: number) => void;
  private onMessageChange?: (message: string) => void;
  private onPlayingChange?: (playing: boolean) => void;

  private steps: AlgorithmStep[] = [];
  private currentIndex = -1;
  private speed = 1;
  private playing = false;
  private timer: number | null = null;

  constructor(options: SimulationOptions) {
    this.getGraph = options.getGraph;
    this.onGraphUpdate = options.onGraphUpdate;
    this.onStepChange = options.onStepChange;
    this.onMessageChange = options.onMessageChange;
    this.onPlayingChange = options.onPlayingChange;
  }

  setSpeed(value: number) {
    this.speed = value;
  }

  loadResult(result: AlgorithmResult) {
    this.stop();
    this.steps = result.steps ?? [];
    this.currentIndex = -1;
    this.onStepChange?.(this.currentIndex, this.steps.length);
    this.onMessageChange?.("");
    const graph = this.getGraph();
    graph.resetStates();
    this.onGraphUpdate();
  }

  play() {
    if (this.steps.length === 0) return;
    if (this.currentIndex >= this.steps.length - 1) return;
    if (this.playing) return;
    this.playing = true;
    this.onPlayingChange?.(true);

    const interval = () => {
      if (!this.playing) return;
      const delay = 500 / this.speed;
      this.stepForward();
      this.timer = window.setTimeout(interval, delay);
    };

    interval();
  }

  pause() {
    this.playing = false;
    this.onPlayingChange?.(false);
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
  }

  stop() {
    this.pause();
    this.currentIndex = -1;
    this.onStepChange?.(this.currentIndex, this.steps.length);
    this.onMessageChange?.("");
    const graph = this.getGraph();
    graph.resetStates();
    this.onGraphUpdate();
  }

  stepForward() {
    if (this.currentIndex >= this.steps.length - 1) {
      this.pause();
      return;
    }
    this.currentIndex += 1;
    this.applyStep(this.steps[this.currentIndex]);
    this.onStepChange?.(this.currentIndex, this.steps.length);
    this.onGraphUpdate();
  }

  stepBackward() {
    if (this.currentIndex < 0) return;
    const graph = this.getGraph();
    graph.resetStates();
    this.currentIndex -= 1;
    for (let i = 0; i <= this.currentIndex; i++) {
      this.applyStep(this.steps[i]);
    }
    this.onStepChange?.(this.currentIndex, this.steps.length);
    this.onGraphUpdate();
  }

  goToStep(index: number) {
    if (!this.steps.length) return;
    const clamped = Math.max(-1, Math.min(index, this.steps.length - 1));
    const graph = this.getGraph();
    graph.resetStates();
    for (let i = 0; i <= clamped; i++) {
      this.applyStep(this.steps[i]);
    }
    this.currentIndex = clamped;
    this.onStepChange?.(this.currentIndex, this.steps.length);
    this.onGraphUpdate();
  }

  private applyStep(step: AlgorithmStep) {
    if (step.message) {
      this.onMessageChange?.(step.message);
    }

    switch (step.action) {
      case "VISIT_NODE":
      case "PROCESS_NODE":
        this.setNodeState(step.nodeId, "PROCESSING");
        break;
      case "FINISH_NODE":
        this.setNodeState(step.nodeId, "VISITED");
        break;
      case "TRAVERSE_EDGE":
        this.setEdgeState(step.edgeSource, step.edgeTarget, "TRAVERSING");
        break;
      case "FINISH_EDGE":
        this.setEdgeState(step.edgeSource, step.edgeTarget, "VISITED");
        break;
      case "MARK_PATH_NODE":
        this.setNodeState(step.nodeId, "PATH");
        break;
      case "MARK_PATH_EDGE":
        this.setEdgeState(step.edgeSource, step.edgeTarget, "PATH");
        break;
      case "MARK_TREE_EDGE":
        this.setEdgeState(step.edgeSource, step.edgeTarget, "TREE_EDGE");
        break;
      case "MARK_BRIDGE":
        this.setEdgeState(step.edgeSource, step.edgeTarget, "BRIDGE");
        break;
      case "MARK_ARTICULATION":
        this.setNodeState(step.nodeId, "ARTICULATION");
        break;
      case "MARK_COMPONENT":
        if (step.nodeGroup) {
          const state = componentStateFor(step.componentId ?? 0);
          step.nodeGroup.forEach((id) => this.setNodeState(id, state));
        }
        break;
      case "MARK_START":
        this.setNodeState(step.nodeId, "START");
        break;
      case "MARK_END":
        this.setNodeState(step.nodeId, "END");
        break;
      case "LOG":
      default:
        break;
    }
  }

  private setNodeState(nodeId?: number, state?: NodeStateKey) {
    if (nodeId === undefined || !state) return;
    const graph = this.getGraph();
    const node = graph.getNode(nodeId);
    if (node) node.state = state;
  }

  private setEdgeState(source?: number, target?: number, state?: EdgeStateKey) {
    if (source === undefined || target === undefined || !state) return;
    const graph = this.getGraph();
    const edge = graph.getEdge(source, target);
    if (edge) edge.state = state;
  }
}
