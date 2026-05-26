"use client";

interface Props {
  open: boolean;
  onClose: () => void;
  data: Record<string, unknown>;
}

export default function TimetableModal({ open, onClose, data }: Props) {
  const timetable = data.timetable as number[][][] | undefined;
  const periodCount = typeof data.periodCount === "number" ? data.periodCount : 0;
  const classroomLimit = typeof data.classroomLimit === "number" ? data.classroomLimit : 0;
  const totalLectures = typeof data.totalLectures === "number" ? data.totalLectures : 0;
  const lowerBound = typeof data.lowerBound === "number" ? data.lowerBound : 0;

  if (!open || !timetable) return null;

  // Extract unique Left nodes (teachers/classes) and Right nodes (rooms/courses)
  const leftNodesSet = new Set<number>();
  const rightNodesSet = new Set<number>();

  timetable.forEach((periodMatches) => {
    periodMatches.forEach(([u, v]) => {
      leftNodesSet.add(u);
      rightNodesSet.add(v);
    });
  });

  const leftNodes = Array.from(leftNodesSet).sort((a, b) => a - b);

  // Build mapping from [leftNode][periodIndex] -> rightNode
  const allocation = new Map<number, Map<number, number>>();
  leftNodes.forEach((leftId) => {
    allocation.set(leftId, new Map());
  });

  timetable.forEach((periodMatches, periodIdx) => {
    periodMatches.forEach(([u, v]) => {
      allocation.get(u)?.set(periodIdx, v);
    });
  });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <div className="max-h-[90vh] w-full max-w-4xl overflow-auto rounded-2xl bg-panel p-6 shadow-panel border border-border flex flex-col">
        {/* Modal Header */}
        <div className="flex items-center justify-between border-b border-border pb-4 shrink-0">
          <div>
            <h2 className="text-lg font-bold text-ink">Timetabling Allocation Matrix</h2>
            <p className="text-xs text-inkMuted mt-1">
              Classroom limit (k) = {classroomLimit} | Total lectures = {totalLectures}
            </p>
          </div>
          <button
            className="rounded-xl border border-border bg-white px-3.5 py-1.5 text-xs font-bold text-ink hover:bg-panel-soft transition"
            onClick={onClose}
          >
            Close Window
          </button>
        </div>

        {/* Info Cards */}
        <div className="mt-4 grid grid-cols-3 gap-4 shrink-0">
          <div className="rounded-xl border border-border p-4 bg-white shadow-sm flex flex-col items-center">
            <span className="text-xs text-inkMuted font-medium uppercase tracking-wider">Scheduled Periods</span>
            <span className="text-3xl font-extrabold text-ink mt-1 font-mono">{periodCount}</span>
          </div>
          <div className="rounded-xl border border-border p-4 bg-white shadow-sm flex flex-col items-center">
            <span className="text-xs text-inkMuted font-medium uppercase tracking-wider">Lower Bound (Optimal)</span>
            <span className="text-3xl font-extrabold text-blue-600 mt-1 font-mono">{lowerBound}</span>
          </div>
          <div className="rounded-xl border border-border p-4 bg-emerald-50/50 border-emerald-100 shadow-sm flex flex-col items-center justify-center">
            <span className="text-xs text-emerald-800 font-medium uppercase tracking-wider">Status</span>
            <span className="text-base font-bold text-emerald-700 mt-1">
              {periodCount === lowerBound ? "⭐ Optimal Schedule" : "✓ Schedule Found"}
            </span>
          </div>
        </div>

        {/* Matrix Container */}
        <div className="mt-6 flex-1 overflow-auto rounded-xl border border-border bg-white p-4">
          <table className="w-full border-collapse text-[10px]">
            <thead>
              <tr>
                <th className="border-b-2 border-r-2 border-border bg-panel-soft p-1.5 text-left font-bold text-inkMuted uppercase">
                  Left Node (Source)
                </th>
                {timetable.map((_, idx) => (
                  <th key={`p-${idx}`} className="border-b-2 border-border bg-panel-soft p-1.5 text-center font-bold text-ink">
                    Period {idx + 1}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {leftNodes.map((leftId) => (
                <tr key={leftId} className="hover:bg-panel-soft/50 transition-colors">
                  <td className="border-r-2 border-b border-border bg-panel-soft p-1.5 font-bold font-mono text-ink">
                    Node {leftId}
                  </td>
                  {timetable.map((_, periodIdx) => {
                    const matchVal = allocation.get(leftId)?.get(periodIdx);
                    return (
                      <td
                        key={`c-${leftId}-${periodIdx}`}
                        className={`border-b border-border p-1.5 text-center font-mono ${
                          matchVal !== undefined
                            ? "bg-blue-50/70 font-semibold text-blue-700"
                            : "text-inkMuted/40"
                        }`}
                      >
                        {matchVal !== undefined ? `➔ Node ${matchVal}` : "-"}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
