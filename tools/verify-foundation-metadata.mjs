import { readFile, readdir } from "node:fs/promises";
import { join, resolve } from "node:path";

const root = resolve(import.meta.dirname, "..");
const errors = [];

const rootPackage = JSON.parse(await read("package.json"));
assert(rootPackage.packageManager.startsWith("pnpm@11.25.0+sha512."), "pnpm must be exact and integrity-pinned");
assert(rootPackage.engines.node === "24.20.0", "Node engine must match .nvmrc");
assert((await read(".nvmrc")).trim() === "24.20.0", ".nvmrc must pin Node 24.20.0");

const compose = await read("compose.yaml");
assert(
  compose.includes("postgis/postgis:16-3.5@sha256:94146ac37bc61e2322f88016056c5920729cb8c64c8542ed590af8fc2abdac07"),
  "Compose must use the approved immutable PostGIS reference",
);
assert(
  compose.includes("axllent/mailpit:v1.31.0@sha256:c96991d9bef73594c246d89ca81411d4e916f03e76a7d2d72fa2ab5dd3c9ce24"),
  "Compose must use the approved immutable Mailpit reference",
);

const gradle = await read("backend/build.gradle.kts");
assert(gradle.includes('id("org.springframework.boot") version "4.1.1"'), "Spring Boot must remain 4.1.1");
assert(gradle.includes('testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))'), "Testcontainers BOM must remain 2.0.5");
assert(!/junit[^\n]*["']5\./i.test(gradle), "No direct JUnit 5 declaration is allowed");

const wrapper = await read("backend/gradle/wrapper/gradle-wrapper.properties");
assert(wrapper.includes("gradle-9.7.1-bin.zip"), "Gradle wrapper must remain 9.7.1");
assert(wrapper.includes("distributionSha256Sum=acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a"), "Gradle distribution checksum is missing or changed");

const packageFiles = await findNamed(root, "package.json");
for (const packageFile of packageFiles) {
  if (packageFile.includes("node_modules")) continue;
  const packageJson = JSON.parse(await readFile(packageFile, "utf8"));
  for (const section of ["dependencies", "devDependencies", "optionalDependencies"]) {
    for (const [name, version] of Object.entries(packageJson[section] ?? {})) {
      if (String(version).startsWith("workspace:")) continue;
      assert(/^\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?$/.test(String(version)), `${name} in ${packageFile} is not exact: ${version}`);
    }
  }
}

const workflowDirectory = join(root, ".github/workflows");
try {
  const workflows = await readdir(workflowDirectory);
  for (const workflow of workflows.filter((name) => name.endsWith(".yml") || name.endsWith(".yaml"))) {
    const content = await readFile(join(workflowDirectory, workflow), "utf8");
    for (const match of content.matchAll(/uses:\s*[^\s]+@([^\s#]+)/g)) {
      assert(/^[0-9a-f]{40}$/.test(match[1]), `${workflow} contains a mutable action reference: ${match[0]}`);
    }
  }
} catch (error) {
  if (error.code !== "ENOENT") throw error;
}

assert(!(await exists("backend/src/main/resources/db/migration")), "T01 must not contain Flyway migrations");
assert(!(await exists("turbo.json")), "Turborepo implementation is deferred from T01");

if (errors.length > 0) {
  console.error(errors.join("\n"));
  process.exitCode = 1;
} else {
  console.log("Foundation metadata verification passed.");
}

function assert(condition, message) {
  if (!condition) errors.push(`- ${message}`);
}

async function read(path) {
  return readFile(join(root, path), "utf8");
}

async function exists(path) {
  try {
    await readdir(join(root, path));
    return true;
  } catch (error) {
    if (error.code === "ENOENT") return false;
    throw error;
  }
}

async function findNamed(directory, name) {
  const entries = await readdir(directory, { withFileTypes: true, recursive: true });
  return entries
    .filter((entry) => entry.isFile() && entry.name === name)
    .map((entry) => join(entry.parentPath, entry.name));
}
