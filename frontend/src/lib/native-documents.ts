import { registerPlugin } from "@capacitor/core";

type NativeFile = { name: string; mime: string; base64: string };
const documents = registerPlugin<{ open(file: NativeFile): Promise<void>; share(file: NativeFile): Promise<void> }>("CashDocuments");

export async function deliverNativeDocument(attachment: { name: string; type: string; blob: Blob }, action: "open" | "share") {
  if (attachment.blob.size === 0 || attachment.blob.size > 5 * 1024 * 1024) throw new Error("Arquivo inválido ou maior que 5 MB.");
  const data = await new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(new Error("Não foi possível ler o arquivo salvo."));
    reader.onload = () => resolve(String(reader.result).split(",")[1] ?? "");
    reader.readAsDataURL(attachment.blob);
  });
  await documents[action]({ name: attachment.name, mime: attachment.type, base64: data });
}
