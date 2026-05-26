import { Graph } from "@/lib/graph/graph";

export type ParameterType = "NODE_SELECT" | "INTEGER" | "BOOLEAN";

export interface ParameterInfo {
  key: string;
  label: string;
  type: ParameterType;
  defaultValue: unknown;
  required: boolean;
}

export type AlgorithmAction =
  | "VISIT_NODE"
  | "PROCESS_NODE"
  | "FINISH_NODE"
  | "TRAVERSE_EDGE"
  | "FINISH_EDGE"
  | "MARK_PATH_NODE"
  | "MARK_PATH_EDGE"
  | "MARK_TREE_EDGE"
  | "MARK_BRIDGE"
  | "MARK_ARTICULATION"
  | "MARK_COMPONENT"
  | "MARK_START"
  | "MARK_END"
  | "LOG";

export interface AlgorithmStep {
  action: AlgorithmAction;
  nodeId?: number;
  edgeSource?: number;
  edgeTarget?: number;
  componentId?: number;
  nodeGroup?: number[];
  message?: string;
}

export interface AlgorithmResult {
  steps: AlgorithmStep[];
  summary: string;
  data: Record<string, unknown>;
}

export interface GraphAlgorithm {
  name: string;
  category: string;
  description: string;
  requiredParameters: ParameterInfo[];
  execute: (graph: Graph, parameters: Record<string, unknown>) => AlgorithmResult;
}

export const Step = {
  visitNode(nodeId: number, message?: string): AlgorithmStep {
    return { action: "VISIT_NODE", nodeId, message };
  },
  processNode(nodeId: number, message?: string): AlgorithmStep {
    return { action: "PROCESS_NODE", nodeId, message };
  },
  finishNode(nodeId: number, message?: string): AlgorithmStep {
    return { action: "FINISH_NODE", nodeId, message };
  },
  traverseEdge(edgeSource: number, edgeTarget: number, message?: string): AlgorithmStep {
    return { action: "TRAVERSE_EDGE", edgeSource, edgeTarget, message };
  },
  finishEdge(edgeSource: number, edgeTarget: number, message?: string): AlgorithmStep {
    return { action: "FINISH_EDGE", edgeSource, edgeTarget, message };
  },
  markPathNode(nodeId: number, message?: string): AlgorithmStep {
    return { action: "MARK_PATH_NODE", nodeId, message };
  },
  markPathEdge(edgeSource: number, edgeTarget: number, message?: string): AlgorithmStep {
    return { action: "MARK_PATH_EDGE", edgeSource, edgeTarget, message };
  },
  markTreeEdge(edgeSource: number, edgeTarget: number, message?: string): AlgorithmStep {
    return { action: "MARK_TREE_EDGE", edgeSource, edgeTarget, message };
  },
  markBridge(edgeSource: number, edgeTarget: number, message?: string): AlgorithmStep {
    return { action: "MARK_BRIDGE", edgeSource, edgeTarget, message };
  },
  markArticulation(nodeId: number, message?: string): AlgorithmStep {
    return { action: "MARK_ARTICULATION", nodeId, message };
  },
  markComponent(componentId: number, nodeGroup: number[], message?: string): AlgorithmStep {
    return { action: "MARK_COMPONENT", componentId, nodeGroup, message };
  },
  markStart(nodeId: number, message?: string): AlgorithmStep {
    return { action: "MARK_START", nodeId, message };
  },
  markEnd(nodeId: number, message?: string): AlgorithmStep {
    return { action: "MARK_END", nodeId, message };
  },
  log(message: string): AlgorithmStep {
    return { action: "LOG", message };
  }
};
