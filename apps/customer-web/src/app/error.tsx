"use client";

export default function GlobalError({ reset }: Readonly<{ reset: () => void }>) {
  return (
    <main className="shell" role="alert">
      <h1>Something went wrong</h1>
      <p>The page could not be loaded. No private diagnostic details are shown.</p>
      <button type="button" onClick={reset}>
        Try again
      </button>
    </main>
  );
}
