import type { Metadata } from "next";
import { DashboardScreen } from "@/components/dashboard-screen";
import "./menu.css";

export const metadata: Metadata = { title: "Início | CASH Mobile" };

export default function MenuPage() {
  return <DashboardScreen />;
}
