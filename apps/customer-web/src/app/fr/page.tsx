import Link from "next/link";

export default function FrenchHomePage() {
  return (
    <main className="shell" lang="fr-CA">
      <section className="panel" aria-labelledby="foundation-heading">
        <p>Cheffy Bites</p>
        <h1 id="foundation-heading" className="text-3xl font-semibold">
          Fondation prête pour la prochaine étape approuvée
        </h1>
        <p>Ce point de contrôle fournit une structure reproductible sans contenu de marché.</p>
        <Link href="/" hrefLang="en-CA" lang="en-CA">
          English (Canada)
        </Link>
      </section>
    </main>
  );
}
