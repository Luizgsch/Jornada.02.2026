import type { CapacitorConfig } from "@capacitor/cli";

const config: CapacitorConfig = {
  appId: "br.com.cashmobile.app",
  appName: "CASH Mobile",
  webDir: "out",
  // Habilitado somente pelo comando de desenvolvimento, sem URL remota persistida.
  server: process.env.CAP_LIVE_RELOAD === "1"
    ? { androidScheme: "https", url: "http://10.0.2.2:3000" }
    : { androidScheme: "https" },
};

export default config;
