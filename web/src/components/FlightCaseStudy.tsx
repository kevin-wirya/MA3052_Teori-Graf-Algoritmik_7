"use client";

import { useState, useEffect, useMemo } from "react";
import dynamic from "next/dynamic";
import flightDataRaw from "../../indonesian_flights.json";

interface Airport {
  id: string;
  name: string;
  city: string;
  iata: string;
  lat: number;
  lon: number;
}

interface Route {
  src: string;
  dest: string;
  airline: string;
}

interface FlightSchedule {
  airline: string;
  flightNumber: string;
  depTime: string;
  arrTime: string;
  durationStr: string;
}

// Convert raw JSON data securely
const flightData = flightDataRaw as {
  airports: Airport[];
  routes: Route[];
};

// Determine local timezone label and offset based on Indonesian airport longitude
function getAirportTimezone(lon: number): { label: string; offset: number } {
  if (lon < 110) {
    return { label: "WIB", offset: 7 }; // Western Indonesia (UTC+7)
  } else if (lon < 130) {
    return { label: "WITA", offset: 8 }; // Central Indonesia (UTC+8)
  } else {
    return { label: "WIT", offset: 9 }; // Eastern Indonesia (UTC+9)
  }
}

// Calculate Haversine distance in km
function haversineDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371; // Earth's radius in km
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

// Generate stable realistic schedules for a flight leg
function generateSchedules(src: Airport, dest: Airport): FlightSchedule[] {
  const dist = haversineDistance(src.lat, src.lon, dest.lat, dest.lon);
  // Cruise speed ~800 km/h + 30 mins buffer for takeoff/landing
  const durationMin = Math.round((dist / 800) * 60) + 30;
  const hours = Math.floor(durationMin / 60);
  const mins = durationMin % 60;
  const durationStr = `${hours > 0 ? `${hours}j ` : ""}${mins}m`;

  const srcTz = getAirportTimezone(src.lon);
  const destTz = getAirportTimezone(dest.lon);

  // Stable seed based on IATA codes to keep flight list consistent
  const seed = src.iata.charCodeAt(0) + dest.iata.charCodeAt(0) + src.iata.charCodeAt(1);
  
  const airlines = [
    { name: "Garuda Indonesia", code: "GA" },
    { name: "Citilink", code: "QG" },
    { name: "Batik Air", code: "ID" },
    { name: "Lion Air", code: "JT" },
    { name: "Pelita Air", code: "IP" }
  ];

  const schedules: FlightSchedule[] = [];
  const startHours = [7, 12, 17]; // Morning, Afternoon, Evening flight departures

  for (let i = 0; i < 3; i++) {
    const airlineIdx = (seed + i) % airlines.length;
    const airline = airlines[airlineIdx];
    const flightNum = `${airline.code}-${100 + (seed % 800) + i * 15}`;
    
    // Stable departure hour/minute
    const depHour = (startHours[i] + (seed % 3)) % 24;
    const depMin = ((seed * (i + 1)) % 12) * 5;
    const depTimeStr = `${String(depHour).padStart(2, "0")}:${String(depMin).padStart(2, "0")} ${srcTz.label}`;

    // Calculate arrival hour/minute taking timezone offset differences into account
    const tzDiff = destTz.offset - srcTz.offset;
    const arrHourRaw = depHour + hours + tzDiff;
    const arrMinRaw = depMin + mins;
    
    const arrHour = (arrHourRaw + Math.floor(arrMinRaw / 60) + 24) % 24;
    const arrMin = arrMinRaw % 60;
    const arrTimeStr = `${String(arrHour).padStart(2, "0")}:${String(arrMin).padStart(2, "0")} ${destTz.label}`;

    schedules.push({
      airline: airline.name,
      flightNumber: flightNum,
      depTime: depTimeStr,
      arrTime: arrTimeStr,
      durationStr
    });
  }

  return schedules;
}

// Disjoint Set Union (DSU) for Kruskal's MST
class DSU {
  parent: Record<string, string> = {};

  constructor(elements: string[]) {
    elements.forEach(e => {
      this.parent[e] = e;
    });
  }

  find(i: string): string {
    if (this.parent[i] === i) return i;
    this.parent[i] = this.find(this.parent[i]); // Path compression
    return this.parent[i];
  }

  union(i: string, j: string): boolean {
    const rootI = this.find(i);
    const rootJ = this.find(j);
    if (rootI !== rootJ) {
      this.parent[rootI] = rootJ;
      return true;
    }
    return false;
  }
}

// Dynamic import of FlightMap to disable SSR
const FlightMap = dynamic(() => import("./FlightMap"), {
  ssr: false,
  loading: () => (
    <div className="flex-1 bg-panel-soft flex flex-col items-center justify-center text-inkMuted text-xs font-semibold gap-3 min-h-[400px]">
      <span className="animate-spin text-lg">🌍</span>
      <span>Memuat Peta Penerbangan Nusantara...</span>
    </div>
  )
});

export default function FlightCaseStudy() {
  const [srcIata, setSrcIata] = useState<string>("CGK");
  const [destIata, setDestIata] = useState<string>("DJJ");
  const [routingType, setRoutingType] = useState<"dijkstra" | "bfs">("dijkstra");
  
  const [shortestPath, setShortestPath] = useState<string[]>([]);
  const [pathDistance, setPathDistance] = useState<number>(0);
  const [pathError, setPathError] = useState<string | null>(null);

  const [selectedHub, setSelectedHub] = useState<string | null>(null);
  const [showMST, setShowMST] = useState<boolean>(false);
  const [showAllRoutes, setShowAllRoutes] = useState<boolean>(true);

  const airports = useMemo(() => flightData.airports, []);
  const routes = useMemo(() => flightData.routes, []);

  // Sort airports by City Name for the dropdown select list
  const sortedAirports = useMemo(() => {
    return [...airports].sort((a, b) => a.city.localeCompare(b.city));
  }, [airports]);

  // Build Bidirectional Adjacency List for Routing
  const adjList = useMemo(() => {
    const adj: Record<string, { dest: string; dist: number }[]> = {};
    airports.forEach(a => {
      adj[a.iata] = [];
    });

    routes.forEach(r => {
      const srcAir = airports.find(a => a.iata === r.src);
      const destAir = airports.find(a => a.iata === r.dest);
      if (srcAir && destAir) {
        const dist = haversineDistance(srcAir.lat, srcAir.lon, destAir.lat, destAir.lon);
        // Ensure no duplicate edges in adjacency list
        if (!adj[r.src].some(item => item.dest === r.dest)) {
          adj[r.src].push({ dest: r.dest, dist });
        }
        if (!adj[r.dest].some(item => item.dest === r.src)) {
          adj[r.dest].push({ dest: r.src, dist });
        }
      }
    });
    return adj;
  }, [airports, routes]);

  // Calculate Centrality: number of connections per airport
  const hubCentrality = useMemo(() => {
    const counts: Record<string, number> = {};
    airports.forEach(a => {
      counts[a.iata] = 0;
    });

    routes.forEach(r => {
      if (counts[r.src] !== undefined) counts[r.src]++;
      if (counts[r.dest] !== undefined) counts[r.dest]++;
    });

    return Object.entries(counts)
      .map(([iata, count]) => {
        const airport = airports.find(a => a.iata === iata);
        return {
          iata,
          count,
          name: airport ? airport.name : iata,
          city: airport ? airport.city : ""
        };
      })
      .sort((a, b) => b.count - a.count)
      .slice(0, 5); // Get top 5 hubs
  }, [airports, routes]);

  // Compute Kruskal's Spanning Tree for Indonesia Flights
  const mstEdges = useMemo(() => {
    // 1. Gather all unique undirected edges
    const edgeMap = new Map<string, { u: string; v: string; weight: number }>();
    routes.forEach(r => {
      const key = [r.src, r.dest].sort().join("-");
      if (!edgeMap.has(key)) {
        const srcAir = airports.find(a => a.iata === r.src);
        const destAir = airports.find(a => a.iata === r.dest);
        if (srcAir && destAir) {
          const weight = haversineDistance(srcAir.lat, srcAir.lon, destAir.lat, destAir.lon);
          edgeMap.set(key, { u: r.src, v: r.dest, weight });
        }
      }
    });

    const allEdges = Array.from(edgeMap.values()).sort((a, b) => a.weight - b.weight);

    // 2. Filter airports with at least one connection
    const activeIatas = airports.filter(a => adjList[a.iata].length > 0).map(a => a.iata);
    const dsu = new DSU(activeIatas);
    const mst: { u: string; v: string; weight: number }[] = [];

    // 3. Connect components using Kruskal's
    allEdges.forEach(edge => {
      if (dsu.union(edge.u, edge.v)) {
        mst.push(edge);
      }
    });
    return mst;
  }, [airports, routes, adjList]);

  // Shortest Path Finder Executor
  const findPath = () => {
    setPathError(null);
    setSelectedHub(null);

    if (srcIata === destIata) {
      setShortestPath([srcIata]);
      setPathDistance(0);
      return;
    }

    if (routingType === "bfs") {
      // BFS Pathfinding (Least Transits)
      const queue: string[] = [srcIata];
      const visited = new Set<string>([srcIata]);
      const parent: Record<string, string> = {};

      let found = false;
      while (queue.length > 0) {
        const curr = queue.shift()!;
        if (curr === destIata) {
          found = true;
          break;
        }

        const neighbors = adjList[curr] || [];
        for (const neighbor of neighbors) {
          if (!visited.has(neighbor.dest)) {
            visited.add(neighbor.dest);
            parent[neighbor.dest] = curr;
            queue.push(neighbor.dest);
          }
        }
      }

      if (!found) {
        setPathError(`Tidak ada penerbangan penghubung antara ${srcIata} dan ${destIata}.`);
        setShortestPath([]);
        setPathDistance(0);
        return;
      }

      const path: string[] = [];
      let temp = destIata;
      while (temp) {
        path.push(temp);
        temp = parent[temp];
      }
      path.reverse();

      // Calculate total distance of reconstructed path
      let dist = 0;
      for (let i = 0; i < path.length - 1; i++) {
        const currentAir = airports.find(a => a.iata === path[i]);
        const nextAir = airports.find(a => a.iata === path[i + 1]);
        if (currentAir && nextAir) {
          dist += haversineDistance(currentAir.lat, currentAir.lon, nextAir.lat, nextAir.lon);
        }
      }

      setShortestPath(path);
      setPathDistance(dist);

    } else {
      // Dijkstra Pathfinding (Shortest Physical Distance)
      const dists: Record<string, number> = {};
      const parent: Record<string, string> = {};
      const visited = new Set<string>();

      airports.forEach(a => {
        dists[a.iata] = Infinity;
      });
      dists[srcIata] = 0;

      while (true) {
        let u: string | null = null;
        let minDist = Infinity;

        airports.forEach(a => {
          if (!visited.has(a.iata) && dists[a.iata] < minDist) {
            minDist = dists[a.iata];
            u = a.iata;
          }
        });

        if (u === null || u === destIata || minDist === Infinity) break;
        visited.add(u);

        const neighbors = adjList[u] || [];
        for (const neighbor of neighbors) {
          if (!visited.has(neighbor.dest)) {
            const alt = dists[u] + neighbor.dist;
            if (alt < dists[neighbor.dest]) {
              dists[neighbor.dest] = alt;
              parent[neighbor.dest] = u;
            }
          }
        }
      }

      if (dists[destIata] === Infinity) {
        setPathError(`Tidak dapat menghitung rute terpendek antara ${srcIata} dan ${destIata}.`);
        setShortestPath([]);
        setPathDistance(0);
        return;
      }

      const path: string[] = [];
      let temp = destIata;
      while (temp) {
        path.push(temp);
        temp = parent[temp];
      }
      path.reverse();

      setShortestPath(path);
      setPathDistance(dists[destIata]);
    }
  };

  // Run automatically when starting/changing points
  useEffect(() => {
    findPath();
  }, [srcIata, destIata, routingType]);

  return (
    <div className="flex flex-col lg:flex-row flex-1 gap-4 min-h-0 w-full overflow-y-auto lg:overflow-hidden pb-4 lg:pb-0 px-4">
      {/* Sidebar Control Panel */}
      <div className="w-full lg:w-80 shrink-0 flex flex-col gap-4 overflow-y-auto pr-1">
        {/* Module Header Card */}
        <div className="bg-panel border border-border shadow-panel rounded-3xl p-5">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-xl">🇮🇩</span>
            <h2 className="text-base font-bold text-ink leading-tight">Peta Penerbangan Nusantara</h2>
          </div>
          <p className="text-[11px] text-inkMuted leading-relaxed">
            Studi kasus analisis graf dari rute penerbangan domestik riil Indonesia menggunakan algoritma pencarian rute terpendek dan MST.
          </p>
        </div>

        {/* Pathfinder Panel */}
        <div className="bg-panel border border-border shadow-panel rounded-3xl p-5 space-y-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-inkMuted">Shortest Path Router</h3>
          
          {/* Form Selectors */}
          <div className="space-y-3">
            <div>
              <label className="text-[10px] font-bold text-inkMuted block mb-1">BANDARA ASAL</label>
              <select
                value={srcIata}
                onChange={e => setSrcIata(e.target.value)}
                className="w-full text-xs font-medium border border-border rounded-xl px-3 py-2 bg-panel-soft text-ink focus:outline-none focus:border-accent"
              >
                {sortedAirports.map(a => (
                  <option key={a.iata} value={a.iata}>
                    {a.city} ({a.iata})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-[10px] font-bold text-inkMuted block mb-1">BANDARA TUJUAN</label>
              <select
                value={destIata}
                onChange={e => setDestIata(e.target.value)}
                className="w-full text-xs font-medium border border-border rounded-xl px-3 py-2 bg-panel-soft text-ink focus:outline-none focus:border-accent"
              >
                {sortedAirports.map(a => (
                  <option key={a.iata} value={a.iata}>
                    {a.city} ({a.iata})
                  </option>
                ))}
              </select>
            </div>

            {/* Algorithm Metric Toggle */}
            <div>
              <label className="text-[10px] font-bold text-inkMuted block mb-2">KRITERIA RUTE</label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  onClick={() => setRoutingType("dijkstra")}
                  className={`py-1.5 px-2 rounded-xl text-[10px] font-bold border transition-all ${
                    routingType === "dijkstra"
                      ? "bg-accent border-accent text-white shadow-sm"
                      : "bg-panel-soft border-border text-ink hover:bg-slate-100"
                  }`}
                >
                  Jarak (Dijkstra)
                </button>
                <button
                  onClick={() => setRoutingType("bfs")}
                  className={`py-1.5 px-2 rounded-xl text-[10px] font-bold border transition-all ${
                    routingType === "bfs"
                      ? "bg-accent border-accent text-white shadow-sm"
                      : "bg-panel-soft border-border text-ink hover:bg-slate-100"
                  }`}
                >
                  Transit Min (BFS)
                </button>
              </div>
            </div>
          </div>

          {/* Results Summary */}
          <div className="pt-2 border-t border-border/50">
            {pathError ? (
              <div className="text-[10px] font-bold text-red-600 bg-red-50 p-2 rounded-xl border border-red-100">
                ⚠️ {pathError}
              </div>
            ) : shortestPath.length > 0 ? (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-2 bg-panel-soft p-3 rounded-2xl border border-border/30">
                  <div>
                    <span className="text-[9px] font-bold text-inkMuted uppercase block">TOTAL JARAK</span>
                    <span className="text-xs font-bold text-ink">{Math.round(pathDistance).toLocaleString()} km</span>
                  </div>
                  <div>
                    <span className="text-[9px] font-bold text-inkMuted uppercase block">JUMLAH TRANSIT</span>
                    <span className="text-xs font-bold text-ink">
                      {shortestPath.length === 2
                        ? "Penerbangan Langsung"
                        : `${shortestPath.length - 2} Transit`}
                    </span>
                  </div>
                </div>

                {/* Vertical timeline details */}
                <div className="space-y-2 max-h-40 overflow-y-auto pr-1">
                  <span className="text-[9px] font-bold text-inkMuted uppercase block">URUTAN TRANSIT</span>
                  <div className="relative pl-4 border-l-2 border-slate-200 space-y-3 py-1">
                    {shortestPath.map((iata, idx) => {
                      const air = airports.find(a => a.iata === iata);
                      return (
                        <div key={iata} className="relative text-[10px] leading-tight">
                          {/* Dot marker */}
                          <div
                            className={`absolute -left-[21px] top-0.5 w-2 h-2 rounded-full border border-white ${
                              idx === 0
                                ? "bg-emerald-500 scale-125"
                                : idx === shortestPath.length - 1
                                ? "bg-accent-warm scale-125"
                                : "bg-accent"
                            }`}
                          ></div>
                          <div className="font-bold text-ink">{air?.city || iata} ({iata})</div>
                          <div className="text-[8px] text-inkMuted truncate">{air?.name}</div>
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Flight Schedules leg details */}
                <div className="space-y-2 pt-2 border-t border-border/40">
                  <span className="text-[9px] font-bold text-inkMuted uppercase block">Jadwal & Estimasi Waktu</span>
                  <div className="space-y-3 max-h-56 overflow-y-auto pr-1">
                    {shortestPath.slice(0, -1).map((iata, idx) => {
                      const srcAir = airports.find(a => a.iata === iata);
                      const destAir = airports.find(a => a.iata === shortestPath[idx + 1]);
                      if (!srcAir || !destAir) return null;
                      
                      const schedules = generateSchedules(srcAir, destAir);
                      return (
                        <div key={`${iata}-${destAir.iata}`} className="bg-panel-soft border border-border/40 p-2 rounded-xl space-y-2 text-[10px]">
                          <div className="flex justify-between items-center font-bold text-ink border-b border-border/30 pb-1">
                            <span className="truncate pr-1">✈️ Leg {idx + 1}: {srcAir.city} → {destAir.city}</span>
                            <span className="text-[9px] text-accent shrink-0 font-semibold">{schedules[0].durationStr}</span>
                          </div>
                          <div className="space-y-1.5">
                            {schedules.map((flight, fIdx) => (
                              <div key={fIdx} className="flex justify-between items-center bg-white p-1.5 rounded-lg border border-slate-100 gap-1">
                                <div className="min-w-0 flex-1">
                                  <div className="font-bold text-slate-800 truncate leading-none mb-0.5">{flight.airline}</div>
                                  <div className="text-[8px] text-slate-400 font-mono leading-none">{flight.flightNumber}</div>
                                </div>
                                <div className="text-right shrink-0">
                                  <div className="font-bold text-accent leading-none mb-0.5">{flight.depTime} - {flight.arrTime}</div>
                                  <div className="text-[8px] text-slate-400 leading-none">Waktu Lokal</div>
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>

              </div>
            ) : null}
          </div>
        </div>

        {/* Centrality Hub Analysis */}
        <div className="bg-panel border border-border shadow-panel rounded-3xl p-5 space-y-3">
          <h3 className="text-xs font-bold uppercase tracking-wider text-inkMuted">Analisis Hub Busiest (Centrality)</h3>
          <p className="text-[10px] text-inkMuted leading-relaxed">
            Bandara dengan jumlah koneksi rute penerbangan domestik terbanyak (Degree Centrality). Klik baris untuk menyorot jaringannya.
          </p>

          <div className="space-y-1.5">
            {hubCentrality.map(hub => (
              <div
                key={hub.iata}
                onClick={() => {
                  setShowMST(false);
                  setSelectedHub(selectedHub === hub.iata ? null : hub.iata);
                }}
                className={`flex justify-between items-center px-3 py-2 rounded-xl text-[10px] font-medium border cursor-pointer transition-all ${
                  selectedHub === hub.iata
                    ? "bg-blue-50 border-blue-200 text-blue-800 font-bold shadow-xs scale-[1.01]"
                    : "bg-panel-soft border-border/40 text-ink hover:bg-slate-50"
                }`}
              >
                <div className="truncate pr-2">
                  <span className="font-mono text-xs font-bold mr-1.5">{hub.iata}</span>
                  <span>{hub.city}</span>
                </div>
                <div className="shrink-0 bg-white border border-slate-200 px-1.5 py-0.5 rounded-full text-[9px] font-bold text-slate-600">
                  {hub.count} rute
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Minimum Spanning Tree Panel */}
        <div className="bg-panel border border-border shadow-panel rounded-3xl p-5 space-y-3">
          <h3 className="text-xs font-bold uppercase tracking-wider text-inkMuted">Backbone Spanning Tree (MST)</h3>
          
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-bold text-ink">Visualisasikan Kruskal MST</span>
            <button
              onClick={() => {
                setShowMST(!showMST);
                setSelectedHub(null);
              }}
              className={`px-3 py-1.5 rounded-xl text-[10px] font-bold border transition-all ${
                showMST
                  ? "bg-emerald-600 border-emerald-600 text-white shadow-xs"
                  : "bg-panel-soft border-border text-ink hover:bg-slate-100"
              }`}
            >
              {showMST ? "Aktif" : "Nonaktif"}
            </button>
          </div>

          <p className="text-[9px] text-inkMuted leading-normal">
            Jaringan jalur terpendek penghubung seluruh bandara tanpa siklus (*spanning tree*). Ini meminimalkan total jarak rute backbone penerbangan di Indonesia.
          </p>
        </div>
      </div>

      {/* Main Map Visualizer Area */}
      <div className="flex-1 bg-panel border border-border shadow-panel rounded-3xl overflow-hidden min-h-[400px] lg:h-full relative flex flex-col">
        {/* Layer Toggles Floating Card */}
        <div className="absolute top-4 right-4 z-[999] bg-white/95 backdrop-blur-xs px-3 py-2 rounded-2xl border border-border shadow-md flex items-center gap-4 text-[10px] font-bold text-ink">
          <label className="flex items-center gap-1.5 cursor-pointer">
            <input
              type="checkbox"
              checked={showAllRoutes}
              onChange={e => setShowAllRoutes(e.target.checked)}
              disabled={showMST}
              className="accent-accent"
            />
            <span>Tampilkan Semua Rute ({routes.length})</span>
          </label>
        </div>

        <FlightMap
          airports={airports}
          routes={routes}
          shortestPath={shortestPath}
          selectedHub={selectedHub}
          mstEdges={mstEdges}
          showMST={showMST}
          showAllRoutes={showAllRoutes}
        />
      </div>
    </div>
  );
}
