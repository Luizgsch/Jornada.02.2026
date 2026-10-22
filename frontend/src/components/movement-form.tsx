"use client";

import { useRef, useState, type ChangeEvent, type FormEvent } from "react";
import Link from "next/link";
import { DashboardIcon } from "@/components/dashboard-icon";
import { formatMoney, movementCategories, parseMoney, saveMovement } from "@/lib/movements";

export function MovementForm() {
  const [type, setType] = useState<"entrada" | "saida">("entrada");
  const [description, setDescription] = useState("");
  const [value, setValue] = useState("");
  const [category, setCategory] = useState("");
  const [notes, setNotes] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [status, setStatus] = useState("");
  const [saving, setSaving] = useState(false);
  const fileInput = useRef<HTMLInputElement>(null);
  const formRef = useRef<HTMLFormElement>(null);

  function selectFile(event: ChangeEvent<HTMLInputElement>) {
    const selected = event.target.files?.[0];
    if (!selected) return;
    if (!["application/pdf", "image/png", "image/jpeg"].includes(selected.type) || selected.size > 5 * 1024 * 1024 || selected.size === 0) {
      setFile(null);
      setErrors((previous) => ({ ...previous, attachment: "Escolha PDF, PNG ou JPG, não vazio, de até 5 MB." }));
      event.target.value = "";
      return;
    }
    setFile(selected);
    setErrors((previous) => ({ ...previous, attachment: "" }));
    setStatus("");
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (saving) return;
    const date = String(new FormData(event.currentTarget).get("date") ?? "");
    const cents = parseMoney(value);
    const validDate = /^\d{4}-\d{2}-\d{2}$/.test(date) && date >= "1900-01-01" && date <= "9999-12-31" && !Number.isNaN(Date.parse(`${date}T00:00:00Z`)) && new Date(`${date}T00:00:00Z`).toISOString().slice(0, 10) === date;
    const nextErrors: Record<string, string> = {};
    if (!description.trim()) nextErrors.description = "Informe a descrição.";
    if (cents === null) nextErrors.value = "Informe um valor positivo em reais, como 125,50.";
    if (!validDate) nextErrors.date = "Informe uma data válida.";
    if (!movementCategories.includes(category as typeof movementCategories[number])) nextErrors.category = "Selecione uma categoria.";
    setErrors(nextErrors);
    setStatus("");
    const first = Object.keys(nextErrors)[0];
    if (first) { formRef.current?.querySelector<HTMLElement>(`[name="${first}"]`)?.focus(); return; }
    if (cents === null) return;
    setSaving(true);
    try {
      await saveMovement({ id: crypto.randomUUID(), type, description: description.trim(), valueInCents: cents, date, category, account: "Conta principal", notes: notes.trim(), createdAt: new Date().toISOString(), ...(file ? { attachment: { name: file.name, type: file.type, size: file.size, blob: file } } : {}) });
      setDescription(""); setValue(""); setCategory(""); setNotes(""); setFile(null); setType("entrada");
      formRef.current?.reset();
      if (fileInput.current) fileInput.current.value = "";
      setStatus("Movimentação salva somente neste dispositivo, como demonstração.");
    } catch {
      setStatus("Não foi possível salvar neste dispositivo. Seus dados continuam no formulário; tente novamente.");
    } finally { setSaving(false); }
  }

  const fieldError = (name: string) => errors[name] ? <p className="movement-error" id={`${name}-error`}>{errors[name]}</p> : null;
  return <main className="movement-shell">
    <header className="movement-header"><Link href="/menu/" aria-label="Voltar para o início"><DashboardIcon name="back" /></Link><div><h1>Nova movimentação</h1><p>Preencha os dados do lançamento</p></div></header>
    <form ref={formRef} className="movement-form" noValidate onSubmit={submit}>
      <fieldset disabled={saving} className="movement-fields">
        <legend className="sr-only">Dados da movimentação demonstrativa</legend>
        <div className="movement-type" role="group" aria-label="Tipo de movimentação"><button type="button" aria-pressed={type === "entrada"} onClick={() => setType("entrada")}>Entrada</button><button type="button" aria-pressed={type === "saida"} onClick={() => setType("saida")}>Saída</button></div>
        <div className="movement-field"><label htmlFor="movement-description">Descrição</label><input id="movement-description" name="description" placeholder="Pendência do cartório" value={description} maxLength={160} onChange={(e) => { setDescription(e.target.value); setStatus(""); }} required aria-invalid={Boolean(errors.description)} aria-describedby={errors.description ? "description-error" : undefined} />{fieldError("description")}</div>
        <div className="movement-field"><label htmlFor="movement-value">Valor</label><input id="movement-value" name="value" inputMode="decimal" placeholder="R$ 0,00" value={value} maxLength={20} onChange={(e) => { setValue(e.target.value); setStatus(""); }} onBlur={() => { const cents = parseMoney(value); if (cents !== null) setValue(formatMoney(cents)); }} required aria-invalid={Boolean(errors.value)} aria-describedby={errors.value ? "value-error" : undefined} />{fieldError("value")}</div>
        <div className="movement-field"><label htmlFor="movement-date">Data</label><input id="movement-date" name="date" type="date" min="1900-01-01" max="9999-12-31" defaultValue="" required aria-invalid={Boolean(errors.date)} aria-describedby={errors.date ? "date-error" : undefined} />{fieldError("date")}</div>
        <div className="movement-field"><label htmlFor="movement-category">Categoria</label><select id="movement-category" name="category" value={category} onChange={(e) => setCategory(e.target.value)} required aria-invalid={Boolean(errors.category)} aria-describedby={errors.category ? "category-error" : undefined}><option value="">Selecione uma categoria</option>{movementCategories.map((item) => <option key={item} value={item}>{item}</option>)}</select>{fieldError("category")}</div>
        <div className="movement-field"><label htmlFor="movement-account">Conta</label><select id="movement-account" name="account" defaultValue="Conta principal"><option>Conta principal</option></select></div>
        <div className="movement-field"><label htmlFor="movement-notes">Observação</label><textarea id="movement-notes" name="notes" placeholder="Informações adicionais" rows={3} maxLength={1000} value={notes} onChange={(e) => setNotes(e.target.value)} /></div>
        <div className="movement-attachment"><input ref={fileInput} id="movement-file" aria-label="Selecionar anexo de demonstração" type="file" accept="application/pdf,image/png,image/jpeg" onChange={selectFile} className="sr-only" tabIndex={-1} /><button className="attachment-button" type="button" onClick={() => fileInput.current?.click()}><DashboardIcon name="attachment" />{file ? "Trocar anexo" : "Adicionar anexo"}</button>{file && <div className="selected-attachment"><span>{file.name}</span><button type="button" onClick={() => { setFile(null); if (fileInput.current) fileInput.current.value = ""; }}>Remover anexo</button></div>}{fieldError("attachment")}</div>
        <div className="movement-form-actions"><button className="movement-save" type="submit"><DashboardIcon name="check" />{saving ? "Salvando…" : "Salvar movimentação"}</button><Link href="/menu/">Cancelar</Link></div>
      </fieldset>
      <p className="movement-status" role="status" aria-live="polite">{status}</p>
      <p className="movement-demo-note">Demonstração local. Categorias e conta de exemplo. Use apenas dados e anexos fictícios.</p>
    </form>
  </main>;
}
