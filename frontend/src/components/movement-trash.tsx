"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import { formatMoney, listMovements, restoreMovement, type Movement } from "@/lib/movements";

export function MovementTrash() {
  const [items, setItems] = useState<Movement[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState<string | null>(null);
  const [notice, setNotice] = useState("");
  const [attempt, setAttempt] = useState(0);
  useEffect(() => { let active = true; listMovements(true).then((value) => { if (active) { setItems(value); setLoaded(true); } }).catch(() => { if (active) { setError("Não foi possível carregar a lixeira."); setLoaded(true); } }); return () => { active = false; }; }, [attempt]);
  return <main className="finance-shell"><Link className="finance-primary" href="/financeiro/movimentacoes/">Voltar para movimentações</Link><h1 className="trash-heading">Lixeira</h1><p className="finance-context">Registros excluídos não entram nos totais nem na lista de documentos. Restaurar recupera a movimentação e o anexo.</p>{!loaded && <p role="status">Carregando…</p>}{error && <p role="alert">{error}</p>}{error && <button className="finance-secondary" onClick={() => { setError(""); setLoaded(false); setAttempt((value) => value + 1); }}>Tentar novamente</button>}<div className="finance-list">{items.map((item) => <article className="finance-bill" key={item.id}><h2 className="trash-title">{item.description}</h2><p className="finance-context">{item.date.split("-").reverse().join("/")} · {item.type === "entrada" ? "Entrada" : "Saída"} · {formatMoney(item.valueInCents)}</p>{item.attachment && <p className="finance-context">Anexo: {item.attachment.name}</p>}<button disabled={busy !== null} className="finance-secondary" onClick={async () => { setBusy(item.id); setError(""); try { await restoreMovement(item.id); setItems((previous) => previous.filter((record) => record.id !== item.id)); setNotice("Movimentação e anexo restaurados. Os totais foram recuperados."); } catch { setError("Não foi possível restaurar. Tente novamente."); } finally { setBusy(null); } }}>{busy === item.id ? "Restaurando…" : "Restaurar movimentação"}</button></article>)}</div>{loaded && !error && !items.length && <p className="finance-context">Nenhuma movimentação na lixeira.</p>}<p role="status" className="finance-context">{notice}</p></main>;
}
