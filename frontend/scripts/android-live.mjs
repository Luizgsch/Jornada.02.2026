import { spawn, spawnSync } from "node:child_process";

const capacitor = "node_modules/@capacitor/cli/bin/capacitor";
const liveEnv = { ...process.env, CAP_LIVE_RELOAD: "1" };
const synced = spawnSync(process.execPath, [capacitor, "sync", "android"], {
  stdio: "inherit", env: liveEnv,
});
if (synced.status !== 0) process.exit(synced.status ?? 1);

console.log("Live Reload preparado. Execute app (debug) no Android Studio uma vez.");
console.log("Mantenha este terminal aberto. Alteracoes nas telas atualizam o emulador.");

const server = spawn(process.execPath, [
  "node_modules/next/dist/bin/next", "dev", "--hostname", "127.0.0.1", "--port", "3000",
], {
  stdio: "inherit",
  env: { ...process.env, PAGES_BASE_PATH: "", NEXT_PUBLIC_DATA_MODE: "api", NEXT_PUBLIC_API_URL: "http://10.0.2.2:8080" },
});

let cleaned = false;
function cleanup() {
  if (cleaned) return;
  cleaned = true;
  // Restaura configuracao de assets locais para a proxima compilacao.
  spawnSync(process.execPath, [capacitor, "copy", "android"], {
    stdio: "inherit", env: { ...process.env, CAP_LIVE_RELOAD: "0" },
  });
}
for (const signal of ["SIGINT", "SIGTERM"]) {
  process.on(signal, () => {
    server.kill(signal);
    cleanup();
    process.exit(0);
  });
}
server.on("error", (error) => { console.error(error.message); cleanup(); process.exitCode = 1; });
server.on("exit", (code) => { cleanup(); process.exitCode = code ?? 1; });
