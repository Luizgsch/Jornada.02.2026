import { spawnSync } from "node:child_process";

const development = process.argv.includes("--dev");
const apiUrl = development
  ? process.env.ANDROID_API_URL ?? "http://10.0.2.2:8080"
  : process.env.ANDROID_API_URL;

if (!apiUrl || (!development && !apiUrl.startsWith("https://"))) {
  console.error("Defina ANDROID_API_URL com HTTPS para o build Android de producao.");
  process.exit(1);
}

const url = new URL(apiUrl);
if (!["http:", "https:"].includes(url.protocol) || url.username || url.password) {
  throw new Error("URL da API invalida. Nao inclua credenciais na URL.");
}

const result = spawnSync(process.execPath, ["node_modules/next/dist/bin/next", "build"], {
  stdio: "inherit",
  env: { ...process.env, PAGES_BASE_PATH: "", NEXT_PUBLIC_DATA_MODE: "api", NEXT_PUBLIC_API_URL: url.origin },
});
process.exit(result.status ?? 1);
