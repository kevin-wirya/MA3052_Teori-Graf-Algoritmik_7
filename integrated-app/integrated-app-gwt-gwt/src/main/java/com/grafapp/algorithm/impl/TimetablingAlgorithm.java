package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.AlgorithmResult;
import com.grafapp.algorithm.AlgorithmStep;
import com.grafapp.algorithm.GraphAlgorithm;
import com.grafapp.algorithm.ParameterInfo;
import com.grafapp.model.Graph;
import com.grafapp.model.GraphEdge;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Timetabling problem dengan batas jumlah kelas per periode (k).
 */
public class TimetablingAlgorithm implements GraphAlgorithm {

    @Override
    public String getName() {
        return "Timetabling (Limited Rooms)";
    }

    @Override
    public String getCategory() {
        return "Scheduling";
    }

    @Override
    public String getDescription() {
        return "Menjadwalkan pengajaran pada graf bipartit dengan batas k kelas per periode. "
            + "Menggunakan matching maksimum per periode hingga semua kebutuhan terpenuhi.";
    }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Arrays.asList(
            new ParameterInfo("classroomLimit", "Max Classes per Period (k)",
                ParameterInfo.Type.INTEGER, 2, true)
        );
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();

        if (graph == null || graph.getNodeCount() == 0) {
            return new AlgorithmResult(steps, "Graf kosong, jadwal tidak dapat dibuat.", data);
        }

        int classroomLimit = parseLimit(parameters.get("classroomLimit"));
        if (classroomLimit <= 0) {
            return new AlgorithmResult(steps, "Parameter k harus lebih besar dari 0.", data);
        }

        Map<Integer, List<Integer>> adj = BipartiteMatchingHelper.buildAdjacency(graph);
        BipartiteMatchingHelper.PartitionResult partition = BipartiteMatchingHelper.buildPartition(adj);
        if (!partition.bipartite) {
            String summary = "Graf tidak bipartit. Konflik pada edge "
                + partition.conflictU + " - " + partition.conflictV + ".";
            return new AlgorithmResult(steps, summary, data);
        }

        DemandModel demandModel = buildDemandModel(graph, partition.side);
        if (demandModel.totalLectures == 0) {
            return new AlgorithmResult(steps, "Tidak ada kebutuhan mengajar pada graf.", data);
        }

        int lowerBound = Math.max(demandModel.maxDegree,
            (int) Math.ceil(demandModel.totalLectures / (double) classroomLimit));

        steps.add(AlgorithmStep.log("Lower bound periode = " + lowerBound));

        List<List<List<Integer>>> timetable = new ArrayList<>();
        int remaining = demandModel.totalLectures;
        int period = 1;

        // Bangun matching per periode sampai semua kebutuhan habis.
        while (remaining > 0) {
            List<List<Integer>> periodMatching = buildPeriodMatching(
                demandModel,
                partition.side,
                classroomLimit
            );

            if (periodMatching.isEmpty()) {
                steps.add(AlgorithmStep.log("Tidak ada matching pada periode " + period + "."));
                break;
            }

            applyMatching(periodMatching, demandModel);
            remaining -= periodMatching.size();

            steps.add(AlgorithmStep.log("Periode " + period + ": " + formatMatching(periodMatching)));
            timetable.add(periodMatching);
            period++;
        }

        int usedPeriods = timetable.size();

        data.put("timetable", timetable);
        data.put("periodCount", usedPeriods);
        data.put("lowerBound", lowerBound);
        data.put("classroomLimit", classroomLimit);
        data.put("totalLectures", demandModel.totalLectures);

        String summary;
        if (remaining > 0) {
            summary = "Gagal menyusun jadwal lengkap. "
                + "Sisa kebutuhan = " + remaining + " sesi.";
        } else {
            summary = "Jadwal selesai dalam " + usedPeriods + " periode. "
                + "Lower bound = " + lowerBound + ".";
        }

        return new AlgorithmResult(steps, summary, data);
    }

    private int parseLimit(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return -1;
    }

    private DemandModel buildDemandModel(Graph graph, Map<Integer, Integer> side) {
        Map<Integer, Map<Integer, Integer>> demand = new HashMap<>();
        Map<Integer, Integer> degree = new HashMap<>();

        int totalLectures = 0;
        int maxDegree = 0;

        for (GraphEdge edge : graph.getEdges()) {
            int u = edge.getSource();
            int v = edge.getTarget();
            Integer sideU = side.get(u);
            Integer sideV = side.get(v);
            if (sideU == null || sideV == null || sideU.equals(sideV)) {
                continue;
            }

            int left = sideU == 0 ? u : v;
            int right = sideU == 0 ? v : u;

            int count = graph.isWeighted() ? (int) Math.round(edge.getWeight()) : 1;
            if (count <= 0) {
                continue;
            }

            demand.computeIfAbsent(left, k -> new HashMap<>())
                .merge(right, count, Integer::sum);

            degree.put(left, degree.getOrDefault(left, 0) + count);
            degree.put(right, degree.getOrDefault(right, 0) + count);
            totalLectures += count;
        }

        for (int d : degree.values()) {
            if (d > maxDegree) {
                maxDegree = d;
            }
        }

        return new DemandModel(demand, degree, totalLectures, maxDegree);
    }

    private List<List<Integer>> buildPeriodMatching(
        DemandModel demandModel,
        Map<Integer, Integer> side,
        int limit
    ) {
        Map<Integer, Integer> matchTo = new HashMap<>();
        Set<Integer> leftSet = new HashSet<>(demandModel.demand.keySet());
        List<Integer> left = new ArrayList<>(leftSet);

        for (Map.Entry<Integer, Integer> entry : demandModel.degree.entrySet()) {
            matchTo.put(entry.getKey(), -1);
        }

        left.sort((a, b) -> Integer.compare(
            demandModel.degree.getOrDefault(b, 0),
            demandModel.degree.getOrDefault(a, 0)
        ));

        int matchSize = 0;
        boolean progress = true;
        while (progress && matchSize < limit) {
            progress = false;
            for (int start : left) {
                if (matchTo.getOrDefault(start, -1) != -1) {
                    continue;
                }

                AugmentSearchResult search = findAugmentingPath(
                    start,
                    demandModel,
                    side,
                    matchTo
                );
                if (search.found) {
                    applyAugmentingPath(search.path, matchTo);
                    matchSize++;
                    progress = true;
                    if (matchSize >= limit) {
                        break;
                    }
                }
            }
        }

        List<List<Integer>> matchingEdges = new ArrayList<>();
        for (int x : left) {
            int y = matchTo.getOrDefault(x, -1);
            if (y != -1) {
                matchingEdges.add(Arrays.asList(x, y));
            }
        }

        return matchingEdges;
    }

    private AugmentSearchResult findAugmentingPath(
        int start,
        DemandModel demandModel,
        Map<Integer, Integer> side,
        Map<Integer, Integer> matchTo
    ) {
        Queue<Integer> queue = new ArrayDeque<>();
        Map<Integer, Integer> parent = new HashMap<>();
        Set<Integer> reachedLeft = new HashSet<>();
        Set<Integer> reachedRight = new HashSet<>();

        queue.add(start);
        parent.put(start, -1);
        reachedLeft.add(start);

        while (!queue.isEmpty()) {
            int x = queue.poll();
            Map<Integer, Integer> neighbors = demandModel.demand.getOrDefault(x, Collections.emptyMap());

            for (Map.Entry<Integer, Integer> entry : neighbors.entrySet()) {
                int y = entry.getKey();
                int remaining = entry.getValue();
                if (remaining <= 0) {
                    continue;
                }
                if (matchTo.getOrDefault(x, -1) == y) {
                    continue;
                }
                if (side.getOrDefault(y, 0) != 1 || reachedRight.contains(y)) {
                    continue;
                }

                reachedRight.add(y);
                parent.put(y, x);

                int matched = matchTo.getOrDefault(y, -1);
                if (matched == -1) {
                    return new AugmentSearchResult(true, reconstructPath(y, parent));
                }

                if (!reachedLeft.contains(matched)) {
                    reachedLeft.add(matched);
                    parent.put(matched, y);
                    queue.add(matched);
                }
            }
        }

        return new AugmentSearchResult(false, Collections.emptyList());
    }

    private List<Integer> reconstructPath(int end, Map<Integer, Integer> parent) {
        List<Integer> path = new ArrayList<>();
        int cursor = end;
        while (cursor != -1) {
            path.add(cursor);
            cursor = parent.getOrDefault(cursor, -1);
        }
        Collections.reverse(path);
        return path;
    }

    private void applyAugmentingPath(List<Integer> path, Map<Integer, Integer> matchTo) {
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i);
            int v = path.get(i + 1);
            if (matchTo.getOrDefault(u, -1) == v) {
                matchTo.put(u, -1);
                matchTo.put(v, -1);
            } else {
                matchTo.put(u, v);
                matchTo.put(v, u);
            }
        }
    }

    private void applyMatching(List<List<Integer>> matching, DemandModel demandModel) {
        for (List<Integer> edge : matching) {
            int left = edge.get(0);
            int right = edge.get(1);

            Map<Integer, Integer> neighbors = demandModel.demand.get(left);
            if (neighbors == null) {
                continue;
            }

            int remaining = neighbors.getOrDefault(right, 0);
            if (remaining <= 0) {
                continue;
            }

            int updated = remaining - 1;
            if (updated == 0) {
                neighbors.remove(right);
            } else {
                neighbors.put(right, updated);
            }

            demandModel.degree.put(left, demandModel.degree.getOrDefault(left, 0) - 1);
            demandModel.degree.put(right, demandModel.degree.getOrDefault(right, 0) - 1);
        }
    }

    private String formatMatching(List<List<Integer>> matching) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matching.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            List<Integer> edge = matching.get(i);
            sb.append(edge.get(0)).append("-").append(edge.get(1));
        }
        return sb.toString();
    }

    private static final class AugmentSearchResult {
        private final boolean found;
        private final List<Integer> path;

        private AugmentSearchResult(boolean found, List<Integer> path) {
            this.found = found;
            this.path = path;
        }
    }

    private static final class DemandModel {
        private final Map<Integer, Map<Integer, Integer>> demand;
        private final Map<Integer, Integer> degree;
        private final int totalLectures;
        private final int maxDegree;

        private DemandModel(
            Map<Integer, Map<Integer, Integer>> demand,
            Map<Integer, Integer> degree,
            int totalLectures,
            int maxDegree
        ) {
            this.demand = demand;
            this.degree = degree;
            this.totalLectures = totalLectures;
            this.maxDegree = maxDegree;
        }
    }
}
