import type { Metadata } from "next";
import { ProfileScreen } from "@/components/profile-screen";
import "./perfil.css";

export const metadata: Metadata = { title: "Meu perfil | CASH Mobile" };
export default function ProfilePage() { return <ProfileScreen />; }
