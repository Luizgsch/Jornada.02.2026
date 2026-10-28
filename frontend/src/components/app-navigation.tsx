"use client";

import Link from "next/link";
import { DashboardIcon } from "@/components/dashboard-icon";
import { useNotifications } from "@/components/notifications-provider";
import "./app-navigation.css";

export function AppNavigation({ active }: { active: "home" | "alerts" | "finance" | "documents" | "profile" }) {
  const { unreadCount,ready,error } = useNotifications();
  return <nav className="dashboard-navigation" aria-label="Navegação principal">
    <Link className={active === "home" ? "nav-current" : undefined} aria-current={active === "home" ? "page" : undefined} href="/menu/"><span className="nav-icon"><DashboardIcon name="home" /></span><span>Início</span></Link>
    <Link className={active === "finance" ? "nav-current" : undefined} aria-current={active === "finance" ? "page" : undefined} href="/financeiro/"><span className="nav-icon"><DashboardIcon name="wallet" /></span><span>Financeiro</span></Link>
    <Link className={active === "documents" ? "nav-current" : undefined} aria-current={active === "documents" ? "page" : undefined} href="/documentos/"><span className="nav-icon"><DashboardIcon name="document" /></span><span>Documentos</span></Link>
    <Link className={active === "alerts" ? "nav-current" : undefined} aria-current={active === "alerts" ? "page" : undefined} href="/notificacoes/" aria-label={!ready ? "Alertas, carregando" : error ? "Alertas indisponíveis" : `Alertas, ${unreadCount} ${unreadCount === 1 ? "não lido" : "não lidos"}`}><span className="nav-icon nav-alert"><DashboardIcon name="bell" />{unreadCount > 0 && <span className="notification-dot" />}</span><span>Alertas</span></Link>
    <Link className={active === "profile" ? "nav-current" : undefined} aria-current={active === "profile" ? "page" : undefined} href="/perfil/"><span className="nav-icon"><DashboardIcon name="user" /></span><span>Perfil</span></Link>
  </nav>;
}
