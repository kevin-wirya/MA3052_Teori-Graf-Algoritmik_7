import { Graph, GraphEdge } from "@/lib/graph/graph";
import { AlgorithmResult, GraphAlgorithm, Step } from "@/lib/algorithms/types";
import { buildAdjacency, buildPartition } from "@/lib/algorithms/impl/bipartiteHelper";

export const timetablingAlgorithm: GraphAlgorithm = {
  name: "Timetabling (Limited Rooms)",
  category: "Scheduling",
  description:
    "Menjadwalkan pengajaran pada graf bipartit dengan batas k kelas per periode.",
  requiredParameters: [
    { key: "classroomLimit", label: "Max Classes per Period (k)", type: "INTEGER", defaultValue: 2, required: true }
  ],
  execute(graph: Graph, parameters: Record<string, unknown>): AlgorithmResult {
    const steps: any[] = [];
    const data: Record<string, unknown> = {};

    if (graph.nodeCount === 0) {
      return { steps, summary: "Graf kosong, jadwal tidak dapat dibuat.", data };
    }

    const classroomLimit = Number(parameters.classroomLimit ?? -1);
    if (!Number.isFinite(classroomLimit) || classroomLimit <= 0) {
      return { steps, summary: "Parameter k harus lebih besar dari 0.", data };
    }

    const adj = buildAdjacency(graph);
    const partition = buildPartition(adj);
    if (!partition.bipartite) {
      return {
        steps,
        summary: `Graf tidak bipartit. Konflik pada edge ${partition.conflictU} - ${partition.conflictV}.`,
        data
      };
    }

    const demandModel = buildDemandModel(graph, partition.side);
    if (demandModel.totalLectures === 0) {
      return { steps, summary: "Tidak ada kebutuhan mengajar.", data };
    }

    const lowerBound = Math.max(
      demandModel.maxDegree,
      Math.ceil(demandModel.totalLectures / classroomLimit)
    );

    steps.push(Step.log(`Lower bound periode = ${lowerBound}`));

    const timetable: number[][][] = [];
    let remaining = demandModel.totalLectures;
    let period = 1;

    while (remaining > 0) {
      const periodMatching = buildPeriodMatching(demandModel, partition.side, classroomLimit);
      if (!periodMatching.length) {
        steps.push(Step.log(`Tidak ada matching pada periode ${period}.`));
        break;
      }
      applyMatching(periodMatching, demandModel);
      remaining -= periodMatching.length;
      steps.push(Step.log(`Periode ${period}: ${formatMatching(periodMatching, graph)}`));
      timetable.push(periodMatching);
      period += 1;
    }

    data.timetable = timetable;
    data.periodCount = timetable.length;
    data.lowerBound = lowerBound;
    data.classroomLimit = classroomLimit;
    data.totalLectures = demandModel.totalLectures;

    const summary = remaining > 0
      ? `Gagal menyusun jadwal lengkap. Sisa kebutuhan = ${remaining} sesi.`
      : `Jadwal selesai dalam ${timetable.length} periode. Lower bound = ${lowerBound}.`;

    return { steps, summary, data };
  }
};

const buildDemandModel = (graph: Graph, side: Map<number, number>) => {
  const demand = new Map<number, Map<number, number>>();
  const degree = new Map<number, number>();
  let totalLectures = 0;
  let maxDegree = 0;

  for (const edge of graph.getEdges()) {
    const u = edge.source;
    const v = edge.target;
    const sideU = side.get(u);
    const sideV = side.get(v);
    if (sideU === undefined || sideV === undefined || sideU === sideV) continue;

    const left = sideU === 0 ? u : v;
    const right = sideU === 0 ? v : u;

    const count = graph.weighted ? Math.round(edge.weight) : 1;
    if (count <= 0) continue;

    if (!demand.has(left)) demand.set(left, new Map());
    const map = demand.get(left) as Map<number, number>;
    map.set(right, (map.get(right) ?? 0) + count);

    degree.set(left, (degree.get(left) ?? 0) + count);
    degree.set(right, (degree.get(right) ?? 0) + count);
    totalLectures += count;
  }

  degree.forEach((d) => {
    if (d > maxDegree) maxDegree = d;
  });

  return { demand, degree, totalLectures, maxDegree };
};

const buildPeriodMatching = (
  demandModel: ReturnType<typeof buildDemandModel>,
  side: Map<number, number>,
  limit: number
) => {
  const matchTo = new Map<number, number>();
  const left = Array.from(demandModel.demand.keys());

  demandModel.degree.forEach((_, key) => matchTo.set(key, -1));

  left.sort((a, b) => (demandModel.degree.get(b) ?? 0) - (demandModel.degree.get(a) ?? 0));

  let matchSize = 0;
  let progress = true;
  while (progress && matchSize < limit) {
    progress = false;
    for (const start of left) {
      if ((matchTo.get(start) ?? -1) !== -1) continue;
      const search = findAugmentingPath(start, demandModel, side, matchTo);
      if (search.found) {
        applyAugmentingPath(search.path, matchTo);
        matchSize += 1;
        progress = true;
        if (matchSize >= limit) break;
      }
    }
  }

  const matchingEdges: number[][] = [];
  for (const x of left) {
    const y = matchTo.get(x) ?? -1;
    if (y !== -1) matchingEdges.push([x, y]);
  }
  return matchingEdges;
};

const findAugmentingPath = (
  start: number,
  demandModel: ReturnType<typeof buildDemandModel>,
  side: Map<number, number>,
  matchTo: Map<number, number>
) => {
  const queue: number[] = [start];
  const parent = new Map<number, number>();
  const reachedLeft = new Set<number>();
  const reachedRight = new Set<number>();

  parent.set(start, -1);
  reachedLeft.add(start);

  while (queue.length) {
    const x = queue.shift() as number;
    const neighbors = demandModel.demand.get(x) ?? new Map();

    for (const [y, remaining] of neighbors.entries()) {
      if (remaining <= 0) continue;
      if ((matchTo.get(x) ?? -1) === y) continue;
      if ((side.get(y) ?? 0) !== 1 || reachedRight.has(y)) continue;

      reachedRight.add(y);
      parent.set(y, x);

      const matched = matchTo.get(y) ?? -1;
      if (matched === -1) {
        return { found: true, path: reconstructPath(y, parent) };
      }

      if (!reachedLeft.has(matched)) {
        reachedLeft.add(matched);
        parent.set(matched, y);
        queue.push(matched);
      }
    }
  }

  return { found: false, path: [] };
};

const reconstructPath = (end: number, parent: Map<number, number>) => {
  const path: number[] = [];
  let cursor = end;
  while (cursor !== -1) {
    path.push(cursor);
    cursor = parent.get(cursor) ?? -1;
  }
  return path.reverse();
};

const applyAugmentingPath = (path: number[], matchTo: Map<number, number>) => {
  for (let i = 0; i < path.length - 1; i++) {
    const u = path[i];
    const v = path[i + 1];
    if ((matchTo.get(u) ?? -1) === v) {
      matchTo.set(u, -1);
      matchTo.set(v, -1);
    } else {
      matchTo.set(u, v);
      matchTo.set(v, u);
    }
  }
};

const applyMatching = (
  matching: number[][],
  demandModel: ReturnType<typeof buildDemandModel>
) => {
  matching.forEach(([left, right]) => {
    const neighbors = demandModel.demand.get(left);
    if (!neighbors) return;
    const remaining = neighbors.get(right) ?? 0;
    if (remaining <= 0) return;
    const updated = remaining - 1;
    if (updated === 0) neighbors.delete(right);
    else neighbors.set(right, updated);

    demandModel.degree.set(left, (demandModel.degree.get(left) ?? 0) - 1);
    demandModel.degree.set(right, (demandModel.degree.get(right) ?? 0) - 1);
  });
};

const formatMatching = (matching: number[][], graph: Graph) => {
  return matching
    .map((edge) => {
      const uLabel = graph.getNode(edge[0])?.label ?? String(edge[0]);
      const vLabel = graph.getNode(edge[1])?.label ?? String(edge[1]);
      return `${uLabel}-${vLabel}`;
    })
    .join(", ");
};
