"use client";

import { useEffect, useRef, useState, useCallback, useMemo } from "react";
import dynamic from "next/dynamic";
import {
  worldCities,
  countriesData,
  GeoLocation,
  CountryData
} from "@/lib/graph/geotspData";
const LeafletMap2D = dynamic(() => import("./LeafletMap2D"), {
  ssr: false,
  loading: () => (
    <div className="w-full h-full flex items-center justify-center bg-[#fcfaf7] text-slate-500 text-xs font-bold">
      Memuat Peta Interaktif...
    </div>
  )
});

const ThreeGlobe = dynamic(() => import("./ThreeGlobe"), {
  ssr: false,
  loading: () => (
    <div className="w-full h-full flex items-center justify-center bg-[#fcfaf7] text-slate-500 text-xs font-bold animate-pulse">
      Menginisialisasi WebGL 3D Globe...
    </div>
  )
});

// Haversine formula for 3D Globe Great-Circle distance (in km)
function haversineDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371; // Earth radius in km
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

// Flat 2D Euclidean distance scaled to approximate km (approx 111.32 km per degree)
function euclideanDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const dy = lat2 - lat1;
  const dx = lon2 - lon1;
  return Math.hypot(dx, dy) * 111.32;
}

export default function GeoTSPVisualizer() {
  // UI states
  const [selectedLocations, setSelectedLocations] = useState<GeoLocation[]>([
    // Start with a nice sample (major Indonesian cities)
    { name: "Jakarta Pusat", subdivision: "DKI Jakarta", lat: -6.1862, lon: 106.8326 },
    { name: "Bandung", subdivision: "Jawa Barat", lat: -6.9175, lon: 107.6191 },
    { name: "Semarang", subdivision: "Jawa Tengah", lat: -6.9667, lon: 110.4167 },
    { name: "Surabaya", subdivision: "Jawa Timur", lat: -7.2575, lon: 112.7521 },
    { name: "Yogyakarta", subdivision: "DI Yogyakarta", lat: -7.7956, lon: 110.3695 },
    { name: "Denpasar", subdivision: "Bali", lat: -8.6705, lon: 115.2126 },
    { name: "Makassar", subdivision: "Sulawesi Selatan", lat: -5.1477, lon: 119.4328 },
    { name: "Medan", subdivision: "Sumatera Utara", lat: 3.5952, lon: 98.6722 }
  ]);

  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCountryCode, setSelectedCountryCode] = useState("ID");
  const [viewMode, setViewMode] = useState<"2d" | "3d">("3d");
  const [algorithm, setAlgorithm] = useState<"greedy" | "2opt" | "exact">("2opt");
  const [distanceModel, setDistanceModel] = useState<"euclidean" | "haversine">("haversine");

  // Custom coordinate input states
  const [customName, setCustomName] = useState("");
  const [customLat, setCustomLat] = useState("");
  const [customLon, setCustomLon] = useState("");

  // Solving states
  const [tour, setTour] = useState<number[]>([]);
  const [tourDistance, setTourDistance] = useState<number | null>(null);
  const [solvingTime, setSolvingTime] = useState<number | null>(null);

  // Animation states
  const [currentStep, setCurrentStep] = useState(-1);
  const [isPlaying, setIsPlaying] = useState(false);
  const [speed, setSpeed] = useState(1); // steps per second

  const animIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const [resetKey, setResetKey] = useState(0);

  // Handle click on dynamic 2D Leaflet map
  const handleMapClick = useCallback((lat: number, lon: number) => {
    const roundedLat = Math.round(lat * 10000) / 10000;
    const roundedLon = Math.round(lon * 10000) / 10000;
    
    // Avoid exact duplicate coordinates
    if (selectedLocations.some((l) => Math.abs(l.lat - roundedLat) < 1e-5 && Math.abs(l.lon - roundedLon) < 1e-5)) {
      return;
    }

    const numExistingCustom = selectedLocations.filter(loc => loc.subdivision === "Kustom").length;
    const name = `Titik Kustom ${numExistingCustom + 1}`;
    setSelectedLocations(prev => [
      ...prev,
      { name, subdivision: "Kustom", lat: roundedLat, lon: roundedLon }
    ]);
  }, [selectedLocations]);

  // Find country by code
  const currentCountry = useMemo(() => {
    return countriesData.find((c) => c.code === selectedCountryCode);
  }, [selectedCountryCode]);

  // Search filter results
  const searchResults = useMemo(() => {
    if (!searchQuery.trim()) return [];
    const query = searchQuery.toLowerCase();
    
    // Search in world cities
    const worldMatches = worldCities.filter(
      (c) =>
        c.name.toLowerCase().includes(query) ||
        (c.subdivision && c.subdivision.toLowerCase().includes(query))
    );

    // Search in all country province capitals
    const countryMatches: GeoLocation[] = [];
    countriesData.forEach((country) => {
      country.locations.forEach((loc) => {
        if (
          loc.name.toLowerCase().includes(query) ||
          (loc.subdivision && loc.subdivision.toLowerCase().includes(query)) ||
          country.name.toLowerCase().includes(query)
        ) {
          // Avoid duplicate names if they are already in world cities
          if (!worldMatches.some((w) => w.name === loc.name && Math.abs(w.lat - loc.lat) < 1e-4)) {
            countryMatches.push({
              ...loc,
              name: `${loc.name} (${country.name})`
            });
          }
        }
      });
    });

    return [...worldMatches, ...countryMatches].slice(0, 10);
  }, [searchQuery]);

  // Handle location actions
  const addLocation = (loc: GeoLocation) => {
    // Avoid exact duplicate coordinates
    if (selectedLocations.some((l) => Math.abs(l.lat - loc.lat) < 1e-5 && Math.abs(l.lon - loc.lon) < 1e-5)) {
      return;
    }
    setSelectedLocations((prev) => [...prev, loc]);
    resetTour();
  };

  const removeLocation = (index: number) => {
    setSelectedLocations((prev) => prev.filter((_, i) => i !== index));
    resetTour();
  };

  const clearAllLocations = () => {
    setSelectedLocations([]);
    resetTour();
  };

  const addAllPresetProvinces = () => {
    if (!currentCountry) return;
    // Add all locations from selected country
    const toAdd = currentCountry.locations.filter(
      (loc) => !selectedLocations.some((l) => Math.abs(l.lat - loc.lat) < 1e-5 && Math.abs(l.lon - loc.lon) < 1e-5)
    );
    setSelectedLocations((prev) => [...prev, ...toAdd]);
    resetTour();
  };

  const addCustomLocation = (e: React.FormEvent) => {
    e.preventDefault();
    const latNum = parseFloat(customLat);
    const lonNum = parseFloat(customLon);
    if (!customName.trim()) {
      alert("Nama lokasi wajib diisi");
      return;
    }
    if (isNaN(latNum) || latNum < -90 || latNum > 90) {
      alert("Latitude harus berupa angka antara -90 dan 90");
      return;
    }
    if (isNaN(lonNum) || lonNum < -180 || lonNum > 180) {
      alert("Longitude harus berupa angka antara -180 dan 180");
      return;
    }
    addLocation({
      name: customName,
      subdivision: "Kustom",
      lat: latNum,
      lon: lonNum
    });
    setCustomName("");
    setCustomLat("");
    setCustomLon("");
  };

  const resetTour = () => {
    setTour([]);
    setTourDistance(null);
    setSolvingTime(null);
    setCurrentStep(-1);
    setIsPlaying(false);
  };

  // Solve TSP Algorithms
  const solveTSP = () => {
    if (selectedLocations.length < 2) {
      alert("Pilih minimal 2 lokasi untuk mencari TSP!");
      return;
    }

    const n = selectedLocations.length;
    const distFn = (i: number, j: number) => {
      const a = selectedLocations[i];
      const b = selectedLocations[j];
      return distanceModel === "haversine"
        ? haversineDistance(a.lat, a.lon, b.lat, b.lon)
        : euclideanDistance(a.lat, a.lon, b.lat, b.lon);
    };

    const startTime = performance.now();
    let bestTour: number[] = [];
    let bestCost = Infinity;

    if (algorithm === "exact") {
      if (n > 16) {
        const confirmSolve = window.confirm(
          `Held-Karp DP memiliki kompleksitas O(n^2 * 2^n). Menyelesaikan ${n} lokasi dapat membuat tab browser Anda hang.\n\nApakah Anda ingin beralih ke 2-Opt Heuristic yang lebih cepat?`
        );
        if (confirmSolve) {
          setAlgorithm("2opt");
          // run 2-opt instead
          solve2OptTour(n, distFn, startTime);
          return;
        } else {
          // Run Held-Karp anyway (cap at 20 to prevent crashes)
          if (n > 20) {
            alert("Maksimum 20 lokasi untuk Exact DP. Beralih ke 2-Opt.");
            setAlgorithm("2opt");
            solve2OptTour(n, distFn, startTime);
            return;
          }
        }
      }

      // Solve Exact DP Held-Karp
      const memo = new Map<string, { cost: number; parent: number }>();
      
      const getDP = (mask: number, pos: number): { cost: number; parent: number } => {
        if (mask === (1 << n) - 1) {
          return { cost: distFn(pos, 0), parent: 0 };
        }
        
        const key = `${mask}_${pos}`;
        if (memo.has(key)) return memo.get(key)!;

        let minCost = Infinity;
        let bestParent = -1;

        for (let next = 0; next < n; next++) {
          if ((mask & (1 << next)) === 0) {
            const cost = distFn(pos, next) + getDP(mask | (1 << next), next).cost;
            if (cost < minCost) {
              minCost = cost;
              bestParent = next;
            }
          }
        }

        const res = { cost: minCost, parent: bestParent };
        memo.set(key, res);
        return res;
      };

      const result = getDP(1, 0);
      bestCost = result.cost;

      // Reconstruct path
      let currentMask = 1;
      let currentPos = 0;
      bestTour = [0];
      
      while (bestTour.length < n) {
        const nextNode = getDP(currentMask, currentPos).parent;
        bestTour.push(nextNode);
        currentMask |= (1 << nextNode);
        currentPos = nextNode;
      }
      bestTour.push(0); // return to start
    } 
    else if (algorithm === "greedy") {
      // Solve Greedy Nearest Neighbor (try all starts, pick best)
      for (let start = 0; start < n; start++) {
        const visited = new Set<number>([start]);
        const currentTour = [start];
        let currentCost = 0;
        let curr = start;

        while (visited.size < n) {
          let nextNode = -1;
          let minD = Infinity;
          for (let next = 0; next < n; next++) {
            if (!visited.has(next)) {
              const d = distFn(curr, next);
              if (d < minD) {
                minD = d;
                nextNode = next;
              }
            }
          }
          currentTour.push(nextNode);
          visited.add(nextNode);
          currentCost += minD;
          curr = nextNode;
        }
        currentCost += distFn(curr, start);
        currentTour.push(start);

        if (currentCost < bestCost) {
          bestCost = currentCost;
          bestTour = currentTour;
        }
      }
    } 
    else {
      // Solve 2-Opt Heuristic
      solve2OptTour(n, distFn, startTime);
      return;
    }

    const endTime = performance.now();
    setTour(bestTour);
    setTourDistance(bestCost);
    setSolvingTime(endTime - startTime);
    setCurrentStep(0);
    setIsPlaying(true);
  };

  const solve2OptTour = (n: number, distFn: (i: number, j: number) => number, startTime: number) => {
    // Start with a greedy tour from node 0
    const visited = new Set<number>([0]);
    let tour = [0];
    let curr = 0;

    while (visited.size < n) {
      let nextNode = -1;
      let minD = Infinity;
      for (let next = 0; next < n; next++) {
        if (!visited.has(next)) {
          const d = distFn(curr, next);
          if (d < minD) {
            minD = d;
            nextNode = next;
          }
        }
      }
      tour.push(nextNode);
      visited.add(nextNode);
      curr = nextNode;
    }
    tour.push(0);

    // Apply 2-opt swaps until no further improvement
    let improved = true;
    while (improved) {
      improved = false;
      for (let i = 1; i < tour.length - 2; i++) {
        for (let j = i + 1; j < tour.length - 1; j++) {
          const dCurrent = distFn(tour[i - 1], tour[i]) + distFn(tour[j], tour[j + 1]);
          const dNew = distFn(tour[i - 1], tour[j]) + distFn(tour[i], tour[j + 1]);

          if (dNew < dCurrent - 1e-6) {
            // Reverse subset tour[i...j]
            const newTour = [...tour];
            let l = i, r = j;
            while (l < r) {
              const temp = newTour[l];
              newTour[l] = newTour[r];
              newTour[r] = temp;
              l++;
              r--;
            }
            tour = newTour;
            improved = true;
          }
        }
      }
    }

    // Calculate final cost
    let finalCost = 0;
    for (let i = 0; i < tour.length - 1; i++) {
      finalCost += distFn(tour[i], tour[i + 1]);
    }

    const endTime = performance.now();
    setTour(tour);
    setTourDistance(finalCost);
    setSolvingTime(endTime - startTime);
    setCurrentStep(0);
    setIsPlaying(true);
  };

  // Animation controller
  useEffect(() => {
    if (isPlaying && tour.length > 0) {
      const intervalMs = 1000 / speed;
      animIntervalRef.current = setInterval(() => {
        setCurrentStep((prev) => {
          if (prev >= tour.length - 1) {
            setIsPlaying(false);
            return prev;
          }
          return prev + 1;
        });
      }, intervalMs);
    } else {
      if (animIntervalRef.current) {
        clearInterval(animIntervalRef.current);
      }
    }

    return () => {
      if (animIntervalRef.current) {
        clearInterval(animIntervalRef.current);
      }
    };
  }, [isPlaying, tour, speed]);

  const resetView = () => {
    setResetKey((prev) => prev + 1);
  };

  return (
    <div className="flex flex-col lg:flex-row flex-1 gap-4 min-h-0 w-full overflow-y-auto lg:overflow-hidden pb-4 lg:pb-0">
      {/* LEFT PANEL: Location Manager */}
      <aside className="w-full lg:w-80 shrink-0 h-auto lg:h-full flex flex-col gap-3 rounded-2xl border border-border bg-panel p-3.5 shadow-panel">
        
        {/* Preset Selector */}
        <section className="space-y-1.5 shrink-0">
          <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">Negara Preset</h3>
          <div className="grid grid-cols-2 gap-1.5">
            <select
              className="rounded-lg border border-border bg-white px-2 py-1 text-xs focus:border-accent focus:outline-none"
              value={selectedCountryCode}
              onChange={(e) => setSelectedCountryCode(e.target.value)}
            >
              {countriesData.map((c) => (
                <option key={c.code} value={c.code}>
                  {c.name}
                </option>
              ))}
            </select>
            <button
              onClick={addAllPresetProvinces}
              className="rounded-lg bg-accent text-white px-2 py-1 text-xs font-bold hover:bg-accent/90 transition text-center truncate"
              title="Masukkan semua wilayah/ibu kota dari negara ini"
            >
              + Semua Wilayah
            </button>
          </div>
        </section>

        {/* Global Search Locations */}
        <section className="space-y-1 shrink-0 relative">
          <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">Cari & Tambah Lokasi</h3>
          <input
            type="text"
            className="w-full rounded-lg border border-border bg-white px-2 py-1 text-xs focus:border-accent focus:outline-none"
            placeholder="Ketik kota (misal: Paris, Bandung, Tokyo...)"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          {searchResults.length > 0 && (
            <div className="absolute left-0 right-0 top-full mt-1 bg-white border border-border rounded-xl shadow-lg z-50 max-h-48 overflow-y-auto">
              {searchResults.map((res, i) => (
                <button
                  key={i}
                  className="w-full text-left px-3 py-2 text-xs hover:bg-panel-soft transition flex justify-between border-b border-border/20 last:border-b-0"
                  onClick={() => {
                    addLocation(res);
                    setSearchQuery("");
                  }}
                >
                  <span className="font-bold text-ink truncate">{res.name}</span>
                  <span className="text-[9px] text-inkMuted font-mono shrink-0">
                    {res.lat.toFixed(2)}, {res.lon.toFixed(2)}
                  </span>
                </button>
              ))}
            </div>
          )}
        </section>

        {/* Manual Coordinate Input */}
        <section className="space-y-1 shrink-0 border-t border-border/40 pt-2">
          <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">Input Koordinat Kustom</h3>
          <form onSubmit={addCustomLocation} className="space-y-1.5">
            <input
              type="text"
              className="w-full rounded-lg border border-border bg-white px-2 py-1 text-xs focus:border-accent focus:outline-none"
              placeholder="Nama Lokasi"
              value={customName}
              onChange={(e) => setCustomName(e.target.value)}
            />
            <div className="grid grid-cols-2 gap-1.5">
              <input
                type="text"
                className="rounded-lg border border-border bg-white px-2 py-1 text-xs focus:border-accent focus:outline-none"
                placeholder="Lat (-90 s.d 90)"
                value={customLat}
                onChange={(e) => setCustomLat(e.target.value)}
              />
              <input
                type="text"
                className="rounded-lg border border-border bg-white px-2 py-1 text-xs focus:border-accent focus:outline-none"
                placeholder="Lon (-180 s.d 180)"
                value={customLon}
                onChange={(e) => setCustomLon(e.target.value)}
              />
            </div>
            <button
              type="submit"
              className="w-full rounded-lg bg-panel-soft border border-border/80 text-ink hover:bg-border/20 py-1 text-xs font-bold transition"
            >
              + Tambah Koordinat
            </button>
          </form>
        </section>

        {/* Selected Locations List */}
        <section className="flex-1 min-h-0 flex flex-col border-t border-border/40 pt-2.5">
          <div className="flex justify-between items-center mb-1 shrink-0">
            <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">
              Daftar Lokasi ({selectedLocations.length})
            </h3>
            {selectedLocations.length > 0 && (
              <button
                onClick={clearAllLocations}
                className="text-[9px] font-bold text-accentWarm hover:underline"
              >
                Hapus Semua
              </button>
            )}
          </div>
          <div className="flex-1 overflow-y-auto pr-1 space-y-1 max-h-full">
            {selectedLocations.length === 0 ? (
              <p className="text-[10px] text-inkMuted italic text-center py-4">Belum ada lokasi dipilih</p>
            ) : (
              selectedLocations.map((loc, idx) => (
                <div
                  key={idx}
                  className="flex items-center justify-between gap-2 p-1.5 rounded-lg border border-border/40 bg-panel-soft text-xs"
                >
                  <div className="min-w-0 flex-1">
                    <p className="font-bold text-ink truncate leading-tight">{loc.name}</p>
                    <p className="text-[9px] text-inkMuted truncate font-mono">
                      Lat: {loc.lat.toFixed(4)} | Lon: {loc.lon.toFixed(4)}
                    </p>
                  </div>
                  <button
                    onClick={() => removeLocation(idx)}
                    className="text-inkMuted hover:text-accentWarm p-1 transition text-sm font-semibold shrink-0"
                    title="Hapus lokasi"
                  >
                    ✕
                  </button>
                </div>
              ))
            )}
          </div>
        </section>
      </aside>

      {/* CENTER PANEL: Map/Globe Visualization */}
      <section className="flex-1 min-h-[450px] lg:min-h-0 flex flex-col border border-border bg-panel rounded-2xl shadow-panel overflow-hidden">
        
        {/* Map Toolbar */}
        <div className="flex items-center justify-between gap-3 border-b border-border bg-panel-soft px-4 py-2 text-sm shrink-0">
          <div className="flex items-center gap-1.5">
            <span className="text-xs font-bold text-inkMuted mr-1">Proyeksi Peta:</span>
            <button
              className={`rounded-xl px-3 py-1.5 text-xs font-bold transition flex items-center gap-1.5 ${
                viewMode === "2d"
                  ? "bg-accent text-white shadow-sm"
                  : "bg-white text-ink border border-border/85 hover:bg-panel-soft"
              }`}
              onClick={() => setViewMode("2d")}
            >
              🗺️ 2D Flat Map
            </button>
            <button
              className={`rounded-xl px-3 py-1.5 text-xs font-bold transition flex items-center gap-1.5 ${
                viewMode === "3d"
                  ? "bg-accent text-white shadow-sm"
                  : "bg-white text-ink border border-border/85 hover:bg-panel-soft"
              }`}
              onClick={() => setViewMode("3d")}
            >
              🌐 3D Round Earth (Globe)
            </button>
          </div>
          
          <div className="flex items-center gap-2">
            {viewMode === "3d" && (
              <span className="text-[10px] text-inkMuted italic mr-1">
                💡 Seret mouse untuk memutar bola bumi
              </span>
            )}
            <button
              className="rounded-xl border border-border bg-white px-3 py-1.5 text-xs font-bold text-ink hover:bg-panel-soft transition"
              onClick={resetView}
            >
              🎯 Reset View
            </button>
          </div>
        </div>

        {/* Map Canvas / Leaflet Map */}
        <div className="flex-1 min-h-0 relative bg-white overflow-hidden">
          {viewMode === "2d" ? (
            <LeafletMap2D
              selectedLocations={selectedLocations}
              tour={tour}
              currentStep={currentStep}
              onMapClick={handleMapClick}
            />
          ) : (
            <ThreeGlobe
              key={resetKey}
              selectedLocations={selectedLocations}
              tour={tour}
              currentStep={currentStep}
            />
          )}
        </div>

        {/* Animation Playback Control Panel */}
        {tour.length > 0 && (
          <div className="border-t border-border bg-panel-soft p-3 flex flex-col gap-2 shrink-0">
            <div className="flex items-center justify-between text-xs">
              <div className="flex items-center gap-1 text-ink">
                <span className="font-bold text-accent">Langkah Tur:</span>
                <span className="font-mono bg-white px-1.5 py-0.5 rounded border border-border/60">
                  {currentStep >= 0 ? `${selectedLocations[tour[currentStep]].name}` : "Belum mulai"}
                </span>
                {currentStep > 0 && (
                  <>
                    <span className="text-inkMuted mx-0.5">→</span>
                    <span className="font-mono bg-white px-1.5 py-0.5 rounded border border-border/60">
                      {currentStep < tour.length - 1
                        ? selectedLocations[tour[currentStep + 1]].name
                        : selectedLocations[tour[0]].name}
                    </span>
                  </>
                )}
              </div>
              <div className="font-mono text-accent font-bold">
                Progress: {currentStep + 1} / {tour.length}
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="flex gap-1">
                <button
                  disabled={currentStep <= 0}
                  className="rounded bg-white border border-border hover:bg-panel-soft py-1 px-2.5 text-xs transition disabled:opacity-50"
                  onClick={() => setCurrentStep((prev) => Math.max(0, prev - 1))}
                  title="Langkah Sebelumnya"
                >
                  ⏮
                </button>
                <button
                  className="rounded bg-accent text-white hover:bg-accent/90 py-1 px-4 text-xs font-bold transition shadow-sm"
                  onClick={() => {
                    if (currentStep >= tour.length - 1) {
                      setCurrentStep(0);
                    }
                    setIsPlaying(!isPlaying);
                  }}
                >
                  {isPlaying ? "⏸ Pause" : "▶ Play"}
                </button>
                <button
                  disabled={currentStep >= tour.length - 1}
                  className="rounded bg-white border border-border hover:bg-panel-soft py-1 px-2.5 text-xs transition disabled:opacity-50"
                  onClick={() => setCurrentStep((prev) => Math.min(tour.length - 1, prev + 1))}
                  title="Langkah Selanjutnya"
                >
                  ⏭
                </button>
                <button
                  className="rounded bg-accentWarm/10 border border-accentWarm/20 py-1 px-3 text-xs text-accentWarm font-bold transition hover:bg-accentWarm/20"
                  onClick={() => {
                    setIsPlaying(false);
                    setCurrentStep(-1);
                  }}
                  title="Stop"
                >
                  ⏹ Reset
                </button>
              </div>

              {/* Progress slider */}
              <input
                type="range"
                min={-1}
                max={tour.length - 1}
                value={currentStep}
                className="flex-1 slider-custom focus:outline-none"
                onChange={(e) => {
                  setIsPlaying(false);
                  setCurrentStep(parseInt(e.target.value));
                }}
              />

              {/* Speed Controller */}
              <div className="flex items-center gap-1.5 shrink-0 text-xs text-inkMuted">
                <span>Speed:</span>
                <select
                  className="rounded border border-border bg-white py-0.5 px-1 focus:outline-none font-bold"
                  value={speed}
                  onChange={(e) => setSpeed(parseFloat(e.target.value))}
                >
                  <option value={0.5}>0.5x</option>
                  <option value={1}>1.0x</option>
                  <option value={2}>2.0x</option>
                  <option value={4}>4.0x</option>
                </select>
              </div>
            </div>
          </div>
        )}
      </section>

      {/* RIGHT PANEL: Settings & Tour Statistics */}
      <aside className="w-full lg:w-72 shrink-0 h-auto lg:h-full flex flex-col gap-3 rounded-2xl border border-border bg-panel p-3.5 shadow-panel overflow-y-auto">
        
        {/* Configuration settings */}
        <section className="space-y-3">
          <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">Konfigurasi GeoTSP</h3>
          
          {/* Algorithm selection */}
          <div className="space-y-1">
            <label className="text-[10px] font-bold text-inkMuted uppercase">Algoritma TSP</label>
            <select
              className="w-full rounded-lg border border-border bg-white px-2.5 py-1.5 text-xs focus:border-accent focus:outline-none text-ink font-semibold"
              value={algorithm}
              onChange={(e) => setAlgorithm(e.target.value as any)}
            >
              <option value="greedy">Greedy Nearest Neighbor</option>
              <option value="2opt">2-Opt Optimization</option>
              <option value="exact">Exact (Held-Karp DP)</option>
            </select>
            <p className="text-[9px] text-inkMuted leading-tight italic">
              {algorithm === "greedy" && "Cepat, mengambil pilihan lokal terdekat di setiap langkah."}
              {algorithm === "2opt" && "Sangat direkomendasikan. Memperbaiki tur greedy dengan menukar sisi untuk membuang persilangan."}
              {algorithm === "exact" && "Optimal mutlak global. Terbatas maksimal 16-20 lokasi karena kompleksitas eksponensial."}
            </p>
          </div>

          {/* Distance model selection */}
          <div className="space-y-1">
            <label className="text-[10px] font-bold text-inkMuted uppercase">Model Bumi / Jarak</label>
            <select
              className="w-full rounded-lg border border-border bg-white px-2.5 py-1.5 text-xs focus:border-accent focus:outline-none text-ink font-semibold"
              value={distanceModel}
              onChange={(e) => setDistanceModel(e.target.value as any)}
            >
              <option value="haversine">3D Bulat (Great-Circle / Haversine)</option>
              <option value="euclidean">2D Peta Datar (Euclidean)</option>
            </select>
            <p className="text-[9px] text-inkMuted leading-tight italic">
              {distanceModel === "haversine" && "Jarak kelengkungan bumi 3D nyata (haversine) dalam kilometer."}
              {distanceModel === "euclidean" && "Jarak garis lurus Euclidean 2D pada bidang datar derajat."}
            </p>
          </div>

          {/* Calculate TSP button */}
          <button
            onClick={solveTSP}
            disabled={selectedLocations.length < 2}
            className="w-full rounded-lg bg-accent text-white hover:bg-accent/90 py-2 text-xs font-bold shadow-sm transition disabled:opacity-50"
          >
            🎯 Hitung Tur TSP
          </button>
        </section>

        {/* Solver stats metrics */}
        {tourDistance !== null && (
          <section className="space-y-2 border-t border-border/40 pt-3 flex-1 flex flex-col min-h-0">
            <h3 className="text-[10px] font-bold uppercase tracking-wider text-inkMuted">Hasil Kalkulasi</h3>
            
            <div className="grid grid-cols-2 gap-2 text-center text-xs shrink-0">
              <div className="bg-panel-soft border border-border/40 p-2 rounded-xl">
                <div className="text-[9px] text-inkMuted uppercase font-bold">Total Jarak</div>
                <div className="font-bold text-accentWarm text-sm">
                  {tourDistance.toLocaleString("id-ID", { maximumFractionDigits: 1 })} km
                </div>
              </div>
              <div className="bg-panel-soft border border-border/40 p-2 rounded-xl">
                <div className="text-[9px] text-inkMuted uppercase font-bold">Waktu Proses</div>
                <div className="font-bold text-accent text-sm">
                  {solvingTime !== null ? `${solvingTime.toFixed(1)} ms` : "-"}
                </div>
              </div>
            </div>

            {/* Tour Sequence Path list */}
            <div className="flex-1 min-h-0 flex flex-col mt-1">
              <h4 className="text-[9px] font-bold uppercase tracking-wider text-inkMuted mb-1 shrink-0">
                Urutan Perjalanan Tur
              </h4>
              <div className="flex-1 overflow-y-auto pr-1 space-y-1 font-mono text-[10px] bg-panel-soft p-2 rounded-lg border border-border/40 max-h-full">
                {tour.map((nodeIdx, tourStep) => {
                  const loc = selectedLocations[nodeIdx];
                  const isLast = tourStep === tour.length - 1;
                  
                  // Calculate step distance
                  let stepDistText = "";
                  if (tourStep > 0) {
                    const prevNodeIdx = tour[tourStep - 1];
                    const prevLoc = selectedLocations[prevNodeIdx];
                    const d = distanceModel === "haversine"
                      ? haversineDistance(prevLoc.lat, prevLoc.lon, loc.lat, loc.lon)
                      : euclideanDistance(prevLoc.lat, prevLoc.lon, loc.lat, loc.lon);
                    stepDistText = ` (+${d.toFixed(1)} km)`;
                  }

                  const isCurrent = tourStep === currentStep;

                  return (
                    <div
                      key={tourStep}
                      className={`flex items-center gap-1 py-0.5 border-b border-border/10 last:border-b-0 ${
                        isCurrent ? "text-accentWarm font-bold bg-accentWarm/10 px-1 rounded" : "text-ink"
                      }`}
                    >
                      <span className="text-inkMuted font-bold shrink-0">{tourStep + 1}.</span>
                      <span className="truncate flex-1">{loc.name}</span>
                      <span className="text-[9px] text-inkMuted shrink-0">{stepDistText}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          </section>
        )}
      </aside>
    </div>
  );
}
