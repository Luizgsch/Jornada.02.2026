"use client";

/* eslint-disable @next/next/no-img-element -- Os anexos são Blobs locais, sem otimização ou requisição remota. */

import { useEffect, useState } from "react";
import Link from "next/link";
import { deliverNativeDocument } from "@/lib/native-documents";
import { Capacitor } from "@capacitor/core";
import { AppNavigation } from "@/components/app-navigation";
import { DashboardIcon } from "@/components/dashboard-icon";
import { attachmentBlob, formatMoney, listMovements, type Movement } from "@/lib/movements";
import { shortDate } from "@/lib/financial";

type DocumentMovement = Movement & { attachment: NonNullable<Movement["attachment"]> };
const isDocument = (item: Movement): item is DocumentMovement => Boolean(item.attachment);
function fileKind(item: DocumentMovement) { return item.attachment.type === "application/pdf" ? "PDF" : ["image/png", "image/jpeg"].includes(item.attachment.type) ? "Imagem" : "Outro"; }
function fileSize(bytes: number) { return bytes < 1024 * 1024 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / (1024 * 1024)).toLocaleString("pt-BR", { maximumFractionDigits: 1 })} MB`; }

export function DocumentsScreen() {
  const [documents, setDocuments] = useState<DocumentMovement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [attempt, setAttempt] = useState(0);
  const [search, setSearch] = useState("");
  const [kind, setKind] = useState("Todos");
  const [period, setPeriod] = useState("");
  const [selected, setSelected] = useState<DocumentMovement | null>(null);
  const [notice, setNotice] = useState("");
  useEffect(() => {
    let active = true;
    listMovements().then((items) => { if (active) { const docs = items.filter(isDocument); setDocuments(docs); const requested = window.location.hash.slice(1); const matched = docs.find((item) => `doc-${item.id}` === requested); if (matched) setSelected(matched); setLoading(false); } }).catch(() => { if (active) { setError("Não foi possível carregar os anexos."); setLoading(false); } });
    return () => { active = false; };
  }, [attempt]);
  const filtered = documents.filter((item) => (kind === "Todos" || fileKind(item) === kind) && (!period || item.date.startsWith(period)) && `${item.attachment.name} ${item.description} ${item.category}`.toLocaleLowerCase("pt-BR").includes(search.toLocaleLowerCase("pt-BR")));
  return <main className="documents-shell">
    <header className="documents-header"><div><h1>Documentos</h1><p>Comprovantes e anexos financeiros</p></div><Link className="documents-bell" href="/notificacoes/" aria-label="Abrir notificações"><DashboardIcon name="bell" /></Link></header>
    <p className="documents-demo">Demonstração · anexos demonstrativos</p>
    {selected ? <DocumentDetail key={selected.id} item={selected} onBack={() => setSelected(null)} /> : <>
      <Link href="/movimentacao/" className="documents-primary"><DashboardIcon name="attachment" />Anexar à movimentação</Link>
      <label className="documents-search"><DashboardIcon name="search" /><input aria-label="Buscar documento" placeholder="Buscar arquivo ou movimentação" value={search} onChange={(event) => setSearch(event.target.value)} /></label>
      <form key={period} className="documents-period" onSubmit={(event) => { event.preventDefault(); const value = String(new FormData(event.currentTarget).get("month") ?? ""); setPeriod(value); }}><label>Data da movimentação<input name="month" aria-label="Mês da movimentação" type="month" min="1900-01" max="9999-12" defaultValue={period} /></label><button type="submit">Aplicar</button>{period && <button type="button" onClick={() => setPeriod("")}>Limpar mês</button>}</form>
      <div className="documents-filters" role="group" aria-label="Tipo de documento">{["Todos", "PDF", "Imagem"].map((item) => <button key={item} aria-pressed={kind === item} onClick={() => setKind(item)}>{item === "Imagem" ? "Imagens" : item}</button>)}</div>
      {loading ? <p role="status" className="documents-context">Carregando documentos…</p> : error ? <div className="documents-empty" role="alert"><h2>Não foi possível abrir os documentos</h2><p>{error}</p><button className="documents-secondary" onClick={() => { setError(""); setLoading(true); setAttempt((value) => value + 1); }}>Tentar novamente</button></div> : <>
        <p className="documents-count" role="status">{filtered.length} {filtered.length === 1 ? "documento encontrado" : "documentos encontrados"}</p>
        <ul className="documents-list">{filtered.map((item) => <li key={item.id}><button className="documents-card" onClick={() => setSelected(item)} aria-label={`Ver documento ${item.attachment.name}`}><span className="documents-symbol"><DashboardIcon name="document" /></span><span className="documents-copy"><strong>{item.attachment.name}</strong><span>{item.description}</span><span>{fileKind(item)} · {fileSize(item.attachment.size)} · {shortDate(item.date)}</span><span className="documents-view">Ver documento</span></span></button></li>)}</ul>
        {!filtered.length && <div className="documents-empty"><DashboardIcon name="document" /><h2>{documents.length ? "Nenhum resultado encontrado" : "Seus anexos ficam aqui"}</h2><p>{documents.length ? "Altere a busca, o tipo ou o mês para encontrar um documento." : "Adicione um PDF, PNG ou JPG ao registrar uma movimentação de demonstração. O arquivo aparecerá nesta lista."}</p>{documents.length > 0 && <button className="documents-secondary" onClick={() => { setSearch(""); setKind("Todos"); setPeriod(""); }}>Limpar filtros</button>}</div>}
      </>}
    </>}
    <p className="documents-footnote">Arquivos vinculados às movimentações demonstrativas, até 5 MB por anexo. Não há sincronização com o Cash. Use somente arquivos fictícios.</p>
    <AppNavigation active="documents" />
    {notice && <div className="documents-notice" role="status">{notice}<button onClick={() => setNotice("")}>Fechar</button></div>}
  </main>;
}

function DocumentDetail({ item, onBack }: { item: DocumentMovement; onBack: () => void }) {
  const [url, setUrl] = useState("");
  const [imageError, setImageError] = useState(false);
  const [busy, setBusy] = useState(false);
  const [deliveryStatus, setDeliveryStatus] = useState("");
  async function deliver(action: "open" | "share") {
    if (busy) return; setBusy(true); setDeliveryStatus("");
    try { await deliverNativeDocument({...item.attachment, blob: await attachmentBlob(item)}, action); setDeliveryStatus(action === "open" ? "Documento entregue ao leitor do Android." : "Escolha um aplicativo para compartilhar. O envio depende da sua confirmação nele."); }
    catch (error) { const code = (error as { code?: string }).code; setDeliveryStatus(code === "NO_VIEWER" ? "Nenhum leitor compatível instalado. Instale um aplicativo compatível com este arquivo e tente novamente." : code === "UNIMPLEMENTED" ? "Atualize o aplicativo pelo Android Studio para habilitar esta função." : "Não foi possível abrir ou compartilhar. Tente novamente; o anexo continua salvo."); }
    finally { setBusy(false); }
  }
  const [native] = useState(() => Capacitor.isNativePlatform());
  useEffect(() => {
    let active = true; let objectUrl = "";
    attachmentBlob(item).then(blob => { if (!active) return; objectUrl = URL.createObjectURL(blob); setUrl(objectUrl); }).catch(error => { if (active) setDeliveryStatus(error instanceof Error ? error.message : "Não foi possível carregar o arquivo."); });
    return () => { active = false; if (objectUrl) URL.revokeObjectURL(objectUrl); };
  }, [item]);
  const kind = fileKind(item);
  return <section className="document-detail" aria-labelledby="document-title">
    <button className="documents-back" onClick={onBack}><DashboardIcon name="back" />Voltar para documentos</button>
    <p role="status">{!native && deliveryStatus}</p><h2 id="document-title">{item.attachment.name}</h2>
    <p className="documents-context">{kind} · {fileSize(item.attachment.size)} · Anexo da movimentação</p>
    {url && kind === "Imagem" && !imageError && <div className="document-preview"><img src={url} alt={`Anexo da movimentação ${item.description}`} onError={() => setImageError(true)} /></div>}
    {imageError && <p role="alert" className="documents-context">Não foi possível visualizar esta imagem. O arquivo continua salvo.</p>}
    {kind === "PDF" && <div className="document-pdf"><DashboardIcon name="document" /><p>Documento PDF</p><span>{native ? "Arquivo guardado junto à movimentação." : "Baixe o arquivo para visualizar no seu leitor de PDF."}</span></div>}
    {native ? <div className="document-native-actions"><button className="documents-primary" disabled={busy} onClick={() => deliver("open")}>Abrir em outro aplicativo</button><button className="documents-secondary" disabled={busy} onClick={() => deliver("share")}>Compartilhar arquivo</button><p role="status" className="documents-context">{busy ? "Preparando arquivo…" : deliveryStatus}</p></div> : url && <a className="documents-primary" href={url} download={item.attachment.name}><DashboardIcon name="down" />Baixar arquivo</a>}
    <h3>Movimentação vinculada</h3>
    <dl className="document-data"><div><dt>Descrição</dt><dd>{item.description}</dd></div><div><dt>Tipo</dt><dd>{item.type === "entrada" ? "Entrada" : "Saída"}</dd></div><div><dt>Valor</dt><dd>{formatMoney(item.valueInCents)}</dd></div><div><dt>Data</dt><dd>{shortDate(item.date)}</dd></div><div><dt>Categoria</dt><dd>{item.category}</dd></div><div><dt>Conta</dt><dd>{item.account}</dd></div>{item.notes && <div><dt>Observação</dt><dd>{item.notes}</dd></div>}</dl>
  </section>;
}
