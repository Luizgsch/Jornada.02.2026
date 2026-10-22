import type { Metadata } from "next";
import { NotificationsScreen } from "@/components/notifications-screen";
import "./notifications.css";

export const metadata: Metadata = { title: "Notificações | CASH Mobile" };
export default function NotificationsPage() { return <NotificationsScreen />; }
