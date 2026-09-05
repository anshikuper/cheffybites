import { expect, test } from "@playwright/test";

test("serves static English and French public shells", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: /foundation ready/i })).toBeVisible();
  await expect(page.locator("html")).toHaveAttribute("lang", "en-CA");

  await page.getByRole("link", { name: /français/i }).click();
  await expect(page.getByRole("heading", { name: /fondation prête/i })).toBeVisible();
  await expect(page.locator("main")).toHaveAttribute("lang", "fr-CA");
});

for (const path of ["/app/operator", "/app/chef"]) {
  test(`${path} fails closed and is not publicly cacheable`, async ({ request }) => {
    const response = await request.get(path);

    expect(response.status()).toBe(404);
    expect(response.headers()["cache-control"]).toContain("private");
    expect(response.headers()["cache-control"]).toContain("no-store");
  });
}
