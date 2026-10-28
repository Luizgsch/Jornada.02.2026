import { FinancialScreen } from "@/components/financial-screen";
import "../financeiro.css";
export const metadata = { title: "Contas pendentes | CASH Mobile" };
export default function PendingPage() { return <FinancialScreen pendingOnly />; }
