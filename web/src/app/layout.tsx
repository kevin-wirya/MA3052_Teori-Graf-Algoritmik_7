import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Proyek Akhir MA3052 Teori Graf Algoritmik - 7",
  description: "Graph algorithm visualizer in the browser",
  icons: {
    icon: "/logo.png"
  }
};

export default function RootLayout({
  children
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@300..700&family=IBM+Plex+Mono:wght@400;600&display=swap" rel="stylesheet" />
      </head>
      <body>{children}</body>
    </html>
  );
}
