"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { DashboardIcon } from "@/components/dashboard-icon";
import { demoPayables } from "@/lib/financial";
import { demoNotifications } from "@/lib/notifications";
import { formatMoney, listMovements, type Movement } from "@/lib/movements";

type SearchItem = { id: string; title: string; description: string; category: string; href: string };
const normalize = (value: string) => value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLocaleLowerCase("pt-BR").trim();
export function SearchScreen() {
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("Todos");
  const [movements, setMovements] = useState<Movement[]>([]);
  const [loading, setLoading] = useState(true);
  const [failed, setFailed] = useState(false);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    let active = true;
    listMovements().then((items) => { if (active) { setMovements(items); setLoading(false); } }).catch(() => { if (active) { setFailed(true); setLoading(false); } });
    return () => { active = false; };
  }, [attempt]);
  const items: SearchItem[] = [
    ...demoPayables.map((item) => ({ id: `conta-${item.id}`, title: item.description, description: `${item.supplier} · ${formatMoney(item.cents)} · ${item.dueDate.split("-").reverse().join("/")} · Exemplo`, category: "Contas", href: `/financeiro/#conta-${item.id}` })),
    ...movements.map((item) => ({ id: item.id, title: item.description, description: `${formatMoney(item.valueInCents)} · ${item.category} · Registro local`, category: "Movimentações", href: `/financeiro/movimentacoes/#mov-${item.id}` })),
    ...movements.flatMap((item) => item.attachment ? [{ id: `anexo-${item.id}`, title: item.attachment.name, description: `${item.description} · Arquivo local`, category: "Documentos", href: `/documentos/#doc-${item.id}` }] : []),
    ...demoNotifications.map((item) => ({ id: `aviso-${item.id}`, title: item.title, description: `${item.description} · Exemplo do Figma`, category: "Alertas", href: item.id === "aluguel" ? "/notificacoes/detalhe/" : "/notificacoes/" })),
  ];
  const term = normalize(query);
  const results = term ? items.filter((item) => (category === "Todos" || category === item.category) && normalize(`${item.title} ${item.description}`).includes(term)) : [];
  return <main className="finance-shell flow-shell"><Link className="flow-back" href="/menu/"><DashboardIcon name="back" />Voltar ao início</Link><h1>Pesquisa</h1><p className="finance-context">Encontre contas, movimentações, anexos e alertas.</p><label className="finance-search"><DashboardIcon name="search" /><input type="search" aria-label="Pesquisar no aplicativo" placeholder="O que você procura?" maxLength={160} value={query} onChange={(event) => setQuery(event.target.value)} /></label><div className="flow-search-filters" role="group" aria-label="Categoria da pesquisa">{["Todos", "Contas", "Movimentações", "Documentos", "Alertas"].map((item) => <button key={item} aria-pressed={category === item} onClick={() => setCategory(item)}>{item}</button>)}</div>{loading && <p role="status" className="finance-context">Carregando registros locais…</p>}{failed && <div role="alert" className="flow-card"><p>Não foi possível consultar os registros locais. Resultados de exemplo continuam disponíveis.</p><button className="finance-secondary" onClick={() => { setFailed(false); setLoading(true); setAttempt((value) => value + 1); }}>Tentar novamente</button></div>}<p className="finance-count" role="status">{!term ? "Digite um nome, descrição ou valor para pesquisar." : `${results.length} ${results.length === 1 ? "resultado encontrado" : "resultados encontrados"}`}</p><ul className="flow-results">{results.map((item) => <li key={item.id}><span className="flow-result-kind">{item.category}</span><h2>{item.title}</h2><p>{item.description}</p><Link href={item.href}>Abrir {item.category === "Contas" || item.category === "Movimentações" ? "Financeiro" : item.category === "Alertas" ? "alerta" : "Documentos"}<span className="flow-sr-only">: {item.title}</span></Link></li>)}</ul>{term && !results.length && !loading && <div className="finance-empty"><h2>Nenhum resultado</h2><p>Tente outra palavra ou selecione Todos.</p></div>}<p className="finance-footnote">Pesquisa demonstrativa. Contas e alertas são exemplos; movimentações e anexos vêm deste dispositivo.</p></main>;
}
