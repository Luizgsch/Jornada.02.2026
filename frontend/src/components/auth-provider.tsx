"use client";
import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { usePathname, useRouter } from "next/navigation";
import { apiMode, expireSession, request, setToken } from "@/lib/session";

const Context = createContext<{authenticated: boolean; login: (email: string, password: string) => Promise<void>; logout: () => Promise<void>} | null>(null);
export function AuthProvider({children, basePath = ""}: {children: ReactNode; basePath?: string}) {
  const [authenticated, setAuthenticated] = useState(false);
  const [expiresAt, setExpiresAt] = useState(0);
  const pathname = usePathname();
  const router = useRouter();
  const route = (basePath && pathname.startsWith(basePath) ? pathname.slice(basePath.length) : pathname).replace(/\/$/, "") || "/";
  const publicRoute = route === "/" || route === "/recuperar-senha";
  useEffect(() => {
    const expired = () => { setAuthenticated(false); setExpiresAt(0); };
    window.addEventListener("cash-session-expired", expired);
    return () => window.removeEventListener("cash-session-expired", expired);
  }, []);
  useEffect(() => { if (!authenticated && !publicRoute) router.replace("/"); }, [authenticated, publicRoute, router]);
  useEffect(() => {
    if (!expiresAt) return;
    const timer = setTimeout(expireSession, Math.max(0, expiresAt - Date.now()));
    const check = () => { if (Date.now() >= expiresAt) expireSession(); };
    window.addEventListener("focus", check);
    return () => { clearTimeout(timer); window.removeEventListener("focus", check); };
  }, [expiresAt]);
  async function login(email: string, password: string) {
    if (apiMode) {
      const result = await request<{accessToken: string; expiresAt: string}>("/auth/login", {method: "POST", body: JSON.stringify({email: email.trim(), password})});
      setToken(result.accessToken); setExpiresAt(Date.parse(result.expiresAt));
    } else if (email.trim() !== "demo@cash.local" || password !== "Cash123!") throw new Error("Use os dados públicos de teste abaixo.");
    setAuthenticated(true);
  }
  async function logout() {
    try { if (apiMode) await request("/auth/logout", {method: "POST"}); }
    finally { expireSession(); router.replace("/"); }
  }
  return <Context.Provider value={{authenticated, login, logout}}>{publicRoute || authenticated ? children : <main className="login-shell"><p role="status">Entre para acessar o Cash.</p></main>}</Context.Provider>;
}
export function useAuth() { const value = useContext(Context); if (!value) throw new Error("AuthProvider ausente"); return value; }
