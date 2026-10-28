"use client";

import { useState } from "react";
import Link from "next/link";
import { AppNavigation } from "@/components/app-navigation";
import { DashboardIcon } from "@/components/dashboard-icon";
import { useNotifications } from "@/components/notifications-provider";
import { type NotificationCategory } from "@/lib/notifications";

export function NotificationsScreen() {
  const [filter, setFilter] = useState<"todas" | NotificationCategory>("todas");
  const [notice, setNotice] = useState("");
  const { items:demoNotifications, unreadCount, isUnread, ready, error, reload, markAllRead } = useNotifications();
  const filters = [{ id: "todas", label: "Todas" }, { id: "financeira", label: "Financeiras" }, { id: "sistema", label: "Sistema" }] as const;
  return <main className="notifications-shell">
    <header className="notifications-heading"><h1>Notificações</h1><p aria-live="polite">{!ready ? "Carregando alertas…" : error ? "Alertas indisponíveis" : unreadCount === 1 ? "Você tem 1 alerta não lido" : `Você tem ${unreadCount} alertas não lidos`}</p></header>
    <div className="notification-filters" role="group" aria-label="Filtrar notificações">{filters.map((item) => <button key={item.id} aria-pressed={filter === item.id} onClick={() => setFilter(item.id)}>{item.label}</button>)}</div>
    {!ready && <p role="status">Carregando notificações…</p>}{error && <div role="alert"><p>{error}</p><button onClick={reload}>Tentar novamente</button></div>}<button className="finance-secondary" disabled={!ready || Boolean(error) || !unreadCount} onClick={()=>{void markAllRead().catch(reason=>setNotice(reason instanceof Error?reason.message:"Falha na leitura."));}}>Marcar todas como lidas</button><ul className="notification-list">
      {demoNotifications.filter((item) => filter === "todas" || item.category === filter).map((item) => {
        const unread = isUnread(item.id);
        const content = <><span className={`notification-symbol ${unread ? "unread-symbol" : ""}`}><DashboardIcon name={item.category === "financeira" ? "bell" : item.id === "documento" ? "document" : "check"} />{unread && <span className="notification-dot" />}</span><span className="notification-copy"><span className="notification-title-row"><strong>{item.title}</strong><span className="notification-time">{item.time}</span></span><span className="notification-description">{item.description}</span><span className="notification-category">{item.category === "financeira" ? "Financeira" : "Sistema"}</span></span></>;
        return <li key={item.id} className={`notification-card ${unread ? "is-unread" : ""}`}>{<Link href={`/notificacoes/detalhe/#${item.id}`} aria-label={`${item.title}${unread ? ", não lida" : ""}`}>{content}</Link>}</li>;
      })}
    </ul>
    <AppNavigation active="alerts" />
    {notice && <div className="notifications-notice" role="status">{notice}<button onClick={() => setNotice("")} aria-label="Fechar aviso">Fechar</button></div>}
  </main>;
}
