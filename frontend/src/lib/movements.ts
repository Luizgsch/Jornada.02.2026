import * as local from "./movements-local";
import { apiMode, allPages, request } from "./session";
export const movementCategories = ["Receitas diversas", "Despesas administrativas", "Outros"] as const;
export type Movement = {
  id: string;
  type: "entrada" | "saida";
  description: string;
  valueInCents: number;
  date: string;
  category: string;
  account: string;
  notes: string;
  createdAt: string;
  deletedAt?: string;
  attachment?: { name: string; type: string; size: number; blob: Blob };
};


type DocumentInfo = {movementId: string; name: string; contentType: string; size: number};
async function enrich(items: Movement[]): Promise<Movement[]> {
  const documents = await allPages<DocumentInfo>("/documents");
  const byMovement = new Map(documents.map(document => [document.movementId, document]));
  return items.map(item => { const document = byMovement.get(item.id); return {...item, deletedAt: item.deletedAt || undefined, ...(document ? {attachment: {name: document.name, type: document.contentType, size: document.size, blob: new Blob(), remote: true}} : {})}; });
}
export async function attachmentBlob(item: Movement): Promise<Blob> {
  if (!item.attachment) throw new Error("Anexo não encontrado.");
  return apiMode ? request<Blob>(`/movements/${encodeURIComponent(item.id)}/attachment`, {}, true) : item.attachment.blob;
}
export async function listMovements(onlyDeleted = false): Promise<Movement[]> {
  if (!apiMode) return local.listMovements(onlyDeleted);
  const items = await allPages<Movement>(`/movements?deleted=${onlyDeleted}`);
  return onlyDeleted ? items : enrich(items);
}
export async function getMovement(id: string): Promise<Movement | null> {
  if (!apiMode) return local.getMovement(id);
  const [item] = await enrich([await request<Movement>(`/movements/${encodeURIComponent(id)}`)]);
  if (item.attachment) item.attachment.blob = await attachmentBlob(item);
  return item;
}
async function write(movement: Movement, create: boolean): Promise<void> {
  const {type,description,valueInCents,date,category,account,notes} = movement;
  const result = await request<Movement>(create ? "/movements" : `/movements/${encodeURIComponent(movement.id)}`, {method: create ? "POST" : "PUT", body: JSON.stringify({type,description,valueInCents,date,category,account,notes})});
  // Keep the server ID on partial success so retry updates instead of duplicating.
  movement.id = result.id;
  try {
    if (movement.attachment) { const body = new FormData(); body.append("file", movement.attachment.blob, movement.attachment.name); await request(`/movements/${result.id}/attachment`, {method:"PUT", body}); }
    else if (!create) await request(`/movements/${result.id}/attachment`, {method:"DELETE"});
  } catch (error) { throw new MovementSavedError(result.id, error instanceof Error ? error.message : "Falha no anexo."); }
}
export class MovementSavedError extends Error { constructor(public movementId: string, message: string) {super(`Movimentação salva, mas anexo não atualizado. ${message} Tente salvar novamente.`);} }
export async function saveMovement(movement: Movement) { if (!apiMode) return local.saveMovement(movement); await write(movement,true); }
export async function updateMovement(movement: Movement) { if (!apiMode) return local.updateMovement(movement); await write(movement,false); }
export async function trashMovement(id: string) { if (!apiMode) return local.trashMovement(id); await request(`/movements/${encodeURIComponent(id)}`, {method:"DELETE"}); }
export async function restoreMovement(id: string) { if (!apiMode) return local.restoreMovement(id); await request(`/movements/${encodeURIComponent(id)}/restore`, {method:"POST"}); }
export function formatMoney(cents: number): string {
  return (cents / 100).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export function parseMoney(value: string): number | null {
  const normalized = value.replace(/R\$/g, "").replace(/\s/g, "");
  if (!/^(?:\d+|\d{1,3}(?:\.\d{3})+)(?:,\d{1,2})?$/.test(normalized)) return null;
  const [whole, fraction = ""] = normalized.replace(/\./g, "").split(",");
  const cents = Number(whole) * 100 + Number(fraction.padEnd(2, "0"));
  return Number.isSafeInteger(cents) && cents > 0 && cents <= 99999999999 ? cents : null;
}
