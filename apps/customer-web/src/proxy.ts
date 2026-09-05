export function proxy() {
  return new Response(null, {
    status: 404,
    headers: {
      "Cache-Control": "private, no-store, max-age=0",
      Pragma: "no-cache",
    },
  });
}

export const config = {
  matcher: ["/app/operator/:path*", "/app/chef/:path*"],
};