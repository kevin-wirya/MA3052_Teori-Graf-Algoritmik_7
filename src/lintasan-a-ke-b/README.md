# Path Finder - DFS Path Finding Application

## Deskripsi

Aplikasi ini menggunakan algoritma Depth First Search (DFS) untuk menemukan lintasan (path) dari vertex A ke vertex B dalam sebuah graf.

## Fitur

- **Path Finding**: Menemukan jalur dari vertex awal ke vertex akhir menggunakan DFS
- **Visualisasi Interaktif**: Menggunakan GUI Swing untuk menampilkan proses pencarian secara visual dan animasi
- **Input Fleksibel**: Membaca graf dari file test case

## Format File Input

Format file test case:

```
N M
u1 v1
u2 v2
...
uM vM
A
B
```

Dimana:

- `N` = jumlah vertex (0 hingga N-1)
- `M` = jumlah edge
- `u1 v1` hingga `uM vM` = edges dalam graf
- `A` = vertex awal (starting vertex)
- `B` = vertex akhir (end vertex)

## Contoh File Test

File test case disimpan di folder `data/`:

- `path_1.txt` - Contoh 1: Cari lintasan dari 0 ke 6
- `path_2.txt` - Contoh 2: Cari lintasan dari 0 ke 4
- `path_3.txt` - Contoh 3: Cari lintasan dari 0 ke 5

## Cara Menjalankan

### Compile

```bash
cd src/lintasan-a-ke-b
javac PathFinder.java PathFinderVisual.java
```

### Run

```bash
java PathFinder
```

Kemudian masukkan nama file test case (tanpa extension `.txt`):

```
Masukkan nama file (tanpa .txt): path_1
```

## Kontrol Visualisasi

- **SPACE**: Mulai animasi visualisasi
- **R**: Reset animasi ke keadaan awal

## Warna Node dalam Visualisasi

- **Orange**: Starting Node (vertex awal/A)
- **Red**: End Node (vertex akhir/B)
- **Green**: Visited Node (node yang sudah dikunjungi dalam lintasan)
- **White**: Unvisited Node (node yang belum dikunjungi)

## Output Program

Program akan menampilkan:

1. Jumlah edges yang dibaca dari file
2. Lintasan yang ditemukan (jika ada) dalam format: `A -> ... -> B`
3. Visualisasi animasi dari proses pencarian path

Jika tidak ada lintasan, program akan menampilkan: "Tidak ada lintasan dari vertex A ke B"

## Struktur Kode

### PathFinder.java

- `dfsRecursive()`: Fungsi rekursif DFS untuk mencari path
- `findPath()`: Wrapper function untuk memulai pencarian
- `main()`: Input file dan menampilkan hasil

### PathFinderVisual.java

- `PathFinderVisual`: JFrame untuk menampilkan UI utama
- `GraphPanel`: JPanel untuk menggambar graf dan animasi pencarian path
