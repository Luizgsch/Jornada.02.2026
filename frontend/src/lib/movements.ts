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

export async function listMovements(): Promise<Movement[]> {
  const database = await openDatabase();
  return new Promise((resolve, reject) => {
    const transaction = database.transaction("movements", "readonly");
    const request = transaction.objectStore("movements").getAll();
    request.onsuccess = () => { database.close(); resolve((request.result as Movement[]).sort((a, b) => b.createdAt.localeCompare(a.createdAt))); };
    request.onerror = () => { database.close(); reject(request.error); };
  });
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
