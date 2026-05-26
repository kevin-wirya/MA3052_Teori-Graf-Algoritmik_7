import { GraphAlgorithm } from "@/lib/algorithms/types";
import { bfsAlgorithm } from "@/lib/algorithms/impl/bfs";
import { dfsAlgorithm } from "@/lib/algorithms/impl/dfs";
import { connectivityCheckAlgorithm } from "@/lib/algorithms/impl/connectivityCheck";
import { connectedComponentsAlgorithm } from "@/lib/algorithms/impl/connectedComponents";
import { largestComponentAlgorithm } from "@/lib/algorithms/impl/largestComponent";
import { pathFinderAlgorithm } from "@/lib/algorithms/impl/pathFinder";
import { dijkstraAlgorithm } from "@/lib/algorithms/impl/dijkstra";
import { tspGreedyAlgorithm } from "@/lib/algorithms/impl/tspGreedy";
import { tspExactAlgorithm } from "@/lib/algorithms/impl/tspExact";
import { bandwidthOptimizationAlgorithm } from "@/lib/algorithms/impl/bandwidth";
import { minimumSpanningTreeAlgorithm } from "@/lib/algorithms/impl/mstKruskal";
import { primMinimumSpanningTreeAlgorithm } from "@/lib/algorithms/impl/mstPrim";
import { bipartiteCheckAlgorithm } from "@/lib/algorithms/impl/bipartiteCheck";
import { cycleDetectionAlgorithm } from "@/lib/algorithms/impl/cycleDetection";
import { diameterAlgorithm } from "@/lib/algorithms/impl/diameter";
import { girthAlgorithm } from "@/lib/algorithms/impl/girth";
import { bipartiteMaximumMatchingAlgorithm } from "@/lib/algorithms/impl/bipartiteMatching";
import { timetablingAlgorithm } from "@/lib/algorithms/impl/timetabling";
import { islandCountAlgorithm } from "@/lib/algorithms/impl/islandCount";

export class AlgorithmRegistry {
  private algorithms: GraphAlgorithm[] = [];

  static create() {
    const registry = new AlgorithmRegistry();
    registry.register(dfsAlgorithm);
    registry.register(bfsAlgorithm);
    registry.register(connectivityCheckAlgorithm);
    registry.register(connectedComponentsAlgorithm);
    registry.register(largestComponentAlgorithm);
    registry.register(pathFinderAlgorithm);
    registry.register(dijkstraAlgorithm);
    registry.register(tspGreedyAlgorithm);
    registry.register(tspExactAlgorithm);
    registry.register(bandwidthOptimizationAlgorithm);
    registry.register(minimumSpanningTreeAlgorithm);
    registry.register(primMinimumSpanningTreeAlgorithm);
    registry.register(bipartiteMaximumMatchingAlgorithm);
    registry.register(timetablingAlgorithm);
    registry.register(bipartiteCheckAlgorithm);
    registry.register(diameterAlgorithm);
    registry.register(cycleDetectionAlgorithm);
    registry.register(girthAlgorithm);
    registry.register(islandCountAlgorithm);
    return registry;
  }

  register(algo: GraphAlgorithm) {
    this.algorithms.push(algo);
  }

  getAll() {
    return [...this.algorithms];
  }

  getByCategory(category: string) {
    return this.algorithms.filter((algo) => algo.category === category);
  }

  getCategories() {
    const set = new Set(this.algorithms.map((algo) => algo.category));
    return Array.from(set);
  }
}
