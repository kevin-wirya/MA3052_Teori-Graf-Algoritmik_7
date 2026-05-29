"use client";

import { useMemo, useState } from "react";
import { GraphAlgorithm } from "@/lib/algorithms/types";
import { AlgorithmRegistry } from "@/lib/algorithms/registry";

interface Props {
  registry: AlgorithmRegistry;
  selected: GraphAlgorithm | null;
  onSelect: (algo: GraphAlgorithm) => void;
  isOpen: boolean;
  onToggle: () => void;
}

const getCategoryIcon = (category: string) => {
  const lower = category.toLowerCase();
  if (lower.includes("traversal")) return "🔍";
  if (lower.includes("shortest") || lower.includes("path")) return "🛣️";
  if (lower.includes("spanning") || lower.includes("tree") || lower.includes("mst")) return "🌲";
  if (lower.includes("connect")) return "🔗";
  if (lower.includes("matching") || lower.includes("bipartite")) return "🤝";
  if (lower.includes("tsp") || lower.includes("salesman")) return "🎯";
  if (lower.includes("properties") || lower.includes("diameter")) return "📊";
  return "⚙️";
};

export default function AlgorithmSidebar({ registry, selected, onSelect, isOpen, onToggle }: Props) {
  const categories = useMemo(() => registry.getCategories(), [registry]);
  const [openCategories, setOpenCategories] = useState<Record<string, boolean>>(() =>
    Object.fromEntries(categories.map((category) => [category, true]))
  );
  const [searchQuery, setSearchQuery] = useState("");
  const [infoAlgo, setInfoAlgo] = useState<GraphAlgorithm | null>(null);

  const toggleCategory = (category: string) => {
    setOpenCategories((prev) => ({
      ...prev,
      [category]: !prev[category]
    }));
  };

  const handleCategoryIconClick = (category: string) => {
    setOpenCategories((prev) => ({
      ...prev,
      [category]: true
    }));
    if (!isOpen) {
      onToggle();
    }
  };

  // Filter categories and their algorithms based on search query
  const filteredCategories = useMemo(() => {
    if (!searchQuery) return categories;
    return categories.filter((cat) =>
      registry.getByCategory(cat).some((algo) =>
        algo.name.toLowerCase().includes(searchQuery.toLowerCase())
      )
    );
  }, [categories, registry, searchQuery]);

  return (
    <div className="flex h-full flex-col overflow-hidden rounded-2xl border border-border bg-panel shadow-panel transition-all duration-300">
      {/* Header Panel */}
      <div className="flex items-center justify-between border-b border-border px-3 py-3">
        {isOpen ? (
          <>
            <span className="text-xs font-bold uppercase tracking-wider text-accent">Algorithms</span>
            <button
              onClick={onToggle}
              className="rounded-lg p-1 hover:bg-panel-soft text-inkMuted transition-colors"
              title="Collapse Sidebar"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
              </svg>
            </button>
          </>
        ) : (
          <button
            onClick={onToggle}
            className="mx-auto rounded-lg p-1 hover:bg-panel-soft text-inkMuted transition-colors"
            title="Expand Sidebar"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 5l7 7-7 7M5 5l7 7-7 7" />
            </svg>
          </button>
        )}
      </div>

      {isOpen && (
        <div className="px-3 py-2 border-b border-border/50">
          <div className="relative flex items-center">
            <input
              type="text"
              placeholder="Search algorithm..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full rounded-lg border border-border bg-white pl-8 pr-3 py-1.5 text-xs focus:border-accent focus:outline-none transition-colors"
            />
            <span className="absolute left-2.5 text-inkMuted">
              <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </span>
            {searchQuery && (
              <button
                onClick={() => setSearchQuery("")}
                className="absolute right-2 text-inkMuted hover:text-ink text-xs"
              >
                ✕
              </button>
            )}
          </div>
        </div>
      )}

      {/* Main List */}
      <div className="flex-1 overflow-y-auto p-2 space-y-2">
        {isOpen ? (
          filteredCategories.length > 0 ? (
            filteredCategories.map((category) => {
              const isOpenCat = openCategories[category] ?? true;
              const algos = registry.getByCategory(category).filter((algo) =>
                algo.name.toLowerCase().includes(searchQuery.toLowerCase())
              );

              return (
                <div key={category} className="mb-2">
                  <button
                    type="button"
                    className="flex w-full items-center justify-between px-2 py-1 text-[10px] font-bold uppercase tracking-wider text-inkMuted hover:text-ink transition-colors"
                    onClick={() => toggleCategory(category)}
                  >
                    <div className="flex items-center gap-1.5">
                      <span>{getCategoryIcon(category)}</span>
                      <span>{category}</span>
                    </div>
                    <span className="text-[9px]">{isOpenCat ? "▼" : "▶"}</span>
                  </button>
                  {isOpenCat && (
                    <div className="mt-1 flex flex-col gap-1 pl-2">
                      {algos.map((algo) => {
                        const active = selected?.name === algo.name;
                        return (
                          <div
                            key={algo.name}
                            className={`flex items-center justify-between rounded-lg px-2.5 py-0.5 transition duration-200 group/item ${
                              active
                                ? "bg-accent/10 text-accent font-semibold border-l-2 border-accent"
                                : "bg-transparent text-ink hover:bg-panel-soft"
                            }`}
                          >
                            <button
                              type="button"
                              className="flex-1 text-left text-xs py-1.5 select-none focus:outline-none"
                              onClick={() => onSelect(algo)}
                            >
                              {algo.name}
                            </button>
                            <button
                              type="button"
                              onClick={(e) => {
                                e.stopPropagation();
                                setInfoAlgo(algo);
                              }}
                              className="opacity-45 hover:opacity-100 hover:scale-110 p-1 text-[10px] shrink-0 transition-all cursor-pointer select-none focus:outline-none"
                              title={`Info ${algo.name}`}
                            >
                              ℹ️
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              );
            })
          ) : (
            <div className="text-center text-xs text-inkMuted py-4">
              No algorithms found
            </div>
          )
        ) : (
          <div className="flex flex-col items-center gap-2 py-2">
            {categories.map((category) => {
              const isSelectedCategory = selected && selected.category === category;
              return (
                <button
                  key={category}
                  onClick={() => handleCategoryIconClick(category)}
                  className={`rounded-xl p-2 text-base transition-all duration-200 hover:scale-110 relative group ${
                    isSelectedCategory
                      ? "bg-accent/15 border border-accent/20 text-accent scale-105"
                      : "bg-panel-soft hover:bg-border/30 text-inkMuted"
                  }`}
                >
                  {getCategoryIcon(category)}
                  {/* Custom Tooltip */}
                  <div className="absolute left-full ml-2 px-2 py-1 bg-ink text-white text-[10px] rounded-md whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity duration-150 z-50 shadow-md">
                    {category}
                  </div>
                </button>
              );
            })}
          </div>
        )}
      </div>

      {/* Info Modal Dialog */}
      {infoAlgo && (
        <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-fade-in">
          <div className="bg-panel border border-border shadow-panel rounded-3xl p-6 max-w-md w-full relative animate-scale-up space-y-4">
            <div className="flex items-start justify-between">
              <div>
                <span className="text-[9px] font-bold text-accent uppercase tracking-wider bg-accent/10 px-2.5 py-1 rounded-full">
                  Kategori: {infoAlgo.category}
                </span>
                <h3 className="text-base font-bold text-ink mt-2">{infoAlgo.name}</h3>
              </div>
              <button
                type="button"
                onClick={() => setInfoAlgo(null)}
                className="text-inkMuted hover:text-ink text-sm p-1 rounded-lg hover:bg-panel-soft transition focus:outline-none"
              >
                ✕
              </button>
            </div>
            
            <div className="text-xs text-inkMuted leading-relaxed bg-panel-soft p-4 rounded-2xl border border-border/40 whitespace-pre-wrap">
              {infoAlgo.description || "Tidak ada penjelasan tertulis untuk algoritma ini."}
            </div>

            <div className="flex justify-end pt-1">
              <button
                type="button"
                onClick={() => setInfoAlgo(null)}
                className="px-4 py-2 bg-accent text-white text-xs font-bold rounded-xl shadow-sm hover:bg-accent-hover transition-colors focus:outline-none"
              >
                Tutup
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
