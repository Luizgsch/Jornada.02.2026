"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { DashboardIcon, type DashboardIconName } from "@/components/dashboard-icon";
import { useProfile } from "@/components/profile-provider";
import { AppNavigation } from "@/components/app-navigation";
import { useNotifications } from "@/components/notifications-provider";
import { apiMode, request } from "@/lib/session";
import { dashboardSummary } from "@/lib/dashboard-summary";
import { formatMoney, listMovements, type Movement } from "@/lib/movements";

export function DashboardScreen() {
  const { unreadCount,ready:alertsReady,error:alertsError } = useNotifications();
  const { profile } = useProfile();
  const [balanceOverride, setBalanceOverride] = useState<boolean | null>(null);
  const balanceVisible = balanceOverride ?? !profile.hideBalance;
  const initials = profile.name.trim().split(/\s+/).filter(Boolean).map((part) => part[0]).filter((_, index, all) => index === 0 || index === all.length - 1).join("").toLocaleUpperCase("pt-BR");
  const [remoteSummary,setRemoteSummary] = useState<ReturnType<typeof dashboardSummary> | null>(null);
  const [loaded, setLoaded] = useState(false);
  const [attempt, setAttempt] = useState(0);
  const [movements, setMovements] = useState<Movement[]>([]);
  const [movementsError, setMovementsError] = useState(false);
  const [showAll, setShowAll] = useState(false);
  useEffect(() => {
    let active = true;
    void Promise.all([listMovements(), apiMode ? request<{payableOpenCents:number;receivableOpenCents:number;pendingCount:number;movedCents:number;balanceCents:number;chart:{month:string;incomingCents:number;outgoingCents:number}[]}>("/finance/summary?month=2026-10") : Promise.resolve(null)]).then(([items,data]) => { if (active) { setMovements(items); if(data) setRemoteSummary({payable:data.payableOpenCents,receivable:data.receivableOpenCents,pendingCount:data.pendingCount,moved:data.movedCents,balance:data.balanceCents,months:data.chart.map(m=>({key:m.month,label:new Date(`${m.month}-01T12:00:00Z`).toLocaleDateString("pt-BR",{month:"short",timeZone:"UTC"}).replace(".",""),entries:m.incomingCents,exits:m.outgoingCents}))}); setLoaded(true); setMovementsError(false); } }).catch(() => { if (active) { setMovementsError(true); setLoaded(true); } });
    return () => { active = false; };
  }, [attempt]);
  const summary = remoteSummary ?? dashboardSummary(movements);
  const localValue = (cents: number) => !loaded ? "Carregando…" : movementsError ? "Indisponível" : formatMoney(cents);
  const indicators: { label: string; value: string; icon: DashboardIconName; href: string }[] = [
    { label: "A pagar", value: localValue(summary.payable), icon: "down", href: "/financeiro/pendencias/" },
    { label: "A receber", value: localValue(summary.receivable), icon: "up", href: "/financeiro/receber/" },
    { label: "Movimentado", value: localValue(summary.moved), icon: "exchange", href: "/financeiro/movimentacoes/" },
    { label: "Pendências", value: !loaded ? "Carregando…" : movementsError ? "Indisponível" : `${summary.pendingCount} contas`, icon: "clock", href: "/financeiro/pendencias/" },
  ];
  const chartMaximum = Math.max(1, ...summary.months.flatMap((month) => [month.entries, month.exits]));

  return (
    <main className="dashboard-shell">
      <header className="dashboard-header">
        <div className="dashboard-greeting"><p>Outubro de 2026 · Demonstração</p><h1>Olá, {profile.name.trim().split(/\s+/)[0] || "Fulano"}</h1></div>
        <div className="dashboard-actions">
          <Link className="dashboard-icon-button" aria-label="Pesquisar" href="/pesquisa/"><DashboardIcon name="search" /></Link>
          <Link className="dashboard-icon-button notification-button" href="/notificacoes/" aria-label={!alertsReady ? "Notificações, carregando" : alertsError ? "Notificações indisponíveis" : `Notificações, ${unreadCount} ${unreadCount === 1 ? "não lida" : "não lidas"}`}><DashboardIcon name="bell" />{unreadCount > 0 && <span className="notification-dot" />}</Link>
          <Link className="dashboard-avatar" aria-label="Abrir meu perfil" href="/perfil/"><span aria-hidden="true">{initials || "FL"}</span></Link>
        </div>
      </header>
      <section className="dashboard-balance" aria-label="Saldo do cartório">
        <span className="balance-caption">Saldo demonstrativo</span>
        <button className="balance-visibility" aria-label={balanceVisible ? "Ocultar saldo" : "Mostrar saldo"} aria-pressed={balanceVisible} onClick={() => setBalanceOverride(!balanceVisible)}><DashboardIcon name={balanceVisible ? "hidden" : "eye"} /></button>
        <p className="balance-value">{balanceVisible ? localValue(summary.balance) : "Ver saldo do cartório"}</p>
        <p className="balance-explanation">Saldo inicial R$ 0,00 + entradas − saídas de todos os registros demonstrativos. Não é saldo bancário.</p>
      </section>
      <section className="dashboard-indicators" aria-label="Indicadores financeiros demonstrativos">
        {indicators.map((item) => <Link className="indicator-card" key={item.label} href={item.href}><span className="indicator-heading"><span>{item.label}</span><span className="indicator-symbol"><DashboardIcon name={item.icon} /></span></span><strong>{item.value}</strong></Link>)}
      </section>
      <section className="financial-summary" aria-labelledby="financial-title">
        <div className="dashboard-section-heading"><h2 id="financial-title">Resumo financeiro</h2><span className="summary-period">Últimos 6 meses</span></div>
        <figure className="summary-chart" aria-labelledby="chart-caption">
          <figcaption id="chart-caption" className="chart-caption">Movimentações demonstrativas · Maio a outubro de 2026</figcaption>
          <div className="chart-legend"><span><i className="legend-entry" />Entradas</span><span><i className="legend-exit" />Saídas</span></div>
          {!loaded ? <p role="status">Carregando gráfico…</p> : movementsError ? <p>Não foi possível carregar o gráfico.</p> : <>
            <div className="chart-bars" aria-hidden="true">{summary.months.map((month) => <div className="chart-month" key={month.key}><div className="chart-pair"><i className="chart-bar chart-entry" style={{ height: `${month.entries / chartMaximum * 100}%` }} /><i className="chart-bar chart-exit" style={{ height: `${month.exits / chartMaximum * 100}%` }} /></div><span>{month.label}</span></div>)}</div>
            {!summary.months.some((month) => month.entries || month.exits) && <p className="chart-caption">Sem movimentações nestes seis meses.</p>}
          </>}
        </figure>
        {loaded && !movementsError && <details className="chart-data"><summary>Ver valores do gráfico</summary><table><caption>Entradas e saídas demonstrativas por mês, em reais</caption><thead><tr><th scope="col">Mês</th><th scope="col">Entradas</th><th scope="col">Saídas</th></tr></thead><tbody>{summary.months.map((month) => <tr key={month.key}><th scope="row">{month.label}</th><td>{formatMoney(month.entries)}</td><td>{formatMoney(month.exits)}</td></tr>)}</tbody></table></details>}
        {movementsError && <button className="dashboard-text-button" onClick={() => { setLoaded(false); setMovementsError(false); setAttempt((value) => value + 1); }}>Tentar carregar novamente</button>}
      </section>
      <section className="recent-movements" aria-labelledby="recent-title">
        <div className="dashboard-section-heading"><h2 id="recent-title">Últimas movimentações</h2><button className="dashboard-text-button" aria-expanded={showAll} onClick={() => setShowAll((value) => !value)}>{showAll ? "Ver menos" : "Ver todas"}</button></div>
        {!loaded ? <p className="movements-empty" role="status">Carregando movimentações…</p> : movementsError ? <p className="movements-empty">Não foi possível consultar as movimentações.</p> : movements.length ? <ul className="recent-movement-list">{(showAll ? movements : movements.slice(0, 3)).map((item) => <li key={item.id}><div><strong>{item.description}</strong><span>{item.date.split("-").reverse().join("/")} · {item.type === "entrada" ? "Entrada" : "Saída"}</span>{item.attachment && <span>Anexo: {item.attachment.name}</span>}</div><b>{item.type === "entrada" ? "+" : "−"} {formatMoney(item.valueInCents)}</b></li>)}</ul> : <p className="movements-empty">Nenhuma movimentação cadastrada nesta demonstração.</p>}
      </section>
      <p className="dashboard-demo-label">Contas a pagar/receber: exemplos de outubro de 2026. Movimentado: entradas + saídas demonstrativas do mês. Pendências: contas não pagas do mês.</p>
      <Link className="new-movement-button" href="/movimentacao/"><DashboardIcon name="plus" />Nova movimentação</Link>
      <AppNavigation active="home" />
    </main>
  );
}
