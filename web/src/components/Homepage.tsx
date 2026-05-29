"use client";

interface HomepageProps {
  onNavigate: (tab: "graph" | "geotsp" | "flights") => void;
}

export default function Homepage({ onNavigate }: HomepageProps) {
  const name1 = "Kevin Wirya Valerian";
  const nim1 = "13524019";
  const photo1 = "/foto-kevin.jpg";

  const name2 = "Jingglang Galih Rinenggan";
  const nim2 = "13524095";
  const photo2 = "/logo.png";

  return (
    <div className="flex-1 flex flex-col justify-between py-6 px-4 max-w-6xl mx-auto w-full gap-8 animate-rise">
      {/* Hero Header Section */}
      <section className="text-center space-y-3 mt-4">
        <div className="inline-flex items-center gap-2 bg-accent/10 border border-accent/20 px-3 py-1 rounded-full text-xs font-bold text-accent">
          <span>🎓</span>
          <span>Proyek Akhir MA3052 Teori Graf Algoritmik</span>
        </div>
        <h1 className="text-3xl md:text-5xl font-bold tracking-tight text-ink">
          Interactive Graph <span className="text-accent">Visualizer</span> & <span className="text-accent-warm font-sans">Solver</span>
        </h1>
        <p className="text-sm md:text-base text-inkMuted max-w-2xl mx-auto leading-relaxed">
          Eksplorasi berbagai algoritma graf secara interaktif pada kanvas kustom, atau selesaikan Traveling Salesperson Problem (TSP) menggunakan koordinat kota riil di seluruh dunia.
        </p>
      </section>

      {/* Main Feature Cards Grid */}
      <section className="grid grid-cols-1 md:grid-cols-3 gap-6 md:gap-8 items-stretch w-full max-w-5xl mx-auto my-2">
        {/* Card 1: Visualisasi Graf Umum */}
        <div 
          onClick={() => onNavigate("graph")}
          className="bg-panel border border-border hover:border-accent/40 rounded-3xl p-6 flex flex-col justify-between shadow-panel hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-1 cursor-pointer group relative overflow-hidden"
        >
          {/* Top Decorative Vector */}
          <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity pointer-events-none">
            <svg width="120" height="120" viewBox="0 0 100 100" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="20" cy="30" r="8" />
              <circle cx="80" cy="20" r="8" />
              <circle cx="50" cy="80" r="8" />
              <line x1="28" y1="28" x2="72" y2="22" />
              <line x1="20" y1="38" x2="42" y2="76" />
              <line x1="76" y1="26" x2="54" y2="74" />
            </svg>
          </div>

          <div className="space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-accent/10 border border-accent/20 flex items-center justify-center text-2xl text-accent group-hover:scale-110 transition-transform duration-300">
              📊
            </div>
            <div>
              <h2 className="text-xl font-bold text-ink group-hover:text-accent transition-colors">
                Visualisasi Graf Umum
              </h2>
              <p className="text-xs text-inkMuted mt-2 leading-relaxed">
                Buat simpul (node) dan sisi (edge) kustom pada kanvas digital. Jalankan dan simulasikan algoritma traversal (BFS, DFS), Shortest Path (Dijkstra), Minimum Spanning Tree (Prim, Kruskal), Island Count, Bandwidth, serta Bipartite Matching dan Timetable Scheduling secara step-by-step.
              </p>
            </div>
          </div>

          <div className="mt-8 flex items-center gap-2 text-xs font-bold text-accent group-hover:translate-x-1.5 transition-transform duration-300">
            <span>Buka Aplikasi Visualisasi</span>
            <span>→</span>
          </div>
        </div>

        {/* Card 2: WorldTSP */}
        <div 
          onClick={() => onNavigate("geotsp")}
          className="bg-panel border border-border hover:border-accent-warm/40 rounded-3xl p-6 flex flex-col justify-between shadow-panel hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-1 cursor-pointer group relative overflow-hidden"
        >
          {/* Top Decorative Vector */}
          <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity pointer-events-none">
            <svg width="120" height="120" viewBox="0 0 100 100" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="50" cy="50" r="40" strokeDasharray="4 4" />
              <path d="M20,50 Q50,20 80,50" />
              <path d="M20,50 Q50,80 80,50" strokeDasharray="2 2" />
            </svg>
          </div>

          <div className="space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-accent-warm/10 border border-accent-warm/20 flex items-center justify-center text-2xl text-accent-warm group-hover:scale-110 transition-transform duration-300">
              🌍
            </div>
            <div>
              <h2 className="text-xl font-bold text-ink group-hover:text-accent-warm transition-colors">
                WorldTSP Solver
              </h2>
              <p className="text-xs text-inkMuted mt-2 leading-relaxed">
                Pecahkan permasalahan rute terpendek berkeliling dunia (Traveling Salesperson Problem) menggunakan data lokasi kota asli. Visualisasikan rute dalam proyeksi peta datar 2D (Leaflet) maupun rupa bumi bulat 3D Globe (WebGL) dengan algoritma Greedy, 2-Opt, atau Held-Karp.
              </p>
            </div>
          </div>

          <div className="mt-8 flex items-center gap-2 text-xs font-bold text-accent-warm group-hover:translate-x-1.5 transition-transform duration-300">
            <span>Mulai WorldTSP Solver</span>
            <span>→</span>
          </div>
        </div>

        {/* Card 3: Flight Case Study */}
        <div 
          onClick={() => onNavigate("flights")}
          className="bg-panel border border-border hover:border-accent/40 rounded-3xl p-6 flex flex-col justify-between shadow-panel hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-1 cursor-pointer group relative overflow-hidden"
        >
          {/* Top Decorative Vector */}
          <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity pointer-events-none">
            <svg width="120" height="120" viewBox="0 0 100 100" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M10,80 Q50,20 90,80" />
              <path d="M10,80 C30,40 70,40 90,80" strokeDasharray="3 3" />
            </svg>
          </div>

          <div className="space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-accent/10 border border-accent/20 flex items-center justify-center text-2xl text-accent group-hover:scale-110 transition-transform duration-300">
              ✈️
            </div>
            <div>
              <h2 className="text-xl font-bold text-ink group-hover:text-accent transition-colors">
                Penerbangan Nusantara
              </h2>
              <p className="text-xs text-inkMuted mt-2 leading-relaxed">
                Studi kasus rute penerbangan domestik riil Indonesia. Analisis derajat konektivitas bandara (centrality) dan temukan jalur transit terpendek (BFS & Dijkstra) atau gambarkan Kruskal Minimum Spanning Tree.
              </p>
            </div>
          </div>

          <div className="mt-8 flex items-center gap-2 text-xs font-bold text-accent group-hover:translate-x-1.5 transition-transform duration-300">
            <span>Buka Analisis Penerbangan</span>
            <span>→</span>
          </div>
        </div>
      </section>

      {/* Credentials Card Section */}
      <section className="w-full max-w-2xl mx-auto bg-panel/95 backdrop-blur-xs border border-border shadow-panel rounded-3xl p-6 mb-4 relative transition-all duration-300">
        <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted mb-4 text-center">
          Identitas Pengembang (Kelompok 7)
        </h3>

        <div className="flex flex-col sm:flex-row gap-6 items-center sm:items-stretch justify-center">
          {/* Developer 1: Kevin */}
          <div className="flex flex-1 items-center gap-4 min-w-0">
            {/* Circular Profile Photo Frame */}
            <div className="relative shrink-0">
              <div className="w-20 h-20 rounded-full border-2 border-accent/25 bg-panel-soft flex items-center justify-center overflow-hidden relative shadow-sm">
                <img src={photo1} alt={name1} className="w-full h-full object-cover rounded-full" />
              </div>
            </div>

            {/* Profile Info */}
            <div className="flex-1 min-w-0 text-left">
              <h4 className="text-sm font-bold text-ink truncate leading-tight">{name1}</h4>
              <p className="text-xs font-semibold font-mono text-accent">{nim1}</p>
              <p className="text-[10px] text-inkMuted font-medium mt-1">Mahasiswa Teknik Informatika ITB</p>
            </div>
          </div>

          {/* Vertical Divider */}
          <div className="hidden sm:block w-[1px] bg-border/50 self-stretch my-1"></div>
          {/* Horizontal Divider for mobile */}
          <div className="sm:hidden w-full h-[1px] bg-border/30"></div>

          {/* Developer 2: Jingglang */}
          <div className="flex flex-1 items-center gap-4 min-w-0">
            {/* Circular Profile Photo Frame */}
            <div className="relative shrink-0">
              <div className="w-20 h-20 rounded-full border-2 border-accent-warm/25 bg-panel-soft flex items-center justify-center overflow-hidden relative shadow-sm">
                <img src={photo2} alt={name2} className="w-full h-full object-cover rounded-full" />
              </div>
            </div>

            {/* Profile Info */}
            <div className="flex-1 min-w-0 text-left">
              <h4 className="text-sm font-bold text-ink truncate leading-tight">{name2}</h4>
              <p className="text-xs font-semibold font-mono text-accent-warm">{nim2}</p>
              <p className="text-[10px] text-inkMuted font-medium mt-1">Mahasiswa Teknik Informatika ITB</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
