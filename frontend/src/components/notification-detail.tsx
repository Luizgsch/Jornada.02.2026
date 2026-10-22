"use client";

import { useEffect } from "react";
import Link from "next/link";
import { DashboardIcon } from "@/components/dashboard-icon";
import { useNotifications } from "@/components/notifications-provider";

export function NotificationDetail() {
  const { markRead } = useNotifications();
  useEffect(() => { markRead("aluguel"); }, [markRead]);
  return <main className="notifications-shell notification-detail-shell">
    <header className="detail-heading"><Link href="/notificacoes/" aria-label="Voltar para notificações"><DashboardIcon name="back" /></Link><h1>Notificação</h1></header>
    <article className="notification-detail-card">
      <span className="detail-symbol"><DashboardIcon name="bell" /></span>
      <span className="detail-category">Financeira</span>
      <h2>Uma conta vence amanhã</h2>
      <p>A conta “Aluguel do escritório”, no valor de R$ 4.200,00, vence amanhã, 15 de junho de 2025.</p>
      <p className="detail-received">Recebida hoje às 09:42</p>
    </article>
    <Link className="detail-view-account" href="/financeiro/conta/">Ver conta</Link>
  </main>;
}
