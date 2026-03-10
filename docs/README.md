# Implementasi Depth-First Search (DFS) dengan Visualisasi

## Deskripsi
Proyek ini mengimplementasikan algoritma **Depth-First Search (DFS)** dalam bahasa Java beserta visualisasinya menggunakan Java Swing. Implementasi mencakup pendekatan rekursif dan iteratif, serta berbagai aplikasi DFS seperti pencarian jalur dan analisis konektivitas graf.

## Struktur Proyek

```
Tugas-Kelompok/
├── README.md                     # File dokumentasi utama
├── src/
│   └── dfs/                      # Package DFS
│       ├── Graph.java            # Representasi graf
│       ├── DFS.java              # Implementasi algoritma DFS
│       ├── DFSVisualizer.java    # Visualisasi menggunakan Swing
│       └── DFSMain.java          # Kelas utama dan menu
├── tests/
│   └── DFSTest.java              # Unit tests untuk DFS
├── scripts/
│   ├── run_dfs.bat               # Script Windows untuk menjalankan program
│   ├── run_dfs.sh                # Script Unix/Linux untuk menjalankan program
│   ├── run_tests.bat             # Script Windows untuk menjalankan tests
│   └── run_tests.sh              # Script Unix/Linux untuk menjalankan tests
├── docs/
│   ├── README.md                 # Dokumentasi lengkap (file ini)
│   └── API_Documentation.md      # Dokumentasi API
├── examples/
│   └── sample_graphs.md          # Contoh-contoh graf
├── build/                        # Direktori output kompilasi
└── data/                         # Data graf (jika diperlukan)
```

## Fitur Utama

### 1. Implementasi Algoritma
- **DFS Rekursif**: Implementasi menggunakan rekursi dan call stack
- **DFS Iteratif**: Implementasi menggunakan explicit stack
- **Pencarian Jalur**: Mencari path antara dua vertex
- **Analisis Konektivitas**: Mengecek apakah graf terhubung
- **Penghitungan Komponen**: Menghitung jumlah komponen terhubung

### 2. Visualisasi Grafis
- Tampilan visual graf dengan node dan edge
- Animasi step-by-step proses DFS
- Indikator visual untuk node yang sedang dikunjungi
- Kontrol interaktif untuk memilih vertex awal
- Legend dan informasi status real-time

### 3. Interface Pengguna
- Menu interaktif di console
- Graf contoh yang siap pakai
- Opsi untuk membuat graf custom
- Integrasi seamless dengan visualisasi GUI

### 4. Testing Framework
- Unit tests comprehensif
- Test untuk berbagai jenis graf
- Test edge cases dan error handling
- Automated testing via scripts

## Instalasi dan Penggunaan

### Persyaratan Sistem
- Java Development Kit (JDK) 8 atau lebih tinggi
- Operating System: Windows, Linux, atau macOS
- Memory: Minimum 512MB RAM

### Cara Menjalankan

#### Opsi 1: Menggunakan Script (Recommended)

**Windows:**
```batch
cd scripts
run_dfs.bat
```

**Linux/macOS:**
```bash
cd scripts
chmod +x run_dfs.sh
./run_dfs.sh
```

#### Opsi 2: Kompilasi Manual
```bash
# Buat direktori build
mkdir build

# Kompilasi
javac -d build src/dfs/*.java

# Jalankan
java -cp build dfs.DFSMain
```

#### Opsi 3: Menjalankan Tests
**Windows:**
```batch
cd scripts
run_tests.bat
```

**Linux/macOS:**
```bash
cd scripts
chmod +x run_tests.sh
./run_tests.sh
```

## Panduan Penggunaan

### 1. Menu Utama
Program akan menampilkan menu dengan opsi:
1. **Demo dengan Graf Contoh** - Menjalankan demo dengan graf yang sudah dibuat
2. **Buat Graf Custom** - Membuat graf sesuai keinginan pengguna
3. **Keluar** - Menutup program

### 2. Graf Contoh Default
Graf contoh memiliki 6 vertex (0-5) dengan struktur:
```
     0
   /   \
  1     2
 /|\    |
3 4 3---5
```

### 3. Visualisasi Interactive
Setelah menjalankan DFS, window visualisasi akan terbuka dengan fitur:
- **Start Vertex**: Input field untuk memilih vertex awal
- **DFS Rekursif**: Button untuk animasi DFS rekursif
- **DFS Iteratif**: Button untuk animasi DFS iteratif
- **Reset**: Button untuk mereset visualisasi

### 4. Color Coding System
- **Abu-abu**: Node yang belum dikunjungi
- **Hijau**: Node yang sudah dikunjungi
- **Merah**: Node yang sedang dikunjungi saat ini

## Spesifikasi Teknis

### Kompleksitas Algoritma

| Operasi | Time Complexity | Space Complexity |
|---------|----------------|------------------|
| DFS Traversal | O(V + E) | O(V) |
| Path Finding | O(V + E) | O(V) |
| Connectivity Check | O(V + E) | O(V) |
| Component Count | O(V + E) | O(V) |

*V = jumlah vertex, E = jumlah edge*

### Struktur Data
- **Graf**: Adjacency List menggunakan `LinkedList<Integer>[]`
- **DFS Stack**: `java.util.Stack<Integer>` untuk versi iteratif
- **Visited Tracking**: `boolean[]` array untuk tracking node yang dikunjungi

## API Documentation

### Kelas Utama

#### `Graph.java`
- **Constructor**: `Graph(int vertices)`
- **addEdge()**: Menambah edge undirected
- **addDirectedEdge()**: Menambah edge directed
- **getNeighbors()**: Mendapat daftar tetangga vertex
- **printGraph()**: Menampilkan representasi graf

#### `DFS.java`
- **dfsRecursive()**: DFS dengan pendekatan rekursif
- **dfsIterative()**: DFS dengan pendekatan iteratif
- **findPath()**: Mencari jalur antara dua vertex
- **isConnected()**: Cek konektivitas graf
- **countConnectedComponents()**: Hitung jumlah komponen

#### `DFSVisualizer.java`
- **startDFSAnimation()**: Mulai animasi DFS
- **reset()**: Reset state visualisasi
- **createVisualizationWindow()**: Buat window GUI

## Contoh Output

```
=== DEMONSTRASI DFS ===

1. DFS REKURSIF dari vertex 0:
-----------------------------
Memulai DFS Rekursif dari vertex 0
Mengunjungi vertex: 0
Melanjutkan ke vertex: 1 dari vertex: 0
Mengunjungi vertex: 1
Melanjutkan ke vertex: 3 dari vertex: 1
Mengunjungi vertex: 3
Melanjutkan ke vertex: 4 dari vertex: 3
Mengunjungi vertex: 4
Urutan kunjungan: [0, 1, 3, 4, 2, 5]

2. ANALISIS KONEKTIVITAS:
------------------------
Graf terhubung: Ya
Jumlah komponen terhubung: 1
```

## Testing dan Quality Assurance

### Test Coverage
- ✅ Basic DFS functionality (recursive & iterative)
- ✅ Different graph types (linear, star, complete, cycle)
- ✅ Path finding algorithms
- ✅ Connectivity analysis
- ✅ Edge cases (single vertex, empty graph, disconnected components)

### Menjalankan Tests
```bash
# Windows
scripts\run_tests.bat

# Unix/Linux
scripts/run_tests.sh
```

## Aplikasi dan Use Cases

1. **Graph Traversal**: Mengunjungi semua node dalam graf
2. **Pathfinding**: Menemukan jalur antara dua node
3. **Cycle Detection**: Mengidentifikasi siklus dalam graf
4. **Topological Sorting**: Mengurutkan node secara topologi
5. **Connected Components**: Mengidentifikasi komponen terhubung
6. **Maze Solving**: Menyelesaikan labirin 2D
7. **Game AI**: Algoritma untuk puzzle dan strategy games

## Kontribusi dan Development

### Roadmap
- [ ] Support untuk weighted graphs
- [ ] Implementasi algoritma graf tambahan (BFS, Dijkstra)
- [ ] Export hasil ke format file (JSON, XML)
- [ ] 3D visualization option
- [ ] Performance benchmarking tools
- [ ] Interactive graph builder GUI

### Cara Berkontribusi
1. Fork repository
2. Buat feature branch
3. Commit changes dengan deskriptif message
4. Submit pull request dengan dokumentasi

## Lisensi dan Credits

Proyek ini dibuat untuk keperluan akademis dan pembelajaran algoritma graf.

**Authors:**
- Tim Teori Graf Algoritmik
- IF Semester 4

**Dependencies:**
- Java Standard Library
- Java Swing (built-in)

## Troubleshooting

### Common Issues

**Q: Program tidak bisa dikompilasi**  
A: Pastikan JDK terinstall dan PATH sudah dikonfigurasi dengan benar

**Q: Visualisasi tidak muncul**  
A: Pastikan sistem mendukung Java GUI (X11 forwarding untuk SSH)

**Q: OutOfMemoryError untuk graf besar**  
A: Tingkatkan heap size: `java -Xmx1G -cp build dfs.DFSMain`

### Logging dan Debug
Untuk debugging, tambahkan flag verbose:
```bash
java -verbose:gc -cp build dfs.DFSMain
```

---

*Last updated: February 25, 2026*