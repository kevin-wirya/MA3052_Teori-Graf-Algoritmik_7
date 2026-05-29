"use client";

import { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Polyline, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

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

interface FlightMapProps {
  airports: Airport[];
  routes: Route[];
  shortestPath: string[]; // List of IATA codes in order
  selectedHub: string | null; // Selected IATA code to show connections
  mstEdges: { u: string; v: string; weight: number }[]; // MST connections
  showMST: boolean;
  showAllRoutes: boolean;
}

// Custom Leaflet marker icons
const createAirportIcon = (iata: string, isHighlighted: boolean, isHub: boolean) => {
  let bgColor = "#204d6b"; // Default blue
  let size = 10;
  let shadow = "0 1px 3px rgba(0,0,0,0.3)";

  if (isHighlighted) {
    bgColor = "#c8652f"; // Warm orange
    size = 14;
    shadow = "0 0 10px rgba(200, 101, 47, 0.9)";
  } else if (isHub) {
    bgColor = "#10b981"; // Emerald green
    size = 12;
    shadow = "0 0 8px rgba(16, 185, 129, 0.7)";
  }

  return L.divIcon({
    className: "custom-airport-marker",
    html: `
      <div class="flex items-center gap-1 whitespace-nowrap" style="transform: translate(-${size/2}px, -${size/2}px);">
        <div class="rounded-full border-2 border-white shadow-md flex items-center justify-center transition-all duration-300" 
             style="width: ${size}px; height: ${size}px; background-color: ${bgColor}; box-shadow: ${shadow};">
        </div>
        ${isHighlighted || isHub ? `
          <span class="bg-white/95 border border-slate-200 text-slate-800 text-[9px] font-bold px-1 py-0.5 rounded shadow-sm leading-none select-none">
            ${iata}
          </span>
        ` : `<span class="text-[8px] text-inkMuted opacity-60 font-semibold select-none">${iata}</span>`}
      </div>
    `,
    iconSize: [size, size],
    iconAnchor: [size/2, size/2]
  });
};

// Component to handle auto-focus/bounds zooming on paths
function MapController({ shortestPath, airports }: { shortestPath: string[]; airports: Airport[] }) {
  const map = useMap();

  useEffect(() => {
    if (shortestPath.length > 0) {
      const activeAirports = airports.filter(a => shortestPath.includes(a.iata));
      if (activeAirports.length > 0) {
        const bounds = L.latLngBounds(activeAirports.map(a => [a.lat, a.lon]));
        map.fitBounds(bounds, { padding: [50, 50], maxZoom: 7, animate: true });
      }
    }
  }, [shortestPath, airports, map]);

  return null;
}

export default function FlightMap({
  airports,
  routes,
  shortestPath,
  selectedHub,
  mstEdges,
  showMST,
  showAllRoutes
}: FlightMapProps) {
  const [mapStyle] = useState<"google-roadmap" | "osm">("google-roadmap");

  const getTileLayerUrl = () => {
    return mapStyle === "google-roadmap"
      ? "https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}"
      : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
  };

  // Build a lookup map for coordinates
  const airportCoords = airports.reduce((acc, airport) => {
    acc[airport.iata] = [airport.lat, airport.lon] as [number, number];
    return acc;
  }, {} as Record<string, [number, number]>);

  // 1. Gather normal route coordinates
  const allRouteCoords: [number, number][][] = [];
  if (showAllRoutes && !showMST) {
    routes.forEach(route => {
      const srcCoord = airportCoords[route.src];
      const destCoord = airportCoords[route.dest];
      if (srcCoord && destCoord) {
        allRouteCoords.push([srcCoord, destCoord]);
      }
    });
  }

  // 2. Gather MST route coordinates
  const mstRouteCoords: [number, number][][] = [];
  if (showMST) {
    mstEdges.forEach(edge => {
      const srcCoord = airportCoords[edge.u];
      const destCoord = airportCoords[edge.v];
      if (srcCoord && destCoord) {
        mstRouteCoords.push([srcCoord, destCoord]);
      }
    });
  }

  // 3. Gather selected Hub direct route coordinates
  const hubRouteCoords: [number, number][][] = [];
  if (selectedHub && !showMST) {
    routes.forEach(route => {
      if (route.src === selectedHub || route.dest === selectedHub) {
        const srcCoord = airportCoords[route.src];
        const destCoord = airportCoords[route.dest];
        if (srcCoord && destCoord) {
          hubRouteCoords.push([srcCoord, destCoord]);
        }
      }
    });
  }

  // 4. Gather shortest path coordinates
  const pathCoordinates: [number, number][] = [];
  if (shortestPath.length > 1) {
    shortestPath.forEach(iata => {
      const coord = airportCoords[iata];
      if (coord) {
        pathCoordinates.push(coord);
      }
    });
  }

  return (
    <div className="w-full h-full relative">
      <MapContainer
        center={[-2.5, 118]}
        zoom={5}
        className="w-full h-full"
        zoomControl={true}
        minZoom={4}
        maxZoom={10}
      >
        <TileLayer
          attribution='&copy; <a href="https://google.com">Google Maps</a>'
          url={getTileLayerUrl()}
        />

        {/* 1. All background routes (Light Grey) */}
        {showAllRoutes && !showMST && (
          <Polyline
            positions={allRouteCoords}
            color="#cbd5e1"
            weight={1.2}
            opacity={0.35}
          />
        )}

        {/* 2. MST Edges (Emerald Green) */}
        {showMST && (
          <Polyline
            positions={mstRouteCoords}
            color="#10b981"
            weight={2.5}
            opacity={0.9}
            dashArray="5, 5"
          />
        )}

        {/* 3. Selected Hub Connections (Blue-indigo) */}
        {selectedHub && !showMST && (
          <Polyline
            positions={hubRouteCoords}
            color="#3b82f6"
            weight={2.2}
            opacity={0.8}
          />
        )}

        {/* 4. Shortest Route Path (Orange-red Glowing line) */}
        {shortestPath.length > 1 && (
          <Polyline
            positions={pathCoordinates}
            color="#c8652f"
            weight={4}
            opacity={0.95}
          />
        )}

        {/* 5. Airport Markers */}
        {airports.map(airport => {
          const isHighlighted = shortestPath.includes(airport.iata);
          const isHub = selectedHub === airport.iata;
          
          return (
            <Marker
              key={airport.iata}
              position={[airport.lat, airport.lon]}
              icon={createAirportIcon(airport.iata, isHighlighted, isHub)}
            >
              {/* Tooltip detail */}
              <div className="p-2 text-xs font-sans max-w-xs">
                <div className="font-bold text-slate-800">{airport.name}</div>
                <div className="text-slate-500 font-medium">{airport.city} ({airport.iata})</div>
                <div className="text-[10px] text-slate-400 mt-1">
                  Lat: {airport.lat.toFixed(4)}, Lon: {airport.lon.toFixed(4)}
                </div>
              </div>
            </Marker>
          );
        })}

        {/* Focus map view on path coordinates */}
        <MapController shortestPath={shortestPath} airports={airports} />
      </MapContainer>
    </div>
  );
}
