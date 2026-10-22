"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { DashboardIcon, type DashboardIconName } from "@/components/dashboard-icon";
import { useProfile } from "@/components/profile-provider";
import { AppNavigation } from "@/components/app-navigation";
import { useNotifications } from "@/components/notifications-provider";
import { formatMoney, listMovements, type Movement } from "@/lib/movements";

const indicators: { label: string; value: string; icon: DashboardIconName }[] = [
  { label: "A pagar", value: "R$ 12.840", icon: "down" },
  { label: "A receber", value: "R$ 26.500", icon: "up" },
  { label: "Movimentado", value: "R$ 82.340", icon: "exchange" },
  { label: "Pendências", value: "6 itens", icon: "clock" },
];

export function DashboardScreen() {
  const { unreadCount } = useNotifications();
  const { profile } = useProfile();
  const [balanceOverride, setBalanceOverride] = useState<boolean | null>(null);
  const balanceVisible = balanceOverride ?? !profile.hideBalance;
  const initials = profile.name.trim().split(/\s+/).filter(Boolean).map((part) => part[0]).filter((_, index, all) => index === 0 || index === all.length - 1).join("").toLocaleUpperCase("pt-BR");
  const [notice, setNotice] = useState("");
  const [movements, setMovements] = useState<Movement[]>([]);
  const [movementsError, setMovementsError] = useState(false);
  const [showAll, setShowAll] = useState(false);
  useEffect(() => {
    let active = true;
    void listMovements().then((items) => { if (active) setMovements(items); }).catch(() => { if (active) setMovementsError(true); });
    return () => { active = false; };
  }, []);
  const pending = (name: string) => setNotice(`${name} ainda não está disponível nesta demonstração.`);

  return (
    <main className="dashboard-shell">
      <header className="dashboard-header">
        <div className="dashboard-greeting"><p>Quarta-feira, 30 de Setembro</p><h1>Olá, {profile.name.trim().split(/\s+/)[0] || "Fulano"}</h1></div>
        <div className="dashboard-actions">
          <Link className="dashboard-icon-button" aria-label="Pesquisar" href="/pesquisa/"><DashboardIcon name="search" /></Link>
          <Link className="dashboard-icon-button notification-button" href="/notificacoes/" aria-label={`Notificações, ${unreadCount} ${unreadCount === 1 ? "não lida" : "não lidas"}`}><DashboardIcon name="bell" />{unreadCount > 0 && <span className="notification-dot" />}</Link>
          <Link className="dashboard-avatar" aria-label="Abrir meu perfil" href="/perfil/"><span aria-hidden="true">{initials || "FL"}</span></Link>
        </div>
      </header>
      <section className="dashboard-balance" aria-label="Saldo do cartório">
        <span className="balance-caption">Saldo disponível</span>
        <button className="balance-visibility" aria-label={balanceVisible ? "Ocultar saldo" : "Mostrar saldo"} aria-pressed={balanceVisible} onClick={() => setBalanceOverride(!balanceVisible)}><DashboardIcon name={balanceVisible ? "hidden" : "eye"} /></button>
        <p className="balance-value">{balanceVisible ? "Não informado" : "Ver saldo do cartório"}</p>
      </section>
      <section className="dashboard-indicators" aria-label="Indicadores financeiros demonstrativos">
        {indicators.map((item) => <button className="indicator-card" key={item.label} onClick={() => pending(`O detalhamento “${item.label}”`)}><span className="indicator-heading"><span>{item.label}</span><span className="indicator-symbol"><DashboardIcon name={item.icon} /></span></span><strong>{item.value}</strong></button>)}
      </section>
      <section className="financial-summary" aria-labelledby="financial-title">
        <div className="dashboard-section-heading"><h2 id="financial-title">Resumo financeiro</h2><span className="summary-period">Últimos 6 meses</span></div>
        <div className="summary-chart" role="img" aria-label="Resumo financeiro de janeiro a junho. Séries ainda não disponíveis nesta demonstração.">
          <div className="chart-legend"><span><i className="legend-entry" />Entradas</span><span><i className="legend-exit" />Saídas</span></div>
          <div className="chart-months">{["Jan", "Fev", "Mar", "Abr", "Mai", "Jun"].map((month) => <span key={month}>{month}</span>)}</div>
        </div>
      </section>
      <section className="recent-movements" aria-labelledby="recent-title">
        <div className="dashboard-section-heading"><h2 id="recent-title">Últimas movimentações</h2><button className="dashboard-text-button" aria-expanded={showAll} onClick={() => setShowAll((value) => !value)}>{showAll ? "Ver menos" : "Ver todas"}</button></div>
        {movements.length ? <ul className="recent-movement-list">{(showAll ? movements : movements.slice(0, 3)).map((item) => <li key={item.id}><div><strong>{item.description}</strong><span>{item.date.split("-").reverse().join("/")} · {item.type === "entrada" ? "Entrada" : "Saída"}</span>{item.attachment && <span>Anexo: {item.attachment.name}</span>}</div><b>{item.type === "entrada" ? "+" : "−"} {formatMoney(item.valueInCents)}</b></li>)}</ul> : <p className="movements-empty">{movementsError ? "Não foi possível consultar as movimentações deste dispositivo." : "Nenhuma movimentação cadastrada nesta demonstração."}</p>}
      </section>
      <p className="dashboard-demo-label">Demonstração acadêmica · Valores fictícios do protótipo</p>
      <Link className="new-movement-button" href="/movimentacao/"><DashboardIcon name="plus" />Nova movimentação</Link>
      <AppNavigation active="home" />
      {notice && <p className="dashboard-notice" role="status">{notice}<button onClick={() => setNotice("")} aria-label="Fechar aviso">Fechar</button></p>}
    </main>
  );
}
