import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["var(--font-sans)", "ui-sans-serif", "system-ui"],
        mono: ["var(--font-mono)", "ui-monospace", "SFMono-Regular"]
      },
      colors: {
        ink: "var(--ink)",
        inkMuted: "var(--ink-muted)",
        panel: "var(--panel)",
        panelSoft: "var(--panel-soft)",
        accent: "var(--accent)",
        accentWarm: "var(--accent-warm)",
        grid: "var(--grid)",
        border: "var(--border)"
      },
      boxShadow: {
        panel: "0 16px 40px rgba(15, 23, 42, 0.12)",
        insetSoft: "inset 0 0 0 1px rgba(15, 23, 42, 0.06)"
      },
      keyframes: {
        rise: {
          "0%": { opacity: "0", transform: "translateY(10px)" },
          "100%": { opacity: "1", transform: "translateY(0)" }
        }
      },
      animation: {
        rise: "rise 0.4s ease-out both"
      }
    }
  },
  plugins: []
};

export default config;
