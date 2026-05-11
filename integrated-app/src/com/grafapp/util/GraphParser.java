package com.grafapp.util;

import com.grafapp.model.Graph;
import com.grafapp.model.GraphNode;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Parser untuk mengubah input teks adjacency list menjadi objek Graph.
 */
public class GraphParser {

    public static class ParseResult {
        private final Graph graph;
        private final int startVertex;
        private final boolean fixedCoordinates;

        public ParseResult(Graph graph, int startVertex) {
            this(graph, startVertex, false);
        }

        public ParseResult(Graph graph, int startVertex, boolean fixedCoordinates) {
            this.graph = graph;
            this.startVertex = startVertex;
            this.fixedCoordinates = fixedCoordinates;
        }

        public Graph getGraph() { return graph; }
        public int getStartVertex() { return startVertex; }
        public boolean hasStartVertex() { return startVertex >= 0; }
        public boolean hasFixedCoordinates() { return fixedCoordinates; }
    }

    /**
     * Parse edge list format with header.
     * Format:
     *   Baris 1: "n m" (n = jumlah node, m = jumlah edge)
     *   Baris 2..m+1: "u v" atau "u v w" (m baris edge)
     *   Baris terakhir (opsional): starting vertex
     */
    public static ParseResult parseEdgeListWithStart(String text, boolean directed) {
        return parseEdgeListWithStart(text, directed, false);
    }

    public static ParseResult parseEdgeListWithStart(String text, boolean directed, boolean weighted) {
        Graph graph = new Graph(directed);
        graph.setWeighted(weighted);
        int startVertex = -1;

        // Kumpulkan baris non-kosong non-komentar
        List<String> dataLines = collectDataLines(text);

        if (dataLines.isEmpty()) return new ParseResult(graph, -1);

        // Coba deteksi header "n m"
        String first = dataLines.get(0);
        String[] hdr = first.split("[\\s,;]+");
        if (hdr.length == 2) {
            try {
                int n = Integer.parseInt(hdr[0]);
                int m = Integer.parseInt(hdr[1]);
                // Validasi: setelah header, harus ada m baris edge + opsional 1 baris start
                int remaining = dataLines.size() - 1;
                if (remaining == m || remaining == m + 1) {
                    // Format terdeteksi: n m header
                    for (int i = 0; i < n; i++) graph.addNode(i);

                    // Parse m baris edge
                    for (int i = 1; i <= m && i < dataLines.size(); i++) {
                        parseEdgeLine(graph, dataLines.get(i), weighted);
                    }

                    // Baris terakhir = starting vertex (jika ada sisa 1 baris)
                    if (remaining == m + 1) {
                        String lastLine = dataLines.get(dataLines.size() - 1);
                        String[] lastParts = lastLine.split("[\\s,;]+");
                        if (lastParts.length == 1) {
                            try {
                                startVertex = Integer.parseInt(lastParts[0]);
                            } catch (NumberFormatException e) { /* bukan start vertex */ }
                        }
                    }

                    return new ParseResult(graph, startVertex);
                }
            } catch (NumberFormatException e) { /* bukan header */ }
        }

        // Fallback: plain edge list tanpa header
        boolean labelMode = requiresLabelMapping(dataLines);
        if (labelMode) {
            Map<String, Integer> labelToId = new LinkedHashMap<>();
            for (String line : dataLines) {
                String[] parts = line.split("[\\s,;]+");
                if (parts.length >= 2) {
                    if (isTimetableMetadata(parts)) {
                        continue;
                    }
                    int u = getOrCreateLabelId(parts[0], labelToId, graph);
                    int v = getOrCreateLabelId(parts[1], labelToId, graph);
                    if (weighted && parts.length >= 3) {
                        try {
                            double w = Double.parseDouble(parts[2]);
                            graph.addEdge(u, v, w);
                        } catch (NumberFormatException e) {
                            graph.addEdge(u, v);
                        }
                    } else {
                        graph.addEdge(u, v);
                    }
                } else if (parts.length == 1) {
                    if (isTimetableMetadata(parts)) {
                        continue;
                    }
                    getOrCreateLabelId(parts[0], labelToId, graph);
                }
            }
            return new ParseResult(graph, -1);
        }

        for (String line : dataLines) {
            String[] parts = line.split("[\\s,;]+");
            if (parts.length >= 2) {
                parseEdgeLine(graph, line, weighted);
            } else if (parts.length == 1) {
                try {
                    graph.addNode(Integer.parseInt(parts[0]));
                } catch (NumberFormatException e) { /* skip */ }
            }
        }
        return new ParseResult(graph, -1);
    }

    /**
     * Parse format koordinat untuk TSP.
     * Format:
     *   Baris 1: n (jumlah titik)
     *   Baris 2..n+1: "x y" (koordinat titik)
     *
     * Node ID akan dibuat berurutan dari 0..n-1.
     * Edge lengkap otomatis dibentuk dengan bobot jarak Euclidean.
     */
    public static ParseResult parseTspCoordinates(String text, boolean hasLabels) {
        Graph graph = createEmptyCoordinateGraph();

        List<String> dataLines = collectDataLines(text);

        if (dataLines.isEmpty()) {
            return new ParseResult(graph, -1, true);
        }

        int startIndex = 0;
        String[] firstLine = dataLines.get(0).split("[\\s,;]+");
        if (firstLine.length == 1) {
            try {
                Integer.parseInt(firstLine[0]);
                startIndex = 1; // It has a header
            } catch (NumberFormatException ex) {
                // Not a header
            }
        }

        int nodeCount = dataLines.size() - startIndex;
        if (nodeCount <= 0) {
            return new ParseResult(graph, -1, true);
        }

        List<GraphNode> orderedNodes = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            String[] parts = dataLines.get(startIndex + i).split("[\\s,;]+");
            int minParts = hasLabels ? 3 : 2;
            if (parts.length < minParts) {
                return new ParseResult(createEmptyCoordinateGraph(), -1, true);
            }

            double x;
            double y;
            String label = null;
            try {
                x = Double.parseDouble(parts[parts.length - (hasLabels ? 2 : 2)]); // Wait, X and Y are always the last two!
                // Actually, if hasLabels is true, the last two are X and Y.
                // If hasLabels is false, they are still the last two (or the only two).
                x = Double.parseDouble(parts[parts.length - 2]);
                y = Double.parseDouble(parts[parts.length - 1]);
                
                if (hasLabels) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < parts.length - 2; j++) {
                        if (j > 0) sb.append(" ");
                        sb.append(parts[j]);
                    }
                    label = sb.toString();
                }
            } catch (NumberFormatException ex) {
                return new ParseResult(createEmptyCoordinateGraph(), -1, true);
            }

            GraphNode node = new GraphNode(i);
            if (label != null && !label.isEmpty()) {
                node.setLabel(label);
            }
            node.setCoordinate(x, y);
            node.setX(x);
            node.setY(y);
            node.setPinned(true);
            graph.addNode(node);
            orderedNodes.add(node);
        }

        for (int i = 0; i < orderedNodes.size(); i++) {
            for (int j = i + 1; j < orderedNodes.size(); j++) {
                GraphNode a = orderedNodes.get(i);
                GraphNode b = orderedNodes.get(j);
                double distance = Math.hypot(
                    a.getCoordinateX() - b.getCoordinateX(),
                    a.getCoordinateY() - b.getCoordinateY()
                );
                graph.addEdge(a.getId(), b.getId(), distance);
            }
        }

        return new ParseResult(graph, 0, true);
    }

    /**
     * Cek apakah teks kemungkinan besar memakai format koordinat TSP.
     * Valid jika:
     *   - Baris pertama hanya berisi satu integer n > 0
     *   - Ada tepat n baris koordinat setelahnya
     *   - Tiap baris koordinat berisi minimal dua angka (x y)
     */
    public static boolean isTspCoordinateFormat(String text) {
        List<String> dataLines = collectDataLines(text);
        if (dataLines.isEmpty()) {
            return false;
        }

        String[] firstLine = dataLines.get(0).split("[\\s,;]+");
        if (firstLine.length != 1) {
            return false;
        }

        int n;
        try {
            n = Integer.parseInt(firstLine[0]);
        } catch (NumberFormatException ex) {
            return false;
        }

        if (n <= 0) {
            return false;
        }

        int nodeCount = dataLines.size() - 1;
        if (nodeCount != n) {
            return false;
        }

        for (int i = 1; i < dataLines.size(); i++) {
            String[] parts = dataLines.get(i).split("[\\s,;]+");
            if (parts.length < 2) {
                return false;
            }
            try {
                Double.parseDouble(parts[parts.length - 2]);
                Double.parseDouble(parts[parts.length - 1]);
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        return true;
    }

    private static Graph createEmptyCoordinateGraph() {
        Graph graph = new Graph(false);
        graph.setWeighted(true);
        return graph;
    }

    private static List<String> collectDataLines(String text) {
        List<String> dataLines = new ArrayList<>();
        for (String line : text.split("\\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("//")) {
                dataLines.add(trimmed);
            }
        }
        return dataLines;
    }

    /**
     * Parse satu baris edge: "u v" atau "u v weight"
     */
    private static void parseEdgeLine(Graph graph, String line, boolean weighted) {
        String[] parts = line.trim().split("[\\s,;]+");
        if (parts.length < 2) return;
        try {
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            if (weighted && parts.length >= 3) {
                try {
                    double w = Double.parseDouble(parts[2]);
                    graph.addEdge(u, v, w);
                } catch (NumberFormatException e) {
                    graph.addEdge(u, v);
                }
            } else {
                graph.addEdge(u, v);
            }
        } catch (NumberFormatException e) { /* skip */ }
    }

    private static boolean requiresLabelMapping(List<String> dataLines) {
        for (String line : dataLines) {
            String[] parts = line.split("[\\s,;]+");
            if (parts.length == 0) {
                continue;
            }
            if (isTimetableMetadata(parts)) {
                continue;
            }
            int limit = Math.min(parts.length, 2);
            for (int i = 0; i < limit; i++) {
                if (!isIntegerToken(parts[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isIntegerToken(String token) {
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static boolean isTimetableMetadata(String[] parts) {
        return parts.length >= 2 && parts[0].equalsIgnoreCase("k") && isIntegerToken(parts[1]);
    }

    private static int getOrCreateLabelId(
        String label,
        Map<String, Integer> labelToId,
        Graph graph
    ) {
        Integer existing = labelToId.get(label);
        if (existing != null) {
            return existing;
        }
        int id = labelToId.size();
        labelToId.put(label, id);
        graph.addNode(new GraphNode(id, label));
        return id;
    }

    /**
     * Convenience: parse edge list tanpa info start vertex.
     */
    public static Graph parseEdgeList(String text, boolean directed) {
        return parseEdgeListWithStart(text, directed).getGraph();
    }

    /**
     * Parse adjacency matrix format.
     * Setiap baris = satu row matrix, dipisah spasi/koma.
     */
    public static Graph parseAdjacencyMatrix(String text, boolean directed) {
        Graph graph = new Graph(directed);
        List<int[]> matrix = new ArrayList<>();

        for (String line : text.split("\\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split("[\\s,;]+");
            int[] row = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                row[i] = Integer.parseInt(parts[i]);
            }
            matrix.add(row);
        }

        int n = matrix.size();
        for (int i = 0; i < n; i++) graph.addNode(i);

        for (int i = 0; i < n; i++) {
            int[] row = matrix.get(i);
            int start = directed ? 0 : i + 1;
            for (int j = start; j < row.length && j < n; j++) {
                if (row[j] != 0) graph.addEdge(i, j);
            }
        }
        return graph;
    }

    /** Load graph dari file (edge list format, undirected) */
    public static Graph parseFromFile(String filePath) throws IOException {
        String text = new String(Files.readAllBytes(Paths.get(filePath)));
        return parseEdgeList(text, false);
    }

    /** Baca teks mentah dari file */
    public static String readFileText(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}
