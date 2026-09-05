import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import ErrorView from "./error";
import Loading from "./loading";
import HomePage from "./page";

describe("public foundation shell", () => {
  it("renders the neutral shell with a French language link", () => {
    render(<HomePage />);

    expect(screen.getByRole("main")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: /foundation ready/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /français/i })).toHaveAttribute("href", "/fr");
  });

  it("provides accessible loading and safe error primitives", () => {
    const { rerender } = render(<Loading />);
    expect(screen.getByRole("status")).toHaveTextContent("Loading");

    rerender(<ErrorView reset={() => undefined} />);
    expect(screen.getByRole("alert")).not.toHaveTextContent(/stack|sql|hostname/i);
    expect(screen.getByRole("button", { name: /try again/i })).toBeInTheDocument();
  });
});
