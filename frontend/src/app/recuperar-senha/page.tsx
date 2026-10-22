import type { Metadata } from "next";
import { PasswordRecovery } from "@/components/password-recovery";
import "../fluxos.css";

export const metadata: Metadata = { title: "Recuperar senha | CASH Mobile" };
export default function RecoveryPage() { return <PasswordRecovery />; }
