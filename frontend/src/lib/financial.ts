// Catálogo fictício de consulta. Não representa contratos ou dados da Escriba.
export const financialReferenceDate = "2026-10-22";
export type Payable = { id: string; description: string; supplier: string; category: string; dueDate: string; cents: number; paidDate?: string; account: string };
export const demoPayables: Payable[] = [
  { id: "energia", description: "Energia elétrica", supplier: "Fornecedor de energia (exemplo)", category: "Infraestrutura", dueDate: "2026-10-18", cents: 68000, account: "Conta principal" },
  { id: "aluguel", description: "Aluguel do escritório", supplier: "Locador (exemplo)", category: "Infraestrutura", dueDate: "2026-10-23", cents: 420000, account: "Conta principal" },
  { id: "internet", description: "Internet e telefonia", supplier: "Operadora (exemplo)", category: "Serviços", dueDate: "2026-10-26", cents: 36000, account: "Conta principal" },
  { id: "software", description: "Software de gestão", supplier: "Fornecedor de software (exemplo)", category: "Tecnologia", dueDate: "2026-10-15", cents: 89000, paidDate: "2026-10-15", account: "Conta principal" },
];
export function payableStatus(item: Payable): "Pago" | "Vencido" | "Em aberto" {
  return item.paidDate ? "Pago" : item.dueDate < financialReferenceDate ? "Vencido" : "Em aberto";
}
export const demoRevenue = [
  { sector: "Notas", employee: "Funcionário A", cents: 1080000 },
  { sector: "Notas", employee: "Funcionário B", cents: 720000 },
  { sector: "Registro", employee: "Funcionário C", cents: 850000 },
];
export const demoBudget = [
  { category: "Infraestrutura", planned: 500000, actual: 488000 },
  { category: "Serviços", planned: 30000, actual: 36000 },
  { category: "Tecnologia", planned: 100000, actual: 89000 },
];
export const demoFinancialMonth = "2026-10";
export const demoReceivables = [
  { id: "receber-notas", description: "Serviços de notas", payer: "Cliente A (exemplo)", dueDate: "2026-10-25", cents: 1800000 },
  { id: "receber-registro", description: "Serviços de registro", payer: "Cliente B (exemplo)", dueDate: "2026-10-30", cents: 850000 },
];
export function shortDate(value: string) { return value.split("-").reverse().join("/"); }
