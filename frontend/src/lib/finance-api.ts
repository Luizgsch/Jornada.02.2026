import { allPages, apiMode, request } from "./session";
import { demoPayables, demoReceivables, demoRevenue, demoBudget, demoFinancialMonth } from "./financial";
type Account = {id:string; description:string; party:string; category:string; dueDate:string; valueInCents:number; paidDate?:string; account:string};
export async function financialCatalog(month = demoFinancialMonth) {
  if (!apiMode) return {payables: demoPayables.filter(x=>x.dueDate.startsWith(month)), receivables: demoReceivables.filter(x=>x.dueDate.startsWith(month)), revenue: month===demoFinancialMonth ? demoRevenue : [], budget: month===demoFinancialMonth ? demoBudget : []};
  const [payables, receivables, revenue, budget] = await Promise.all([
    allPages<Account>(`/finance/accounts?kind=payable&month=${month}`),
    allPages<Account>(`/finance/accounts?kind=receivable&month=${month}`),
    request<{items:{sector:string;employee:string;valueInCents:number}[]}>(`/finance/revenue?month=${month}`),
    request<{items:{category:string;plannedCents:number;actualCents:number}[]}>(`/finance/budget?month=${month}`),
  ]);
  return {payables: payables.map(x=>({...x,supplier:x.party,cents:x.valueInCents})), receivables: receivables.map(x=>({...x,payer:x.party,cents:x.valueInCents})), revenue:revenue.items.map(x=>({...x,cents:x.valueInCents})), budget:budget.items.map(x=>({category:x.category,planned:x.plannedCents,actual:x.actualCents}))};
}
export type FinancialCatalog = Awaited<ReturnType<typeof financialCatalog>>;
export const emptyCatalog: FinancialCatalog = {payables:[],receivables:[],revenue:[],budget:[]};
