"use client";

import { createContext, useContext, useEffect, useState, useCallback, type ReactNode } from "react";
import { demoStorage } from "@/lib/demo-storage";
import { demoNotifications } from "@/lib/notifications";

const Context = createContext<{ unreadCount: number; isUnread: (id: string) => boolean; markRead: (id: string) => void } | null>(null);

export function NotificationsProvider({ children }: { children: ReactNode }) {
  const [readIds, setReadIds] = useState<string[]>([]);
  const [ready, setReady] = useState(false);
  useEffect(() => {
    let active = true;
    void demoStorage.read<unknown>("read-notifications").then((saved) => {
      if (!active) return;
      if (Array.isArray(saved)) {
        const validIds = saved.filter((id): id is string => typeof id === "string" && demoNotifications.some((item) => item.id === id));
        setReadIds((previous) => [...new Set([...previous, ...validIds])]);
      }
      setReady(true);
    }).catch(() => { if (active) setReady(true); });
    return () => { active = false; };
  }, []);
  useEffect(() => {
    if (ready) void demoStorage.write("read-notifications", readIds).catch(() => { /* Leitura continua em memoria se armazenamento estiver indisponivel. */ });
  }, [ready, readIds]);
  const isUnread = useCallback((id: string) => Boolean(demoNotifications.find((item) => item.id === id)?.unread && !readIds.includes(id)), [readIds]);
  const markRead = useCallback((id: string) => {
    setReadIds((previous) => previous.includes(id) ? previous : [...previous, id]);
  }, []);
  const unreadCount = demoNotifications.filter((item) => isUnread(item.id)).length;
  return <Context.Provider value={{ unreadCount, isUnread, markRead }}>{children}</Context.Provider>;
}

export function useNotifications() {
  const context = useContext(Context);
  if (!context) throw new Error("NotificationsProvider ausente");
  return context;
}
