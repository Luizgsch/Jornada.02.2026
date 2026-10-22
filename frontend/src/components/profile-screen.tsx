"use client";

import { useState } from "react";
import Link from "next/link";
import { AppNavigation } from "@/components/app-navigation";
import { DashboardIcon } from "@/components/dashboard-icon";
import { useProfile } from "@/components/profile-provider";

export function ProfileScreen() {
  const { profile, ready, loadError, save } = useProfile();
  const [notice, setNotice] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [passwordDone, setPasswordDone] = useState(false);
  const initials = profile.name.trim().split(/\s+/).filter(Boolean).map((part) => part[0]).filter((_, index, all) => index === 0 || index === all.length - 1).join("").toLocaleUpperCase("pt-BR");
  return <main className="profile-shell">
    <header className="profile-heading"><h1>Meu perfil</h1><Link href="/notificacoes/" className="profile-bell" aria-label="Abrir notificações"><DashboardIcon name="bell" /></Link></header>
    <section className="profile-identity" aria-label="Resumo do perfil"><span className="profile-avatar" aria-hidden="true">{initials || "FL"}</span><div><h2>{profile.name}</h2><p>{profile.email}</p><span>Responsável financeiro · Demonstração</span></div></section>
    <p className="profile-note">Dados fictícios. Alterações ficam somente neste dispositivo.</p>
    {!ready ? <p role="status">Carregando perfil…</p> : <>
      {loadError && <p role="alert" className="profile-note">Não foi possível recuperar o perfil salvo. Exibindo dados de exemplo; tente recarregar a página.</p>}
      <form key={JSON.stringify(profile)} className="profile-form" onSubmit={async (event) => {
        event.preventDefault(); const form = event.currentTarget; const data = new FormData(form);
        const name = String(data.get("name") ?? "").trim(); const office = String(data.get("office") ?? "").trim();
        if (!name || !office) { setSaveError("Informe nome e cartório, sem preencher apenas espaços."); form.querySelector<HTMLInputElement>(!name ? "#profile-name" : "#profile-office")?.focus(); return; }
        setSaving(true); setSaveError(""); setNotice("");
        try { await save({ name, email: String(data.get("email") ?? "").trim(), phone: String(data.get("phone") ?? "").trim(), office, hideBalance: data.has("hideBalance"), reduceMotion: data.has("reduceMotion") }); setNotice("Perfil e preferências salvos neste dispositivo."); } catch { setSaveError("Não foi possível salvar. Seus dados continuam no formulário; tente novamente."); } finally { setSaving(false); }
      }}>
        <fieldset disabled={saving}><legend>Dados pessoais</legend>
          <label htmlFor="profile-name">Nome completo<input id="profile-name" name="name" defaultValue={profile.name} required maxLength={100} autoComplete="off" /></label>
          <label htmlFor="profile-email">E-mail<input id="profile-email" name="email" type="email" defaultValue={profile.email} required maxLength={254} autoComplete="off" autoCapitalize="none" /></label>
          <label htmlFor="profile-phone">Telefone <span>(opcional)</span><input id="profile-phone" name="phone" type="tel" defaultValue={profile.phone} maxLength={25} autoComplete="off" placeholder="Use um número fictício" /></label>
          <label htmlFor="profile-office">Cartório<input id="profile-office" name="office" defaultValue={profile.office} required maxLength={120} autoComplete="off" /></label>
          <p className="profile-role">Perfil de acesso: <strong>Responsável financeiro</strong><br />Permissões reais serão definidas na integração.</p>
          <h2 className="profile-section-title">Configurações</h2>
          <label className="profile-setting"><span><strong>Ocultar saldo ao abrir</strong><small>Mantém o saldo oculto ao entrar no Início.</small></span><input type="checkbox" name="hideBalance" defaultChecked={profile.hideBalance} /></label>
          <label className="profile-setting"><span><strong>Reduzir animações</strong><small>Reduz movimentos e transições do aplicativo.</small></span><input type="checkbox" name="reduceMotion" defaultChecked={profile.reduceMotion} /></label>
          <button type="submit" className="profile-primary">{saving ? "Salvando…" : "Salvar alterações"}</button>
        </fieldset>
        {saveError && <p role="alert" className="profile-error">{saveError}</p>}
      </form>
      <section className="profile-security" aria-labelledby="security-title"><h2 id="security-title">Segurança</h2><details className="profile-password"><summary><DashboardIcon name="user" /><span>Trocar senha<small>Simulação, sem alterar credenciais reais</small></span></summary><p className="profile-note">Use senhas fictícias. O login atual é demonstrativo; nenhuma senha será salva.</p><form onSubmit={(event) => { event.preventDefault(); const data = new FormData(event.currentTarget); const current = String(data.get("current") ?? ""); const next = String(data.get("new") ?? ""); if (current === next) { setPasswordError("Use uma nova senha diferente da atual."); setPasswordDone(false); return; } if (next !== data.get("confirm")) { setPasswordError("As senhas não coincidem."); setPasswordDone(false); event.currentTarget.querySelector<HTMLInputElement>("#profile-confirm")?.focus(); return; } event.currentTarget.reset(); setPasswordError(""); setPasswordDone(true); }} onChange={() => { setPasswordError(""); setPasswordDone(false); }}><label htmlFor="profile-current">Senha atual fictícia<input type="password" id="profile-current" name="current" required maxLength={128} autoComplete="off" /></label><label htmlFor="profile-new">Nova senha fictícia<input type="password" id="profile-new" name="new" required minLength={8} maxLength={128} autoComplete="new-password" aria-describedby="profile-password-help" /></label><p id="profile-password-help" className="profile-note">Mínimo de 8 caracteres.</p><label htmlFor="profile-confirm">Confirmar nova senha<input type="password" id="profile-confirm" name="confirm" required minLength={8} maxLength={128} autoComplete="new-password" /></label>{passwordError && <p className="profile-error" role="alert">{passwordError}</p>}<button className="profile-primary" type="submit">Simular troca de senha</button>{passwordDone && <p className="profile-note" role="status">Simulação concluída. Nenhuma senha real foi alterada ou armazenada.</p>}</form></details>
      <div className="profile-biometry"><strong>Biometria</strong><p>Será implementada em breve.</p></div></section>
      <section className="profile-about"><h2>Sobre o aplicativo</h2><p>CASH Mobile · Projeto acadêmico</p><p>Modo demonstrativo, sem autenticação ou integração com o Cash.</p><Link href="/" className="profile-exit">Sair da demonstração</Link><p>Seus registros locais permanecem neste dispositivo.</p></section>
    </>}
    <AppNavigation active="profile" />
    {notice && <div className="profile-status" role="status">{notice}<button onClick={() => setNotice("")}>Fechar</button></div>}
  </main>;
}
