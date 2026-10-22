import type { Metadata } from "next";
import { MovementForm } from "@/components/movement-form";
import "./movement.css";

export const metadata: Metadata = { title: "Nova movimentação | CASH Mobile" };
export default function MovementPage() { return <MovementForm />; }
