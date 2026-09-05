import Link from "next/link";

export default function HomePage() {
  return (
    <main className="shell">
      <section className="panel" aria-labelledby="foundation-heading">
        <p>Cheffy Bites</p>
        <h1 id="foundation-heading" className="text-3xl font-semibold">
          Foundation ready for the next approved slice
        </h1>
        <p>This checkpoint provides a reproducible application shell without marketplace content.</p>
        <Link href="/fr" hrefLang="fr-CA" lang="fr-CA">
          Français (Canada)
        </Link>
      </section>
    </main>
  );
}
