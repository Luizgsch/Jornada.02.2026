"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { AppNavigation } from "@/components/app-navigation";
import { DashboardIcon } from "@/components/dashboard-icon";
import { formatMoney, listMovements, type Movement } from "@/lib/movements";
import { demoBudget, demoFinancialMonth, demoPayables, demoRevenue, financialReferenceDate, payableStatus, shortDate } from "@/lib/financial";

const sections = ["Contas a pagar", "Movimentações", "Receitas", "Orçamento"] as const;
type Section = typeof sections[number];
export function FinancialScreen({ initialSection = "Contas a pagar" }: { initialSection?: Section }) {
  const [section, setSection] = useState<Section>(initialSection);
  const [month, setMonth] = useState(demoFinancialMonth);
  const [status, setStatus] = useState("Todos");
  const [search, setSearch] = useState("");
  const [type, setType] = useState("todos");
  const [sector, setSector] = useState("Todos");
  const [group, setGroup] = useState("setor");
  const [movements, setMovements] = useState<Movement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    let live = true;
    listMovements().then((items) => { if (live) { setMovements(items); const requested = window.location.hash.slice(1); const match = items.find((item) => `mov-${item.id}` === requested); if (match) setMonth(match.date.slice(0, 7)); setLoading(false); setError(""); } }).catch(() => { if (live) { setLoading(false); setError("Não foi possível carregar as movimentações deste dispositivo."); } });
    return () => { live = false; };
  }, [attempt]);
  const payables = demoPayables.filter((item) => item.dueDate.startsWith(month));
  const bills = payables.filter((item) => (status === "Todos" || payableStatus(item) === status) && `${item.description} ${item.supplier}`.toLocaleLowerCase("pt-BR").includes(search.toLocaleLowerCase("pt-BR")));
  const localMovements = movements.filter((item) => item.date.startsWith(month));
  const entries = localMovements.filter((item) => item.type === "entrada").reduce((sum, item) => sum + item.valueInCents, 0);
  const exits = localMovements.filter((item) => item.type === "saida").reduce((sum, item) => sum + item.valueInCents, 0);
  const filteredMovements = localMovements.filter((item) => (type === "todos" || item.type === type) && `${item.description} ${item.category}`.toLocaleLowerCase("pt-BR").includes(search.toLocaleLowerCase("pt-BR"))).sort((a, b) => b.date.localeCompare(a.date) || b.createdAt.localeCompare(a.createdAt));
  const revenue = month === demoFinancialMonth ? demoRevenue.filter((item) => sector === "Todos" || item.sector === sector) : [];
  const grouped = Object.entries(revenue.reduce<Record<string, number>>((result, item) => { const key = group === "setor" ? item.sector : item.employee; result[key] = (result[key] ?? 0) + item.cents; return result; }, {}));
  const budget = month === demoFinancialMonth ? demoBudget : [];
  const planned = budget.reduce((sum, item) => sum + item.planned, 0);
  const actual = budget.reduce((sum, item) => sum + item.actual, 0);
  const selectSection = (next: Section) => { setSection(next); setSearch(""); };
  return <main className="finance-shell">
    <header className="finance-header"><div><h1>Financeiro</h1><p>Acompanhe as finanças do cartório</p></div><Link href="/notificacoes/" className="finance-bell" aria-label="Abrir notificações"><DashboardIcon name="bell" /></Link></header>
    <p className="finance-demo">Demonstração · dados fictícios</p>
    <form key={month} className="finance-period" onSubmit={(event) => { event.preventDefault(); const value = String(new FormData(event.currentTarget).get("month") ?? ""); if (/^\d{4}-(0[1-9]|1[0-2])$/.test(value)) setMonth(value); }}><label className="finance-month">Período<input name="month" aria-label="Período financeiro" type="month" min="1900-01" max="9999-12" defaultValue={month} required /></label><button type="submit">Aplicar</button></form>
    <div className="finance-sections" role="group" aria-label="Área financeira">{sections.map((item) => <button key={item} aria-pressed={section === item} onClick={() => selectSection(item)}>{item}</button>)}</div>
    <section aria-labelledby="finance-section-title" className="finance-content">
      <h2 id="finance-section-title">{section}</h2>
      {section === "Contas a pagar" && <>
        <p className="finance-context">Consulta de compromissos. Posição de exemplo em {shortDate(financialReferenceDate)}.</p>
        <dl className="finance-totals"><div><dt>Total em aberto</dt><dd>{formatMoney(payables.filter((item) => !item.paidDate).reduce((sum, item) => sum + item.cents, 0))}</dd></div><div><dt>Vencido</dt><dd>{formatMoney(payables.filter((item) => payableStatus(item) === "Vencido").reduce((sum, item) => sum + item.cents, 0))}</dd></div></dl>
        <label className="finance-search"><DashboardIcon name="search" /><input aria-label="Buscar conta ou fornecedor" placeholder="Buscar conta ou fornecedor" value={search} onChange={(event) => setSearch(event.target.value)} /></label>
        <div className="finance-status" role="group" aria-label="Situação da conta">{["Todos", "Em aberto", "Vencido", "Pago"].map((item) => <button key={item} aria-pressed={status === item} onClick={() => setStatus(item)}>{item}</button>)}</div>
        <p className="finance-count" role="status">{bills.length} {bills.length === 1 ? "conta encontrada" : "contas encontradas"}</p>
        <div className="finance-list">{bills.map((item) => <details id={`conta-${item.id}`} className="finance-bill" key={item.id}><summary><span className="finance-row"><strong>{item.description}</strong><b>{formatMoney(item.cents)}</b></span><span className="finance-row finance-bill-meta"><span>Vence em {shortDate(item.dueDate)}</span><span className={`finance-badge ${payableStatus(item) === "Vencido" ? "overdue" : ""}`}>{payableStatus(item)}</span></span><span className="finance-detail-hint">Ver detalhes</span></summary><dl className="finance-detail"><div><dt>Fornecedor</dt><dd>{item.supplier}</dd></div><div><dt>Categoria</dt><dd>{item.category}</dd></div><div><dt>Conta</dt><dd>{item.account}</dd></div>{item.paidDate && <div><dt>Pagamento de exemplo</dt><dd>{shortDate(item.paidDate)}</dd></div>}</dl><p className="finance-context">Somente consulta. Nenhum pagamento é realizado pelo aplicativo.</p></details>)}</div>
        {!bills.length && <Empty title="Nenhuma conta encontrada" text="Altere o período, a busca ou a situação. Exemplos disponíveis em outubro de 2026." />}
      </>}
      {section === "Movimentações" && <>
        <p className="finance-context">Registros salvos neste dispositivo. Não incluem os exemplos das outras abas.</p>
        {!loading && !error && <><dl className="finance-totals"><div><dt>Entradas no período</dt><dd>{formatMoney(entries)}</dd></div><div><dt>Saídas no período</dt><dd>{formatMoney(exits)}</dd></div></dl><p className="finance-net">Resultado do período <strong>{formatMoney(entries - exits)}</strong></p><p className="finance-context">Entradas menos saídas; não representa saldo bancário.</p></>}
        <Link className="finance-primary" href="/movimentacao/"><DashboardIcon name="plus" />Nova movimentação</Link>
        <label className="finance-search"><DashboardIcon name="search" /><input aria-label="Buscar movimentação" placeholder="Buscar descrição ou categoria" value={search} onChange={(event) => setSearch(event.target.value)} /></label>
        <div className="finance-status" role="group" aria-label="Tipo de movimentação">{[{ id: "todos", label: "Todas" }, { id: "entrada", label: "Entradas" }, { id: "saida", label: "Saídas" }].map((item) => <button key={item.id} aria-pressed={type === item.id} onClick={() => setType(item.id)}>{item.label}</button>)}</div>
        {loading ? <p role="status" className="finance-context">Carregando movimentações…</p> : error ? <div role="alert" className="finance-empty"><p>{error}</p><button className="finance-secondary" onClick={() => { setLoading(true); setError(""); setAttempt((value) => value + 1); }}>Tentar novamente</button></div> : <><p className="finance-count" role="status">{filteredMovements.length} {filteredMovements.length === 1 ? "registro encontrado" : "registros encontrados"}</p><div className="finance-list">{filteredMovements.map((item) => <details id={`mov-${item.id}`} key={item.id} className="finance-bill"><summary><span className="finance-row"><strong>{item.description}</strong><b>{item.type === "entrada" ? "+" : "−"} {formatMoney(item.valueInCents)}</b></span><span className="finance-bill-meta">{shortDate(item.date)} · {item.type === "entrada" ? "Entrada" : "Saída"}</span><span className="finance-detail-hint">Ver detalhes</span></summary><dl className="finance-detail"><div><dt>Categoria</dt><dd>{item.category}</dd></div><div><dt>Conta</dt><dd>{item.account}</dd></div>{item.notes && <div><dt>Observação</dt><dd>{item.notes}</dd></div>}{item.attachment && <div><dt>Anexo local</dt><dd>{item.attachment.name}</dd></div>}</dl></details>)}</div>{!filteredMovements.length && <Empty title="Nenhuma movimentação encontrada" text="Registre uma movimentação de demonstração ou ajuste os filtros." />}</>}
      </>}
      {section === "Receitas" && <>
        <p className="finance-context">Consulta por setor e funcionário. Nomes e valores de exemplo.</p>
        <label className="finance-select">Setor<select value={sector} onChange={(event) => setSector(event.target.value)}><option>Todos</option><option>Notas</option><option>Registro</option></select></label>
        <div className="finance-status" role="group" aria-label="Agrupar receitas">{[{ id: "setor", label: "Por setor" }, { id: "funcionario", label: "Por funcionário" }].map((item) => <button key={item.id} aria-pressed={group === item.id} onClick={() => setGroup(item.id)}>{item.label}</button>)}</div>
        <p className="finance-net">Total de receitas <strong>{formatMoney(revenue.reduce((sum, item) => sum + item.cents, 0))}</strong></p>
        <ul className="finance-revenue">{grouped.map(([name, cents]) => <li key={name}><span>{name}</span><strong>{formatMoney(cents)}</strong></li>)}</ul>
        {!grouped.length && <Empty title="Sem receitas neste período" text="Os exemplos estão disponíveis em outubro de 2026." />}
        <p className="finance-context">Visibilidade por perfil e origem das receitas serão definidas na integração.</p>
      </>}
      {section === "Orçamento" && <>
        <p className="finance-context">Despesas previstas e realizadas por categoria. Valores de exemplo do mês selecionado.</p>
        <dl className="finance-totals"><div><dt>Previsto</dt><dd>{formatMoney(planned)}</dd></div><div><dt>Realizado</dt><dd>{formatMoney(actual)}</dd></div></dl>
        <p className="finance-net">{actual > planned ? "Acima do previsto" : "Disponível no orçamento"}<strong>{formatMoney(Math.abs(planned - actual))}</strong></p>
        <ul className="finance-budget">{budget.map((item) => <li key={item.category}><h3>{item.category}</h3><dl className="finance-detail"><div><dt>Previsto</dt><dd>{formatMoney(item.planned)}</dd></div><div><dt>Realizado</dt><dd>{formatMoney(item.actual)}</dd></div></dl><p className={item.actual > item.planned ? "finance-over" : ""}>{item.actual > item.planned ? "Acima do previsto" : "Abaixo do previsto"}: <strong>{formatMoney(Math.abs(item.planned - item.actual))}</strong></p></li>)}</ul>
        {!budget.length && <Empty title="Sem orçamento neste período" text="Os exemplos estão disponíveis em outubro de 2026." />}
      </>}
    </section>
    <p className="finance-footnote">Contas, receitas e orçamento são exemplos de consulta. A integração com o Cash precisa ser validada com a Escriba.</p>
    <AppNavigation active="finance" />
    {notice && <div role="status" className="finance-notice">{notice}<button onClick={() => setNotice("")}>Fechar</button></div>}
  </main>;
}
function Empty({ title, text }: { title: string; text: string }) { return <div className="finance-empty"><DashboardIcon name="wallet" /><h3>{title}</h3><p>{text}</p></div>; }
