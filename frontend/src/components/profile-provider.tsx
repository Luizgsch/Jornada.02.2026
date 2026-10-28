"use client";

import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { apiMode, request } from "@/lib/session";
import { useAuth } from "./auth-provider";
import { demoStorage } from "@/lib/demo-storage";

export type DemoProfile = { name: string; email: string; phone: string; office: string; hideBalance: boolean; reduceMotion: boolean };
export const defaultProfile: DemoProfile = { name: "Fulano de Lima", email: "fulano@example.com", phone: "", office: "Cartório de demonstração", hideBalance: true, reduceMotion: false };
const ProfileContext = createContext<{ profile: DemoProfile; ready: boolean; loadError: boolean; save: (value: DemoProfile) => Promise<void> } | null>(null);
export function ProfileProvider({ children }: { children: ReactNode }) {
  const {authenticated} = useAuth();
  const [profile, setProfile] = useState(defaultProfile);
  const [ready, setReady] = useState(false);
  const [loadError, setLoadError] = useState(false);
  useEffect(() => {
    let active = true;
    if (!authenticated) return;
    (apiMode ? Promise.all([request<DemoProfile>("/profile"), request<Pick<DemoProfile,"hideBalance"|"reduceMotion">>("/profile/preferences")]).then(([data,prefs])=>({...data,...prefs})) : demoStorage.read<Partial<DemoProfile>>("profile")).then((value) => {
      if (!active) return;
      setLoadError(false);
      if (value && typeof value === "object") setProfile({ name: typeof value.name === "string" ? value.name.slice(0, 100) : defaultProfile.name, email: typeof value.email === "string" ? value.email.slice(0, 254) : defaultProfile.email, phone: typeof value.phone === "string" ? value.phone.slice(0, 25) : "", office: typeof value.office === "string" ? value.office.slice(0, 120) : defaultProfile.office, hideBalance: typeof value.hideBalance === "boolean" ? value.hideBalance : true, reduceMotion: value.reduceMotion === true });
    }).catch(() => { if (active) setLoadError(true); }).finally(() => { if (active) setReady(true); });
    return () => { active = false; };
  }, [authenticated]);
  async function save(value: DemoProfile) {
    if (apiMode) {
      await request("/profile", {method:"PUT",body:JSON.stringify({name:value.name,email:value.email,phone:value.phone,office:value.office})});
      try { await request("/profile/preferences", {method:"PUT",body:JSON.stringify({hideBalance:value.hideBalance,reduceMotion:value.reduceMotion})}); }
      catch { throw new Error("Dados pessoais salvos, mas preferências não atualizadas. Tente salvar novamente."); }
    } else await demoStorage.write("profile", value);
    setProfile(value);
  }
  return <ProfileContext.Provider value={{ profile, ready, loadError, save }}><div className={profile.reduceMotion ? "profile-reduced-motion" : undefined}>{children}</div></ProfileContext.Provider>;
}
export function useProfile() { const value = useContext(ProfileContext); if (!value) throw new Error("ProfileProvider não encontrado"); return value; }
