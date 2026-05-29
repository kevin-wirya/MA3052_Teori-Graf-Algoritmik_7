"use client";

import { useEffect, useRef, useState } from "react";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import { GeoLocation } from "@/lib/graph/geotspData";

interface ThreeGlobeProps {
  selectedLocations: GeoLocation[];
  tour: number[];
  currentStep: number;
}

// Spherical linear interpolation between two normalized vectors on a sphere
function slerpVectors(v1: THREE.Vector3, v2: THREE.Vector3, t: number, radius: number): THREE.Vector3 {
  const n1 = v1.clone().normalize();
  const n2 = v2.clone().normalize();
  
  const dot = Math.min(1.0, Math.max(-1.0, n1.dot(n2)));
  const theta = Math.acos(dot);
  
  if (theta < 0.001) {
    return n1.clone().lerp(n2, t).normalize().multiplyScalar(radius);
  }
  
  const sinTheta = Math.sin(theta);
  const factor1 = Math.sin((1 - t) * theta) / sinTheta;
  const factor2 = Math.sin(t * theta) / sinTheta;
  
  const res = new THREE.Vector3()
    .addScaledVector(n1, factor1)
    .addScaledVector(n2, factor2);
    
  return res.multiplyScalar(radius);
}

export default function ThreeGlobe({ selectedLocations, tour, currentStep }: ThreeGlobeProps) {
  const mountRef = useRef<HTMLDivElement | null>(null);
  const labelsContainerRef = useRef<HTMLDivElement | null>(null);

  const selectedLocationsRef = useRef(selectedLocations);
  useEffect(() => {
    selectedLocationsRef.current = selectedLocations;
  }, [selectedLocations]);
  
  // Track locations with calculated 3D coordinates to project labels
  const [locationsWithCoords, setLocationsWithCoords] = useState<(GeoLocation & { pos: THREE.Vector3 })[]>([]);
  
  const R = 5; // Globe radius in 3D scene units

  // Helper to project Lat/Lon to 3D Sphere Cartesian Space
  const get3DCoords = (lat: number, lon: number, radius: number): THREE.Vector3 => {
    const phi = (90 - lat) * (Math.PI / 180);
    const theta = (lon + 180) * (Math.PI / 180);
    return new THREE.Vector3(
      -radius * Math.sin(phi) * Math.cos(theta),
      radius * Math.cos(phi),
      radius * Math.sin(phi) * Math.sin(theta)
    );
  };

  // Update locations 3D coordinates when selectedLocations changes
  useEffect(() => {
    const coords = selectedLocations.map((loc) => ({
      ...loc,
      pos: get3DCoords(loc.lat, loc.lon, R),
    }));
    setLocationsWithCoords(coords);
  }, [selectedLocations]);

  // Keep references to Three.js objects for fast updates
  const sceneRef = useRef<THREE.Scene | null>(null);
  const cameraRef = useRef<THREE.PerspectiveCamera | null>(null);
  const controlsRef = useRef<OrbitControls | null>(null);
  const rendererRef = useRef<THREE.WebGLRenderer | null>(null);
  const markersGroupRef = useRef<THREE.Group | null>(null);
  const pathsGroupRef = useRef<THREE.Group | null>(null);
  const targetCameraPosRef = useRef<THREE.Vector3 | null>(null);
  const isUserInteractingRef = useRef<boolean>(false);

  // 1. Initial Scene Setup
  useEffect(() => {
    const mount = mountRef.current;
    if (!mount) return;

    const width = mount.clientWidth;
    const height = mount.clientHeight;

    // Scene & Camera Setup
    const scene = new THREE.Scene();
    sceneRef.current = scene;
    scene.background = new THREE.Color(0xfcfaf7); // Warm paper background matching UI

    const camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 100);
    cameraRef.current = camera;
    camera.position.set(0, 5, 13);

    // WebGL Renderer Setup (with high HD quality and anti-aliasing)
    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    rendererRef.current = renderer;
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.setSize(width, height);
    mount.appendChild(renderer.domElement);

    // Orbit Controls Setup for Butter-Smooth Rotations
    const controls = new OrbitControls(camera, renderer.domElement);
    controlsRef.current = controls;
    controls.enableDamping = true;
    controls.dampingFactor = 0.08;
    controls.rotateSpeed = 0.8;
    controls.minDistance = 6.5;
    controls.maxDistance = 25;

    controls.addEventListener("start", () => {
      isUserInteractingRef.current = true;
      targetCameraPosRef.current = null; // stop automatic transition
    });
    controls.addEventListener("end", () => {
      isUserInteractingRef.current = false;
    });

    // Globe Sphere creation with NASA Blue Marble Texture
    const textureLoader = new THREE.TextureLoader();
    const earthTexture = textureLoader.load("/earth-blue-marble.jpg", (tex) => {
      // Set anisotropic filtering for high-definition sharpness on zoom
      tex.anisotropy = renderer.capabilities.getMaxAnisotropy();
      // Re-render when texture loads to prevent white globe flicker
      renderer.render(scene, camera);
    });
    
    // Using MeshPhongMaterial for glossy specular reflection on oceans and land masses
    const earthGeo = new THREE.SphereGeometry(R, 64, 64);
    const earthMat = new THREE.MeshPhongMaterial({
      map: earthTexture,
      specular: new THREE.Color(0x222222),
      shininess: 12,
    });
    const earthMesh = new THREE.Mesh(earthGeo, earthMat);
    scene.add(earthMesh);

    // Ambient Atmospheric Glow Layer
    const glowGeo = new THREE.SphereGeometry(R * 1.015, 32, 32);
    const glowMat = new THREE.MeshBasicMaterial({
      color: 0x8ab4f8,
      transparent: true,
      opacity: 0.12,
      blending: THREE.AdditiveBlending,
      side: THREE.BackSide,
    });
    const glowMesh = new THREE.Mesh(glowGeo, glowMat);
    scene.add(glowMesh);

    // Realistic Lighting Setup
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.45);
    scene.add(ambientLight);

    const dirLight = new THREE.DirectionalLight(0xffffff, 0.9);
    dirLight.position.set(-10, 10, 10);
    scene.add(dirLight);

    // Dynamic Groups for Markers & Paths
    const markersGroup = new THREE.Group();
    markersGroupRef.current = markersGroup;
    scene.add(markersGroup);

    const pathsGroup = new THREE.Group();
    pathsGroupRef.current = pathsGroup;
    scene.add(pathsGroup);

    // Focus immediately on initial locations if there are any
    const currentLocs = selectedLocationsRef.current;
    if (currentLocs.length > 0) {
      const centroid = new THREE.Vector3();
      currentLocs.forEach((loc) => {
        centroid.add(get3DCoords(loc.lat, loc.lon, 1));
      });
      centroid.normalize();
      
      const targetPos = centroid.clone().multiplyScalar(13);
      camera.position.copy(targetPos);
      controls.update();
    }

    // Frame Update & CSS Overlay Label Projection loop
    const tempV = new THREE.Vector3();
    let animationId: number;

    const animate = () => {
      animationId = requestAnimationFrame(animate);

      // Smooth camera transition if target is set
      if (targetCameraPosRef.current && !isUserInteractingRef.current) {
        camera.position.lerp(targetCameraPosRef.current, 0.05);
        if (camera.position.distanceTo(targetCameraPosRef.current) < 0.01) {
          camera.position.copy(targetCameraPosRef.current);
          targetCameraPosRef.current = null;
        }
      }

      // Damp controls rotation momentum
      controls.update();

      // Project 3D coordinate nodes into absolute 2D screen positions for labels
      const container = labelsContainerRef.current;
      if (container) {
        const labels = container.children;
        const currentLocs = selectedLocationsRef.current;
        currentLocs.forEach((loc, idx) => {
          if (idx >= labels.length) return;
          const el = labels[idx] as HTMLElement;
          if (!el) return;

          const pos = get3DCoords(loc.lat, loc.lon, R);
          
          // Perform back-face culling by dot-product angle check with camera position
          const dot = pos.dot(camera.position);
          const isVisible = dot > R * R * 0.15; // Point must be on facing hemisphere

          if (!isVisible) {
            el.style.display = "none";
            return;
          }

          // Project to 2D screen coordinate pixels
          tempV.copy(pos);
          tempV.project(camera);
          
          const x = (tempV.x * 0.5 + 0.5) * mount.clientWidth;
          const y = (tempV.y * -0.5 + 0.5) * mount.clientHeight;

          el.style.display = "block";
          el.style.transform = `translate(-50%, -125%) translate(${x}px, ${y}px)`;
        });
      }

      renderer.render(scene, camera);
    };

    animate();

    // Resize handler
    const handleResize = () => {
      if (!mountRef.current) return;
      const w = mountRef.current.clientWidth;
      const h = mountRef.current.clientHeight;
      camera.aspect = w / h;
      camera.updateProjectionMatrix();
      renderer.setSize(w, h);
    };
    
    window.addEventListener("resize", handleResize);

    // Cleanup WebGL instances on unmount
    return () => {
      cancelAnimationFrame(animationId);
      window.removeEventListener("resize", handleResize);
      controls.dispose();
      
      if (mount.contains(renderer.domElement)) {
        mount.removeChild(renderer.domElement);
      }
      
      earthGeo.dispose();
      earthMat.dispose();
      earthTexture.dispose();
      glowGeo.dispose();
      glowMat.dispose();
      renderer.dispose();
    };
  }, []);

  // 2. State & Props Updates (Runs on changes to selectedLocations, tour, currentStep)
  useEffect(() => {
    const scene = sceneRef.current;
    const camera = cameraRef.current;
    const controls = controlsRef.current;
    const markersGroup = markersGroupRef.current;
    const pathsGroup = pathsGroupRef.current;

    if (!scene || !camera || !controls || !markersGroup || !pathsGroup) return;

    // Update markers
    while (markersGroup.children.length > 0) {
      const child = markersGroup.children[0];
      if (child instanceof THREE.Mesh) {
        child.geometry.dispose();
        if (Array.isArray(child.material)) {
          child.material.forEach((m) => m.dispose());
        } else {
          child.material.dispose();
        }
      }
      markersGroup.remove(child);
    }

    selectedLocations.forEach((loc, idx) => {
      const pos = get3DCoords(loc.lat, loc.lon, R);
      const isActive = tour.length > 0 && tour[currentStep] === idx;

      // Custom Pin marker
      const markerGeo = new THREE.SphereGeometry(isActive ? 0.14 : 0.08, 16, 16);
      const markerMat = new THREE.MeshBasicMaterial({
        color: isActive ? 0xc8652f : 0x204d6b,
      });
      const markerMesh = new THREE.Mesh(markerGeo, markerMat);
      markerMesh.position.copy(pos);
      markersGroup.add(markerMesh);

      // Subtly raise coordinate pins above the surface
      markerMesh.position.multiplyScalar(1.008);
    });

    // Update paths
    while (pathsGroup.children.length > 0) {
      const child = pathsGroup.children[0];
      if (child instanceof THREE.Line) {
        child.geometry.dispose();
        if (Array.isArray(child.material)) {
          child.material.forEach((m) => m.dispose());
        } else {
          child.material.dispose();
        }
      }
      pathsGroup.remove(child);
    }

    if (tour.length > 0) {
      // Draw active traversed lines
      const activePoints: THREE.Vector3[] = [];
      const stepsToRender = currentStep >= 0 ? currentStep : 0;

      for (let i = 0; i <= stepsToRender; i++) {
        if (i >= tour.length) break;
        const loc = selectedLocations[tour[i]];
        activePoints.push(get3DCoords(loc.lat, loc.lon, R * 1.006));
      }

      // Draw segment by segment using spherical interpolation (slerp)
      for (let i = 0; i < stepsToRender; i++) {
        const startNode = activePoints[i];
        const endNode = activePoints[i + 1];
        if (!startNode || !endNode) continue;

        const segmentPoints: THREE.Vector3[] = [];
        const divisions = 24;
        for (let step = 0; step <= divisions; step++) {
          const v = slerpVectors(startNode, endNode, step / divisions, R * 1.006);
          // Rise peak of the arc slightly to look like a flight path
          const heightOffset = 1.0 + Math.sin((step / divisions) * Math.PI) * 0.02;
          v.multiplyScalar(heightOffset);
          segmentPoints.push(v);
        }

        const activeLineGeo = new THREE.BufferGeometry().setFromPoints(segmentPoints);
        const activeLineMat = new THREE.LineBasicMaterial({
          color: 0xc8652f, // Warm orange for solved active tour path
        });
        const activeLine = new THREE.Line(activeLineGeo, activeLineMat);
        pathsGroup.add(activeLine);
      }

      // Draw remaining path segments
      if (currentStep < tour.length - 1) {
        for (let i = Math.max(0, currentStep); i < tour.length - 1; i++) {
          const startNode = get3DCoords(selectedLocations[tour[i]].lat, selectedLocations[tour[i]].lon, R * 1.006);
          const endNode = get3DCoords(selectedLocations[tour[i + 1]].lat, selectedLocations[tour[i + 1]].lon, R * 1.006);

          const segmentPoints: THREE.Vector3[] = [];
          const divisions = 24;
          for (let step = 0; step <= divisions; step++) {
            const v = slerpVectors(startNode, endNode, step / divisions, R * 1.006);
            const heightOffset = 1.0 + Math.sin((step / divisions) * Math.PI) * 0.015;
            v.multiplyScalar(heightOffset);
            segmentPoints.push(v);
          }

          const inactiveLineGeo = new THREE.BufferGeometry().setFromPoints(segmentPoints);
          const inactiveLineMat = new THREE.LineBasicMaterial({
            color: 0x94a3b8, // Light gray for pending path
            transparent: true,
            opacity: 0.5,
          });
          const inactiveLine = new THREE.Line(inactiveLineGeo, inactiveLineMat);
          pathsGroup.add(inactiveLine);
        }
      }
    }

    // Auto-focus camera on centroid of locations
    if (selectedLocations.length > 0) {
      const centroid = new THREE.Vector3();
      selectedLocations.forEach((loc) => {
        centroid.add(get3DCoords(loc.lat, loc.lon, 1));
      });
      centroid.normalize();

      const targetPos = centroid.clone().multiplyScalar(13);

      // Only transition camera if it's not already pointing in that general direction
      const currentCameraDir = camera.position.clone().normalize();
      const angle = currentCameraDir.angleTo(centroid);

      if (angle > 0.25) { // ~14 degrees threshold
        targetCameraPosRef.current = targetPos;
      }
    }
  }, [selectedLocations, tour, currentStep]);

  return (
    <div className="w-full h-full relative" ref={mountRef}>
      {/* Vanilla DOM overlay labels layer for performance (no React re-renders at 60 FPS) */}
      <div className="absolute inset-0 pointer-events-none z-10 overflow-hidden" ref={labelsContainerRef}>
        {locationsWithCoords.map((loc, idx) => {
          const isActive = tour.length > 0 && tour[currentStep] === idx;
          return (
            <div
              key={idx}
              style={{ display: "none", position: "absolute", left: 0, top: 0 }}
              className={`absolute pointer-events-none select-none text-[9px] px-1.5 py-0.5 rounded shadow-sm border font-sans whitespace-nowrap leading-none transition-all duration-75 ${
                isActive
                  ? "bg-accentWarm text-white border-accentWarm font-bold scale-110"
                  : "bg-white/95 text-slate-800 border-slate-200"
              }`}
            >
              {loc.name}
            </div>
          );
        })}
      </div>
    </div>
  );
}
