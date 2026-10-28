"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import { DashboardIcon } from "./dashboard-icon";
import { useNotifications } from "./notifications-provider";
export function NotificationDetail(){
  const {items,ready,error,markRead}=useNotifications();
  const [id,setId]=useState("aluguel");
  const [notice,setNotice]=useState("");
  useEffect(()=>{const readHash=()=>setId(window.location.hash.slice(1)||"aluguel");readHash();window.addEventListener("hashchange",readHash);return()=>window.removeEventListener("hashchange",readHash);},[]);
  const item=items.find(x=>x.id===id);
  return <main className="notifications-shell notification-detail-shell"><header className="detail-heading"><Link href="/notificacoes/" aria-label="Voltar para notificações"><DashboardIcon name="back"/></Link><h1>Notificação</h1></header>{!ready?<p role="status">Carregando…</p>:error?<p role="alert">{error}</p>:item?<><article className="notification-detail-card"><span className="detail-symbol"><DashboardIcon name="bell"/></span><span className="detail-category">{item.category==="financeira"?"Financeira":"Sistema"}</span><h2>{item.title}</h2><p>{item.description}</p><p className="detail-received">{item.time} · Demonstração</p></article>{item.unread && <button className="detail-view-account" onClick={()=>{void markRead(item.id).catch(reason=>setNotice(reason instanceof Error?reason.message:"Falha ao marcar leitura."));}}>Marcar como lida</button>}{item.accountId && <Link className="detail-view-account" href={`/financeiro/conta/#${item.accountId}`}>Ver conta</Link>}</>:<p>Notificação não encontrada.</p>}<p role="status">{notice}</p></main>;
}
