"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import { getMovement, type Movement } from "@/lib/movements";
import { MovementForm } from "@/components/movement-form";

export function MovementEditor() {
  const [item, setItem] = useState<Movement | null>(null);
  const [message, setMessage] = useState("Carregando movimentação…");
  useEffect(() => {
    let active = true;
    const id = window.location.hash.slice(1);
    getMovement(id).then((value) => { if (active) { setItem(value); if (!value) setMessage("Movimentação não encontrada ou enviada à lixeira."); } }).catch(() => { if (active) setMessage("Não foi possível carregar. Recarregue a página para tentar novamente."); });
    return () => { active = false; };
  }, []);
  return item ? <MovementForm key={item.id} initial={item} /> : <main className="movement-shell"><h1>Editar movimentação</h1><p role="status">{message}</p><Link href="/financeiro/movimentacoes/">Voltar para movimentações</Link></main>;
}
