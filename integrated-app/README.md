# Graf Algoritmik — Integrated Visualization Platform

Aplikasi GUI terintegrasi untuk visualisasi algoritma graf, dikembangkan untuk mata kuliah **Teori Graf Algoritmik** di ITB.

## Fitur

- **8 Algoritma Built-in**: DFS, BFS, Connected Components, Connectivity Check, Path Finder, Largest Component, Bipartite Check, Cycle Detection
- **Force-Directed Layout**: Physics-based node positioning dengan repulsion, attraction, dan gravity
- **Interaksi Dinamis**: Drag-and-drop node, zoom in/out (scroll), pan (drag area kosong)
- **Simulasi Step-by-Step**: Animasi eksekusi algoritma dengan Play/Pause/Step/Speed kontrol
- **Visual State**: Node berubah warna berdasarkan status (Unvisited → Processing → Visited)
- **Extensible**: Tambah algoritma baru cukup implement `GraphAlgorithm` dan register di `AlgorithmRegistry`

## Struktur Package

```
integrated-app/
├── src/com/grafapp/
│   ├── Main.java                          # Entry point JavaFX
│   ├── model/                             # Data model
│   │   ├── Graph.java                     #   Graf (adjacency list)
│   │   ├── GraphNode.java                 #   Node + physics properties
│   │   ├── GraphEdge.java                 #   Edge + state
│   │   ├── NodeState.java                 #   Enum status visual node
│   │   └── EdgeState.java                 #   Enum status visual edge
│   ├── algorithm/                         # Strategy Pattern
│   │   ├── GraphAlgorithm.java            #   Interface (Strategy)
│   │   ├── AlgorithmRegistry.java         #   Registry semua algoritma
│   │   ├── AlgorithmStep.java             #   Satu langkah animasi
│   │   ├── AlgorithmResult.java           #   Hasil eksekusi
│   │   ├── ParameterInfo.java             #   Deskriptor parameter
│   │   └── impl/                          #   Implementasi algoritma
│   │       ├── DFSAlgorithm.java
│   │       ├── BFSAlgorithm.java
│   │       ├── ConnectedComponentsAlgorithm.java
│   │       ├── ConnectivityCheckAlgorithm.java
│   │       ├── PathFinderAlgorithm.java
│   │       ├── LargestComponentAlgorithm.java
│   │       ├── BipartiteCheckAlgorithm.java
│   │       └── CycleDetectionAlgorithm.java
│   ├── layout/
│   │   └── ForceDirectedLayout.java       # Fruchterman-Reingold engine
│   ├── visualization/
│   │   ├── GraphCanvas.java               # Canvas rendering + interaksi
│   │   └── SimulationController.java      # Play/Pause/Step controller
│   ├── ui/
│   │   ├── MainView.java                  # Layout utama (BorderPane)
│   │   ├── AlgorithmSidebar.java          # Sidebar kiri (daftar algoritma)
│   │   ├── ControlPanel.java              # Panel kanan (input + kontrol)
│   │   └── ResultPanel.java              # Panel hasil eksekusi algoritma
│   └── util/
│       └── GraphParser.java               # Parser edge list / adj matrix
├── styles/
│   └── theme.css                          # Light mode CSS theme
├── build.bat                              # Script kompilasi
├── launch.bat                             # Script menjalankan
└── README.md
```

## Cara Menjalankan

JavaFX SDK 21 sudah di-bundle di dalam folder `lib/`, sehingga tidak perlu install atau konfigurasi tambahan. Cukup pastikan **JDK 11+** sudah terinstall dan tersedia di PATH.

```batch
build.bat
launch.bat
```

## Cara Menggunakan

1. **Input graf** di panel kanan via text area (format: edge list, satu edge per baris `u v`)  
   atau klik **Sample** untuk contoh graf
2. **Pilih algoritma** dari sidebar kiri
3. **Isi parameter** (misal: Start Node) di panel kanan
4. Klik **Run Algorithm** — animasi otomatis berjalan
5. Gunakan **Play/Pause/Step** untuk mengontrol animasi
6. **Drag node** untuk menggeser posisi, **scroll** untuk zoom, **drag area kosong** untuk pan

### Mode Canvas

| Mode   | Fungsi                               |
| ------ | ------------------------------------ |
| Select | Drag node, pan canvas                |
| + Node | Klik area kosong untuk tambah node   |
| + Edge | Klik node A lalu B untuk tambah edge |
| Delete | Klik node untuk menghapus            |

## Menambah Algoritma Baru

Untuk menambah algoritma baru (misal: Dijkstra), cukup 2 langkah:

### 1. Buat class yang implements `GraphAlgorithm`

```java
package com.grafapp.algorithm.impl;

import com.grafapp.algorithm.*;
import com.grafapp.model.Graph;
import java.util.*;

public class DijkstraAlgorithm implements GraphAlgorithm {
    @Override public String getName() { return "Dijkstra's Algorithm"; }
    @Override public String getCategory() { return "Shortest Path"; }
    @Override public String getDescription() { return "Shortest path dari source ke semua node."; }

    @Override
    public List<ParameterInfo> getRequiredParameters() {
        return Arrays.asList(
            new ParameterInfo("startNode", "Source Node", ParameterInfo.Type.NODE_SELECT, 0, true)
        );
    }

    @Override
    public AlgorithmResult execute(Graph graph, Map<String, Object> parameters) {
        List<AlgorithmStep> steps = new ArrayList<>();
        // ... implementasi Dijkstra ...
        // Gunakan AlgorithmStep.visitNode(), traverseEdge(), dll.
        return new AlgorithmResult(steps, "Dijkstra selesai.");
    }
}
```

### 2. Daftarkan di `AlgorithmRegistry`

```java
// Di constructor AlgorithmRegistry:
register(new DijkstraAlgorithm());
```

Tidak perlu mengubah UI — sidebar dan panel kontrol otomatis menyesuaikan!

## Design Patterns

| Pattern            | Penggunaan                                                                         |
| ------------------ | ---------------------------------------------------------------------------------- |
| **Strategy**       | `GraphAlgorithm` interface — setiap algoritma adalah strategy yang interchangeable |
| **Registry**       | `AlgorithmRegistry` — katalog terpusat untuk semua algoritma                       |
| **Observer**       | JavaFX Properties + bindings untuk reaktivitas UI                                  |
| **Factory Method** | `AlgorithmStep` static factory methods untuk type-safe step creation               |

## Optimasi Performa

- **Canvas-based rendering** (immediate mode) — lebih efisien dari Scene Graph untuk graf besar
- **AnimationTimer** untuk physics loop — berjalan di JavaFX Application Thread tanpa overhead thread sync
- **Cooling schedule** pada force-directed layout — simulasi konvergen dan CPU usage menurun seiring waktu
- **Coordinate transform** — zoom/pan menggunakan transform matrix di Canvas, bukan memindahkan node
