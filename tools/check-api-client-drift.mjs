import { mkdtemp, readFile, readdir, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { basename, join, relative, resolve } from "node:path";
import { spawnSync } from "node:child_process";

const repositoryRoot = resolve(import.meta.dirname, "..");
const acceptedDirectory = join(repositoryRoot, "packages/api-client/src/generated");
const temporaryRoot = await mkdtemp(join(tmpdir(), "cheffy-api-client-"));
const generatedDirectory = join(temporaryRoot, "generated");

try {
  const gradle = process.platform === "win32" ? "gradlew.bat" : "./gradlew";
  const result = spawnSync(
    gradle,
    ["generateApiClient", `-PapiClientOutput=${generatedDirectory}`, "--no-configuration-cache"],
    {
      cwd: join(repositoryRoot, "backend"),
      encoding: "utf8",
      env: {
        ...process.env,
        JAVA_HOME: process.env.JAVA_HOME ?? process.env.JDK21_HOME,
      },
      stdio: "inherit",
    },
  );

  if (result.status !== 0) {
    throw new Error("OpenAPI generation failed.");
  }

  await rm(join(generatedDirectory, ".openapi-generator"), { recursive: true, force: true });


  const acceptedFiles = await listFiles(acceptedDirectory);
  const generatedFiles = await listFiles(generatedDirectory);
  if (JSON.stringify(acceptedFiles) !== JSON.stringify(generatedFiles)) {
    throw new Error(`Generated client file set differs. Accepted=${acceptedFiles}; generated=${generatedFiles}`);
  }

  for (const file of acceptedFiles) {
    const [accepted, generated] = await Promise.all([
      readFile(join(acceptedDirectory, file)),
      readFile(join(generatedDirectory, file)),
    ]);
    if (!accepted.equals(generated)) {
      throw new Error(`Generated client drift detected in ${file}.`);
    }
  }

  console.log(`API client drift check passed (${acceptedFiles.length} files).`);
} finally {
  await rm(temporaryRoot, { recursive: true, force: true });
}

async function listFiles(root) {
  const entries = await readdir(root, { withFileTypes: true, recursive: true });
  return entries
    .filter((entry) => entry.isFile() && !entry.parentPath.includes(".openapi-generator"))
    .map((entry) => relative(root, join(entry.parentPath, basename(entry.name))))
    .sort();
}
