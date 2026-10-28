import { demoFinancialMonth, demoPayables, demoReceivables } from "./financial";
import type { Movement } from "./movements";

export function dashboardSummary(movements: Movement[]) {
  const pending = demoPayables.filter((item) => item.dueDate.startsWith(demoFinancialMonth) && !item.paidDate);
  const months = Array.from({ length: 6 }, (_, index) => {
    const date = new Date(`${demoFinancialMonth}-01T12:00:00Z`);
    date.setUTCMonth(date.getUTCMonth() - 5 + index);
    const key = date.toISOString().slice(0, 7);
    const records = movements.filter((item) => item.date.startsWith(key));
    return { key, label: date.toLocaleDateString("pt-BR", { month: "short", timeZone: "UTC" }).replace(".", ""), entries: records.filter((item) => item.type === "entrada").reduce((sum, item) => sum + item.valueInCents, 0), exits: records.filter((item) => item.type === "saida").reduce((sum, item) => sum + item.valueInCents, 0) };
  });
  return {
    payable: pending.reduce((sum, item) => sum + item.cents, 0),
    receivable: demoReceivables.filter((item) => item.dueDate.startsWith(demoFinancialMonth)).reduce((sum, item) => sum + item.cents, 0),
    pendingCount: pending.length,
    moved: movements.filter((item) => item.date.startsWith(demoFinancialMonth)).reduce((sum, item) => sum + item.valueInCents, 0),
    balance: movements.reduce((sum, item) => sum + (item.type === "entrada" ? item.valueInCents : -item.valueInCents), 0),
    months,
  };
}
