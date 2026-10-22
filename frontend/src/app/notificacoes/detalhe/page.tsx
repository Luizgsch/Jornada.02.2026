import type { Metadata } from "next";
import { NotificationDetail } from "@/components/notification-detail";
import "../notifications.css";
import "./detail.css";

export const metadata: Metadata = { title: "Notificação | CASH Mobile" };
export default function DetailPage() { return <NotificationDetail />; }
