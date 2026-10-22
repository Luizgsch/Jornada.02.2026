"use client";

import { useEffect } from "react";
import { App } from "@capacitor/app";
import { Capacitor, type PluginListenerHandle } from "@capacitor/core";

export function NativeLifecycle() {
  useEffect(() => {
    if (!Capacitor.isNativePlatform()) return;
    let disposed = false;
    let handle: PluginListenerHandle | undefined;
    void App.addListener("backButton", ({ canGoBack }) => {
      if (window.location.pathname !== "/" && canGoBack) window.history.back();
      else void App.minimizeApp();
    }).then((listener) => {
      if (disposed) void listener.remove();
      else handle = listener;
    });
    return () => { disposed = true; void handle?.remove(); };
  }, []);
  return null;
}
