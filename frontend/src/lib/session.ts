export const apiMode = process.env.NEXT_PUBLIC_DATA_MODE !== "local-demo";
export const apiUrl = (process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080").replace(/\/$/, "");
let token: string | null = null;
export function setToken(value: string | null) { token = value; }
export function expireSession() {
  token = null;
  if (typeof window !== "undefined") window.dispatchEvent(new Event("cash-session-expired"));
}
export async function request<T>(path: string, init: RequestInit = {}, binary = false): Promise<T> {
  const headers = new Headers(init.headers);
  if (token) headers.set("Authorization", `Bearer ${token}`);
  if (typeof init.body === "string") headers.set("Content-Type", "application/json");
  let response: Response;
  try {
    response = await fetch(`${apiUrl}/api/v1${path}`, { ...init, headers, cache: "no-store", signal: AbortSignal.timeout(15000) });
  } catch { throw new Error("API indisponível. Confira se o backend está ligado e tente novamente."); }
  if (!response.ok) {
    if (response.status === 401 && path !== "/auth/login") expireSession();
    const problem = await response.json().catch(() => null);
    throw new Error(response.status === 401 ? "Credenciais inválidas ou sessão encerrada. Entre novamente." : problem?.detail ?? `Falha na API (${response.status}).`);
  }
  if (response.status === 204) return undefined as T;
  if (binary) return await response.blob() as T;
  const result = await response.json();
  validateNumbers(result);
  return result as T;
}
function validateNumbers(value: unknown): void {
  if (typeof value === "number" && !Number.isSafeInteger(value)) throw new Error("Valor fora da precisão suportada. Consulta indisponível.");
  if (Array.isArray(value)) value.forEach(validateNumbers);
  else if (value && typeof value === "object") Object.values(value).forEach(validateNumbers);
}
export async function allPages<T>(path: string): Promise<T[]> {
  const items: T[] = [];
  for (let page = 0; ; page++) {
    const result = await request<{items: T[]; totalPages: number}>(`${path}${path.includes("?") ? "&" : "?"}size=100&page=${page}`);
    items.push(...result.items);
    if (page + 1 >= result.totalPages) return items;
  }
}
