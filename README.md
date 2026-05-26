# Proyek Akhir MA3052 Teori Graf Algoritmik

Proyek ini adalah aplikasi visualisasi algoritma graf interaktif yang dirancang untuk membantu pemahaman konsep-konsep teori graf. Repositori ini menyediakan dua platform implementasi:
1. **Aplikasi Desktop (JavaFX)**: Visualisasi graf dengan rendering kanvas berbasis JavaFX dan simulasi *step-by-step*.
2. **Aplikasi Web (Next.js & TypeScript)**: Antarmuka modern, interaktif, responsif, dan mudah diakses langsung melalui peramban web (browser).

Visualizer ini mendukung visualisasi berbagai algoritma graf seperti DFS, BFS, Pencarian Lintasan Terpendek (Dijkstra), MST (Prim, Kruskal), Graph Bandwidth Optimization, Timetabling (Scheduling), Island Count, dan masih banyak lagi.

---

## Anggota Kelompok
* Kevin Wirya Valerian - 13524019
* Jingglang Galih Rinenggan - 13524095

---
# 🔗 Akses Cepat
https://graf-algoritmik-web.vercel.app/

## 🖥️ Cara Menjalankan Aplikasi Desktop (JavaFX)

Seluruh berkas kode sumber desktop berada di folder `integrated-app/`.

### Windows (menggunakan Batch Script)
1. **Kompilasi Aplikasi**:
   Masuk ke folder `integrated-app/` lalu jalankan berkas `build.bat`:
   ```cmd
   cd integrated-app
   build.bat
   ```
2. **Jalankan Aplikasi**:
   Setelah kompilasi berhasil, jalankan berkas `launch.bat`:
   ```cmd
   launch.bat
   ```

### macOS / Linux (menggunakan Shell Script)
1. Berikan izin eksekusi pada berkas `.sh` jika belum diberikan:
   ```bash
   cd integrated-app
   chmod +x build.sh launch.sh run_mac.sh
   ```
2. **Kompilasi Aplikasi**:
   Jalankan skrip kompilasi:
   ```bash
   ./build.sh
   ```
3. **Jalankan Aplikasi**:
   Jalankan aplikasi desktop dengan:
   ```bash
   ./launch.sh
   ```
   *(Khusus pengguna macOS, Anda juga bisa menggunakan `./run_mac.sh` jika diperlukan).*

---

## 🌐 Cara Menjalankan Aplikasi Web (Next.js & TypeScript)

Seluruh berkas kode sumber web berada di folder `web/` dan memerlukan runtime **Node.js** terinstal di sistem Anda.

1. **Masuk ke folder web**:
   ```bash
   cd web
   ```
2. **Instalasi Dependensi**:
   ```bash
   npm install
   ```
3. **Jalankan Development Server**:
   ```bash
   npm run dev
   ```
4. **Buka Aplikasi**:
   Buka peramban web Anda dan akses alamat yang tertera (biasanya [http://localhost:3000](http://localhost:3000) atau [http://localhost:3001](http://localhost:3001)).

### Kompilasi untuk Produksi
Jika ingin melakukan build produksi:
```bash
npm run build
npm run start
```
