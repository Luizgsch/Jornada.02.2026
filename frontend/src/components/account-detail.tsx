"use client";
import { useEffect,useState } from "react";
import Link from "next/link";
import { apiMode, request } from "@/lib/session";
import { demoPayables, payableStatus, shortDate, type Payable } from "@/lib/financial";
import { formatMoney } from "@/lib/movements";
export function AccountDetail(){
 const [item,setItem]=useState<Payable|null>(null);const [error,setError]=useState("");const [attempt,setAttempt]=useState(0);
 useEffect(()=>{let active=true;const id=window.location.hash.slice(1)||"aluguel";const load=apiMode?request<Payable & {party:string;valueInCents:number}>(`/finance/accounts/${encodeURIComponent(id)}`).then(x=>({...x,supplier:x.party,cents:x.valueInCents})):Promise.resolve(demoPayables.find(x=>x.id===id)??null);load.then(x=>{if(active){setItem(x);if(!x)setError("Conta não encontrada.");}}).catch(reason=>{if(active)setError(reason instanceof Error?reason.message:"Falha na consulta.");});return()=>{active=false;};},[attempt]);
 return <main className="finance-shell flow-shell"><Link className="flow-back" href="/notificacoes/">Voltar às notificações</Link><h1>Conta a pagar</h1><p className="finance-demo">Demonstração · referência 22/10/2026</p>{error?<div role="alert"><p>{error}</p><button onClick={()=>{setError("");setAttempt(v=>v+1);}}>Tentar novamente</button></div>:!item?<p role="status">Carregando conta…</p>:<article className="flow-card"><h2>{item.description}</h2><p className="flow-value">{formatMoney(item.cents)}</p><dl className="finance-detail"><div><dt>Fornecedor</dt><dd>{item.supplier}</dd></div><div><dt>Vencimento</dt><dd>{shortDate(item.dueDate)}</dd></div><div><dt>Situação</dt><dd>{payableStatus(item)}</dd></div><div><dt>Categoria</dt><dd>{item.category}</dd></div></dl></article>}<p className="finance-footnote">Somente consulta. Nenhum pagamento será realizado.</p><Link className="finance-primary" href="/financeiro/">Consultar outras contas</Link></main>;
}
