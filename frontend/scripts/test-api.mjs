import assert from "node:assert/strict";
import { spawnSync } from "node:child_process";
import { createRequire } from "node:module";
import { mkdirSync, writeFileSync } from "node:fs";
import { resolve } from "node:path";

// Compile only the data adapters. Generated files stay inside this project.
const directory = resolve(".cache/api-tests");
mkdirSync(directory, { recursive: true });
writeFileSync(resolve(directory, "package.json"), '{"type":"commonjs"}');
const compiled = spawnSync(process.execPath, ["node_modules/typescript/bin/tsc", "src/lib/movements.ts", "--module", "commonjs", "--target", "ES2022", "--lib", "ES2022,DOM", "--skipLibCheck", "--outDir", directory], { stdio: "inherit" });
assert.equal(compiled.status, 0);
process.env.NEXT_PUBLIC_DATA_MODE = "api";
const require = createRequire(import.meta.url);
const session = require(resolve(directory, "session.js"));
const movements = require(resolve(directory, "movements.js"));
const originalFetch = globalThis.fetch;
let calls = [];
let respond;
globalThis.fetch = async (url, init) => {
  calls.push({ url, init });
  return respond(url, init);
};
const json = (body, status = 200) => new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
try {
  session.setToken("test-token");
  respond = () => json({ ok: true });
  await session.request("/profile");
  assert.equal(calls.at(-1).init.headers.get("Authorization"), "Bearer test-token");
  assert.equal(calls.at(-1).init.cache, "no-store");
  respond = () => json({ detail: "Arquivo rejeitado" }, 415);
  await assert.rejects(session.request("/documents"), /Arquivo rejeitado/);
  respond = () => { throw new TypeError("Failed to fetch"); };
  await assert.rejects(session.request("/profile"), /API indisponível/);
  respond = () => json({balanceCents:Number.MAX_SAFE_INTEGER+1});
  await assert.rejects(session.request("/finance/summary"), /precisão suportada/);
  respond = () => json({}, 401);
  await assert.rejects(session.request("/profile"), /sessão encerrada/);
  respond = () => json({});
  await session.request("/profile");
  assert.equal(calls.at(-1).init.headers.has("Authorization"), false);

  let page = 0;
  respond = () => json({ items: [++page], totalPages: 2 });
  assert.deepEqual(await session.allPages("/movements?deleted=false"), [1, 2]);
  assert.match(calls.at(-1).url, /deleted=false&size=100&page=1$/);

  const input = { id: "temporary", type: "entrada", description: "Teste", valueInCents: 100, date: "2026-10-22", category: "Outros", account: "Conta principal", notes: "", createdAt: "2026-10-22T12:00:00Z", attachment: { name: "test.pdf", type: "application/pdf", size: 5, blob: new Blob(["%PDF-"]) } };
  calls = [];
  respond = (url) => url.endsWith("/attachment") ? json({ detail: "Limite de anexos" }, 507) : json({ id: "server-id" }, 201);
  await assert.rejects(movements.saveMovement(input), error => error instanceof movements.MovementSavedError && error.movementId === "server-id");
  assert.equal(input.id, "server-id");
  respond = (url) => url.endsWith("/attachment") ? json({}) : json({ id: "server-id" });
  await movements.updateMovement(input);
  assert.equal(calls.filter(x => x.init.method === "POST").length, 1, "Retry must not create another record");
  assert.equal(calls.at(-1).init.body.get("file").name, "test.pdf");

  const stored = { ...input, attachment: undefined };
  calls = [];
  respond = (url) => url.includes("/documents") ? json({ items: [{ movementId: stored.id, name: "test.pdf", contentType: "application/pdf", size: 5 }], totalPages: 1 }) : json({ items: [stored], totalPages: 1 });
  const listed = await movements.listMovements();
  assert.equal(listed[0].attachment.name, "test.pdf");
  assert.equal(calls.length, 2, "Lists must load metadata without downloading files");
  respond = () => new Response("%PDF-", { headers: { "Content-Type": "application/pdf" } });
  assert.equal(await (await movements.attachmentBlob(listed[0])).text(), "%PDF-");
  console.log("API adapters: authorization, errors, pagination, partial upload retry and lazy download passed.");
} finally { globalThis.fetch = originalFetch; }
