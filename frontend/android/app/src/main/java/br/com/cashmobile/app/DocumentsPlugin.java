package br.com.cashmobile.app;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import androidx.core.content.FileProvider;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@CapacitorPlugin(name = "CashDocuments")
public class DocumentsPlugin extends Plugin {
    @PluginMethod public void open(PluginCall call) { deliver(call, false); }
    @PluginMethod public void share(PluginCall call) { deliver(call, true); }

    private void deliver(PluginCall call, boolean share) {
        try {
            String mime = call.getString("mime", "");
            String extension;
            if (mime.equals("application/pdf")) extension = ".pdf";
            else if (mime.equals("image/png")) extension = ".png";
            else if (mime.equals("image/jpeg")) extension = ".jpg";
            else { call.reject("Tipo de arquivo não permitido.", "INVALID_FILE"); return; }
            String encoded = call.getString("base64", "");
            if (encoded.length() > 6990510) { call.reject("Arquivo maior que 5 MB.", "INVALID_FILE"); return; }
            byte[] content = Base64.decode(encoded, Base64.DEFAULT);
            if (content.length == 0 || content.length > 5 * 1024 * 1024) { call.reject("Arquivo inválido.", "INVALID_FILE"); return; }
            File directory = new File(getContext().getCacheDir(), "shared-documents");
            if (!directory.exists() && !directory.mkdirs()) throw new java.io.IOException("Cache indisponível");
            File[] oldFiles = directory.listFiles();
            if (oldFiles != null) for (File old : oldFiles) {
                if (old.isFile() && System.currentTimeMillis() - old.lastModified() > 24L * 60 * 60 * 1000) old.delete();
            }
            String name = call.getString("name", "documento").replaceAll("[^\\p{L}\\p{N}._ -]", "_");
            if (name.length() > 100) name = name.substring(0, 100);
            // Prefixo aleatório e nome sanitizado: o chamador nunca controla o caminho.
            String fileName = name.isEmpty() ? "documento" : name;
            if (!fileName.toLowerCase(java.util.Locale.ROOT).endsWith(extension)) fileName += extension;
            File file = new File(directory, UUID.randomUUID().toString() + "-" + fileName);
            try (FileOutputStream output = new FileOutputStream(file)) { output.write(content); }
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);
            final String title = name.isEmpty() ? "Documento" : name;
            getActivity().runOnUiThread(() -> {
                try {
                    Intent intent;
                    if (share) {
                        intent = new Intent(Intent.ACTION_SEND);
                        intent.setType(mime);
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        intent.putExtra(Intent.EXTRA_TITLE, title);
                    } else {
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, mime);
                    }
                    intent.setClipData(ClipData.newRawUri(title, uri));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    getActivity().startActivity(share ? Intent.createChooser(intent, "Compartilhar documento") : intent);
                    call.resolve(new JSObject().put("opened", true));
                } catch (ActivityNotFoundException error) { call.reject("Instale um aplicativo compatível para abrir este arquivo.", "NO_VIEWER"); }
                catch (Exception error) { call.reject("Não foi possível abrir o documento.", "OPEN_FAILED", error); }
            });
        } catch (Exception error) { call.reject("Não foi possível preparar o arquivo.", "FILE_FAILED", error); }
    }
}
