import type { Metadata } from "next";
import { SearchScreen } from "@/components/search-screen";
import "../financeiro/financeiro.css";
import "../fluxos.css";

export const metadata: Metadata = { title: "Pesquisa | CASH Mobile" };
export default function SearchPage() { return <SearchScreen />; }
