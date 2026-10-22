import type { Metadata } from "next";
import { FinancialScreen } from "@/components/financial-screen";
import "../financeiro.css";

export const metadata: Metadata = { title: "Movimentações | CASH Mobile" };
export default function MovementsPage() { return <FinancialScreen initialSection="Movimentações" />; }
