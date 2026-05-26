package com.grafapp.algorithm;

import com.grafapp.model.Graph;
import java.util.*;

/**
 * Strategy interface untuk semua algoritma graf.
 * Setiap algoritma baru hanya perlu mengimplementasikan interface ini
 * dan mendaftarkannya ke AlgorithmRegistry, tanpa perubahan pada UI.
 */
public interface GraphAlgorithm {

    /** Nama algoritma yang ditampilkan di sidebar */
    String getName();

    /** Kategori: "Traversal", "Connectivity", "Path Finding", dll. */
    String getCategory();

    /** Deskripsi singkat algoritma */
    String getDescription();

    /** Parameter yang diperlukan (misal: start node, end node) */
    List<ParameterInfo> getRequiredParameters();

    /** Eksekusi algoritma dan hasilkan langkah-langkah animasi */
    AlgorithmResult execute(Graph graph, Map<String, Object> parameters);
}
