export const supportedLocales = ["en-CA", "fr-CA"] as const;

export type SupportedLocale = (typeof supportedLocales)[number];

export const localePaths: Readonly<Record<SupportedLocale, string>> = Object.freeze({
  "en-CA": "/",
  "fr-CA": "/fr",
});
