"use client";
import { useState } from "react";
import Link from "next/link";
import { trashMovement, type Movement } from "@/lib/movements";

export function MovementActions({ item, onTrashed }: { item: Movement; onTrashed: () => void }) {
  const [confirm, setConfirm] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  return <div className="movement-record-actions">{!confirm ? <><Link href={`/movimentacao/editar/#${item.id}`}>Editar movimentação</Link><button onClick={() => setConfirm(true)}>Excluir movimentação</button></> : <section aria-label={`Confirmar exclusão de ${item.description}`}><p>Enviar “{item.description}” e seu anexo à lixeira? O registro sai dos totais e pode ser restaurado.</p><button disabled={busy} onClick={async () => { setBusy(true); setError(""); try { await trashMovement(item.id); onTrashed(); } catch { setError("Não foi possível excluir. Tente novamente."); setBusy(false); } }}>{busy ? "Excluindo…" : "Confirmar exclusão"}</button><button disabled={busy} onClick={() => setConfirm(false)}>Cancelar exclusão</button></section>}{error && <p role="alert">{error}</p>}</div>;
}
