import type { Metadata } from "next";
import Link from "next/link";
import { DashboardIcon } from "@/components/dashboard-icon";
import "../../financeiro/financeiro.css";
import "../../fluxos.css";

export const metadata: Metadata = { title: "Conta a pagar | CASH Mobile" };
export default function PayablePage() {
  return <main className="finance-shell flow-shell"><Link className="flow-back" href="/notificacoes/detalhe/"><DashboardIcon name="back" />Voltar à notificação</Link><h1>Conta a pagar</h1><p className="finance-demo">Demonstração · conta do protótipo</p><article className="flow-card"><span className="flow-symbol"><DashboardIcon name="wallet" /></span><h2>Aluguel do escritório</h2><p className="flow-value">R$ 4.200,00</p><dl className="finance-detail"><div><dt>Vencimento</dt><dd>15/06/2025</dd></div><div><dt>Origem</dt><dd>Notificação do Figma</dd></div><div><dt>Situação no exemplo</dt><dd>Em aberto</dd></div></dl><p className="finance-context">Data e valor correspondem à notificação original. Este exemplo é separado das contas de outubro de 2026 do Financeiro.</p></article><p className="finance-footnote">Consulta demonstrativa. Fornecedor, histórico e documentos desta conta não foram informados no protótipo. Nenhum pagamento será realizado.</p><Link className="finance-primary" href="/financeiro/">Consultar outras contas</Link></main>;
}
