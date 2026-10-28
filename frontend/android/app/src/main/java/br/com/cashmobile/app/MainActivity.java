package br.com.cashmobile.app;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;
import android.os.Bundle;
import android.content.pm.ApplicationInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(DocumentsPlugin.class);
        super.onCreate(savedInstanceState);
        // Next exporta uma página HTML por rota; o fallback SPA do Capacitor
        // devolveria o login ao recarregar qualquer página interna.
        getBridge().setWebViewClient(new BridgeWebViewClient(getBridge()) {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String path = request.getUrl().getPath();
                if (getBridge().getServerUrl() == null && request.isForMainFrame()
                        && "GET".equals(request.getMethod())
                        && "https".equals(request.getUrl().getScheme())
                        && "localhost".equals(request.getUrl().getHost())
                        && path != null && !"/".equals(path) && path.matches("/[a-zA-Z0-9_/-]+")) {
                    String asset = "public" + path + (path.endsWith("/") ? "index.html" : "/index.html");
                    try {
                        InputStream page = getAssets().open(asset);
                        return new WebResourceResponse("text/html", "UTF-8",
                                getBridge().getLocalServer().getJavaScriptInjectedStream(page));
                    } catch (IOException ignored) {
                        // Mantém o tratamento padrão para recursos não exportados.
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        // Apenas builds de depuracao podem acessar a API HTTP local do emulador.
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            getBridge().getWebView().getSettings()
                    .setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }
}
