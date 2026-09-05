import type { NextConfig } from "next";

const privateHeaders = [
  { key: "Cache-Control", value: "private, no-store, max-age=0" },
  { key: "Pragma", value: "no-cache" },
  { key: "Vary", value: "Cookie, Authorization" },
];

const nextConfig: NextConfig = {
  poweredByHeader: false,
  reactStrictMode: true,
  async headers() {
    return [
      {
        source: "/app/:path*",
        headers: privateHeaders,
      },
    ];
  },
};

export default nextConfig;
