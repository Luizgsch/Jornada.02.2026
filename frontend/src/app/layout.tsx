import type { Metadata, Viewport } from "next";
import { NativeLifecycle } from "@/components/native-lifecycle";
import { Inter } from "next/font/google";
import "./globals.css";
import "./login.css";

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "CASH Mobile",
  description: "Demonstração acadêmica do CASH Mobile",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  viewportFit: "cover",
  interactiveWidget: "resizes-content",
  themeColor: "#ffffff",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="pt-BR"
      className={`${inter.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col"><NativeLifecycle />{children}</body>
    </html>
  );
}
