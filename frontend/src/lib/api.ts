const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export async function checkApiHealth(): Promise<boolean> {
  const response = await fetch(`${apiUrl.replace(/\/$/, "")}/actuator/health`, {
    signal: AbortSignal.timeout(8000),
    cache: "no-store",
  });
  if (!response.ok) throw new Error("API indisponível");
  const health: { status: string } = await response.json();
  return health.status === "UP";
}
