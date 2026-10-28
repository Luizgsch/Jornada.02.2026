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

function openDatabase(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open("cash-mobile-demo", 1);
    request.onupgradeneeded = () => {
      request.result.createObjectStore("movements", { keyPath: "id" });
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
    request.onblocked = () => reject(new Error("Armazenamento ocupado"));
  });
}

// IndexedDB guarda o registro e o arquivo juntos, sem upload ou acesso ao legado.
export async function saveMovement(movement: Movement): Promise<void> {
  const database = await openDatabase();
  return new Promise((resolve, reject) => {
    const transaction = database.transaction("movements", "readwrite");
    transaction.objectStore("movements").add(movement);
    transaction.oncomplete = () => { database.close(); resolve(); };
    transaction.onerror = transaction.onabort = () => { database.close(); reject(transaction.error ?? new Error("Falha ao salvar")); };
  });
}

export async function listMovements(onlyDeleted = false): Promise<Movement[]> {
  const database = await openDatabase();
  return new Promise((resolve, reject) => {
    const transaction = database.transaction("movements", "readonly");
    const request = transaction.objectStore("movements").getAll();
    request.onsuccess = () => { database.close(); resolve((request.result as Movement[]).filter((item) => Boolean(item.deletedAt) === onlyDeleted).sort((a, b) => b.createdAt.localeCompare(a.createdAt))); };
    request.onerror = () => { database.close(); reject(request.error); };
  });
}

export async function getMovement(id: string): Promise<Movement | null> {
  const items = await listMovements();
  return items.find((item) => item.id === id) ?? null;
}

// A leitura e a alteração usam a mesma transação para não recriar registros excluídos.
async function changeMovement(id: string, change: (current: Movement) => Movement): Promise<void> {
  const database = await openDatabase();
  return new Promise((resolve, reject) => {
    const transaction = database.transaction("movements", "readwrite");
    const store = transaction.objectStore("movements");
    const request = store.get(id);
    let failure: Error | undefined;
    request.onsuccess = () => {
      try {
        if (!request.result) throw new Error("Movimentação não encontrada");
        store.put(change(request.result as Movement));
      } catch (error) { failure = error instanceof Error ? error : new Error("Falha na alteração"); transaction.abort(); }
    };
    transaction.oncomplete = () => { database.close(); resolve(); };
    transaction.onabort = transaction.onerror = () => { database.close(); reject(failure ?? transaction.error ?? new Error("Falha na alteração")); };
  });
}

export function updateMovement(movement: Movement): Promise<void> {
  return changeMovement(movement.id, (current) => {
    if (current.deletedAt) throw new Error("Movimentação está na lixeira");
    return { ...movement, createdAt: current.createdAt, deletedAt: undefined };
  });
}
export function trashMovement(id: string): Promise<void> {
  return changeMovement(id, (current) => ({ ...current, deletedAt: new Date().toISOString() }));
}
export function restoreMovement(id: string): Promise<void> {
  return changeMovement(id, (current) => ({ ...current, deletedAt: undefined }));
}

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
