import type { Metadata } from "next";
import { DocumentsScreen } from "@/components/documents-screen";
import "./documentos.css";

export const metadata: Metadata = { title: "Documentos | CASH Mobile" };
export default function DocumentsPage() { return <DocumentsScreen />; }
