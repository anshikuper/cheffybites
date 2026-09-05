import type { Metadata } from "next";
import type { ReactNode } from "react";
import { QueryProvider } from "../components/providers/QueryProvider";
import "./globals.css";

export const metadata: Metadata = {
  title: "Cheffy Bites",
  description: "Cheffy Bites foundation shell",
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="en-CA">
      <body>
        <QueryProvider>{children}</QueryProvider>
      </body>
    </html>
  );
}
