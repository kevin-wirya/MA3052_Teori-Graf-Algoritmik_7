package com.grafapp.util;

import com.grafapp.model.Graph;
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

        public ParseResult(Graph graph, int startVertex) {
            this.graph = graph;
            this.startVertex = startVertex;
        }

        public Graph getGraph() { return graph; }
        public int getStartVertex() { return startVertex; }
        public boolean hasStartVertex() { return startVertex >= 0; }
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
        String[] lines = text.split("\\n");
        int startVertex = -1;

        // Kumpulkan baris non-kosong non-komentar
        List<String> dataLines = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("//")) {
                dataLines.add(trimmed);
            }
        }

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
