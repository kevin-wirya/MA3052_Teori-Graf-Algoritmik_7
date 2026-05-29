"use client";

import { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Polyline, useMap, useMapEvents } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { GeoLocation } from "@/lib/graph/geotspData";

// Fix leaflet icon issue in Next.js/Webpack
const createCustomMarkerIcon = (isActive: boolean, name: string) => {
  return L.divIcon({
    className: "custom-leaflet-marker",
    html: `
      <div class="flex items-center gap-1.5 whitespace-nowrap" style="transform: translate(-7px, -7px);">
        <div class="w-3.5 h-3.5 rounded-full border-2 border-white shadow-md flex items-center justify-center transition-all" style="background-color: ${
          isActive ? "#c8652f" : "#204d6b"
        }; transform: ${isActive ? "scale(1.2)" : "scale(1)"}; box-shadow: ${
      isActive ? "0 0 8px rgba(200, 101, 47, 0.8)" : "0 2px 4px rgba(0,0,0,0.2)"
    };"></div>
        <span class="bg-white/95 border border-slate-200 text-slate-800 text-[10px] font-bold px-1.5 py-0.5 rounded shadow-sm leading-none select-none">${name}</span>
      </div>
    `,
    iconSize: [14, 14],
    iconAnchor: [7, 7]
  });
};

// Fit map viewport to encompass all loaded coordinate points
function MapController({ locations }: { locations: GeoLocation[] }) {
  const map = useMap();
  useEffect(() => {
    if (locations.length > 0) {
      const bounds = L.latLngBounds(locations.map((loc) => [loc.lat, loc.lon]));
      map.fitBounds(bounds, { padding: [40, 40], maxZoom: 8 });
    }
  }, [locations, map]);
  return null;
}

// Map Click Listener to add custom coordinate points
function MapClickListener({ onMapClick }: { onMapClick: (lat: number, lon: number) => void }) {
  useMapEvents({
    click(e) {
      onMapClick(e.latlng.lat, e.latlng.lng);
    }
  });
  return null;
}

interface LeafletMap2DProps {
  selectedLocations: GeoLocation[];
  tour: number[];
  currentStep: number;
  onMapClick: (lat: number, lon: number) => void;
}

export default function LeafletMap2D({
  selectedLocations,
  tour,
  currentStep,
  onMapClick
}: LeafletMap2DProps) {
  const [mapStyle, setMapStyle] = useState<"google-roadmap" | "google-hybrid" | "osm">("google-roadmap");

  // Determine path coordinates based on the solved tour & animation step
  const pathCoordinates = [];
  if (tour.length > 0 && currentStep >= 0) {
    for (let i = 0; i <= currentStep; i++) {
      const loc = selectedLocations[tour[i]];
      pathCoordinates.push([loc.lat, loc.lon] as [number, number]);
    }
  }

  // Dotted/translucent remaining path
  const remainingCoordinates = [];
  if (tour.length > 0 && currentStep < tour.length - 1 && currentStep >= 0) {
    // Start from the active node
    remainingCoordinates.push([
      selectedLocations[tour[currentStep]].lat,
      selectedLocations[tour[currentStep]].lon
    ] as [number, number]);
    for (let i = currentStep + 1; i < tour.length; i++) {
      const loc = selectedLocations[tour[i]];
      remainingCoordinates.push([loc.lat, loc.lon] as [number, number]);
    }
  }

  // Google Maps tile layer sources
  const getTileLayerUrl = () => {
    switch (mapStyle) {
      case "google-roadmap":
        return "https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}";
      case "google-hybrid":
        return "https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}";
      case "osm":
      default:
        return "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
    }
  };

  return (
    <div className="w-full h-full relative flex flex-col">
      {/* Map style selector bar */}
      <div className="absolute top-3 right-3 bg-white/95 border border-slate-200 px-2 py-1.5 rounded-xl shadow-md z-[1000] flex gap-1 text-[11px]">
        <button
          className={`px-2.5 py-1 rounded-lg font-bold transition ${
            mapStyle === "google-roadmap"
              ? "bg-[#204d6b] text-white"
              : "hover:bg-slate-100 text-slate-700"
          }`}
          onClick={() => setMapStyle("google-roadmap")}
        >
          Google Map
        </button>
        <button
          className={`px-2.5 py-1 rounded-lg font-bold transition ${
            mapStyle === "google-hybrid"
              ? "bg-[#204d6b] text-white"
              : "hover:bg-slate-100 text-slate-700"
          }`}
          onClick={() => setMapStyle("google-hybrid")}
        >
          Satelit
        </button>
        <button
          className={`px-2.5 py-1 rounded-lg font-bold transition ${
            mapStyle === "osm"
              ? "bg-[#204d6b] text-white"
              : "hover:bg-slate-100 text-slate-700"
          }`}
          onClick={() => setMapStyle("osm")}
        >
          OSM
        </button>
      </div>

      <div className="absolute top-3 left-12 bg-white/90 border border-slate-200 px-2.5 py-1 rounded-lg shadow-sm z-[1000] text-[10px] text-slate-600 pointer-events-none">
        💡 Klik di mana saja pada peta untuk menambahkan koordinat kustom
      </div>

      <MapContainer
        center={[-0.7893, 113.9213]} // Centered on Indonesia
        zoom={5}
        style={{ width: "100%", height: "100%" }}
        className="z-10"
      >
        <TileLayer
          url={getTileLayerUrl()}
          attribution={
            mapStyle.startsWith("google")
              ? "&copy; Google Maps"
              : '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          }
          maxZoom={20}
        />

        {/* Path segments */}
        {pathCoordinates.length > 0 && (
          <Polyline
            positions={pathCoordinates}
            pathOptions={{ color: "#c8652f", weight: 3, opacity: 0.9 }}
          />
        )}

        {/* Dotted remaining segments */}
        {remainingCoordinates.length > 0 && (
          <Polyline
            positions={remainingCoordinates}
            pathOptions={{ color: "#c8652f", weight: 2, opacity: 0.5, dashArray: "5, 10" }}
          />
        )}

        {/* Location markers */}
        {selectedLocations.map((loc, idx) => {
          const isActive = tour.length > 0 && tour[currentStep] === idx;
          return (
            <Marker
              key={idx}
              position={[loc.lat, loc.lon]}
              icon={createCustomMarkerIcon(isActive, loc.name)}
            />
          );
        })}

        {/* Controllers */}
        <MapController locations={selectedLocations} />
        <MapClickListener onMapClick={onMapClick} />
      </MapContainer>
    </div>
  );
}
