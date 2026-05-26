package com.grafapp.algorithm;

import java.util.*;

/**
 * Hasil eksekusi algoritma: berisi langkah-langkah animasi, ringkasan, dan data tambahan.
 */
public class AlgorithmResult {
    private final List<AlgorithmStep> steps;
    private final String summary;
    private final Map<String, Object> data;

    public AlgorithmResult(List<AlgorithmStep> steps, String summary) {
        this(steps, summary, new HashMap<>());
    }

    public AlgorithmResult(List<AlgorithmStep> steps, String summary, Map<String, Object> data) {
        this.steps = Collections.unmodifiableList(steps);
        this.summary = summary;
        this.data = data;
    }

    public List<AlgorithmStep> getSteps() { return steps; }
    public String getSummary() { return summary; }
    public Map<String, Object> getData() { return data; }
}
