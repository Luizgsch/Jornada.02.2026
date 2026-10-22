import type { Metadata } from "next";
import { FinancialScreen } from "@/components/financial-screen";
import "./financeiro.css";

export const metadata: Metadata = { title: "Financeiro | CASH Mobile" };
export default function FinancialPage() { return <FinancialScreen />; }
