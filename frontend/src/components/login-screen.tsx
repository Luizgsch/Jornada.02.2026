"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import { demoStorage } from "@/lib/demo-storage";

function UserIcon() {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden="true"><circle cx="12" cy="8" r="3.5" /><path d="M5 21v-2a7 7 0 0 1 14 0v2" /></svg>;
}

function LockIcon() {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden="true"><rect x="5" y="10" width="14" height="11" rx="2" /><path d="M8 10V6a4 4 0 0 1 8 0v4" /></svg>;
}

function FingerprintIcon() {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M8 3.8a9 9 0 0 1 13 8.1v2M3 12a9 9 0 0 1 2-5.7M6 15v-3a6 6 0 0 1 12 0v3a11 11 0 0 1-1 4.6M3 15v1M9 19a15 15 0 0 0 0-7 3 3 0 0 1 6 0v3a15 15 0 0 1-1.3 6M12 12v3a18 18 0 0 1-1 6M6 18a12 12 0 0 1-1 3" /></svg>;
}

function ShieldIcon() {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="m12 3 8 3v6c0 4.5-5 7.8-8 9-3-1.2-8-4.5-8-9V6l8-3Z" /><path d="m8.5 12 2.5 2.5 4.5-5" /></svg>;
}

const rememberedUserKey = "remembered-username";

export function LoginScreen() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [remember, setRemember] = useState(false);
  const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
  const [feedback, setFeedback] = useState("");
  const usernameRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    let active = true;
    void demoStorage.read<string>(rememberedUserKey).then((saved) => {
      if (active && typeof saved === "string") { setUsername(saved); setRemember(true); }
    }).catch(() => { /* Armazenamento local pode estar indisponivel no navegador. */ });
    return () => { active = false; };
  }, []);

  async function saveRememberedUser(enabled: boolean, identifier: string) {
    try {
      if (enabled && identifier.trim()) await demoStorage.write(rememberedUserKey, identifier.trim());
      else await demoStorage.remove(rememberedUserKey);
    } catch {
      setFeedback("Não foi possível lembrar o usuário neste dispositivo.");
    }
  }

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = {
      username: username.trim() ? undefined : "Informe seu usuário ou e-mail.",
      password: password ? undefined : "Informe sua senha.",
    };
    setErrors(nextErrors);
    setFeedback("");
    if (nextErrors.username) { usernameRef.current?.focus(); return; }
    if (nextErrors.password) { passwordRef.current?.focus(); return; }
    void saveRememberedUser(remember, username);
    setFeedback("A tela está pronta. O login de demonstração será conectado à API na próxima etapa.");
  }

  return (
    <main className="login-shell">
      <div className="login-content">
        <header className="login-header">
          <div className="cash-brand" aria-label="Cash"><span className="cash-monogram" aria-hidden="true">C</span><span>Cash</span></div>
          <h1>Bem-vindo ao Cash</h1>
          <p>Sua gestão financeira, simples e segura para cartórios</p>
        </header>
        <form className="login-form" onSubmit={submit} noValidate>
          <div className="login-field">
            <label htmlFor="username">Usuário ou e-mail conta escriba</label>
            <div className="input-wrap"><UserIcon /><input ref={usernameRef} id="username" name="username" type="text" placeholder="nome@cartorio.com.br" autoComplete="username" autoCapitalize="none" spellCheck={false} maxLength={254} required value={username} onChange={(event) => { setUsername(event.target.value); setErrors((previous) => ({ ...previous, username: undefined })); setFeedback(""); }} onBlur={() => { if (remember) void saveRememberedUser(true, username); }} aria-invalid={Boolean(errors.username)} aria-describedby={errors.username ? "username-error" : undefined} /></div>
            {errors.username && <p className="field-error" id="username-error">{errors.username}</p>}
          </div>
          <div className="login-field">
            <label htmlFor="password">Senha</label>
            <div className="input-wrap"><LockIcon /><input ref={passwordRef} id="password" name="password" type="password" placeholder="Digite sua senha" autoComplete="current-password" maxLength={128} required value={password} onChange={(event) => { setPassword(event.target.value); setErrors((previous) => ({ ...previous, password: undefined })); setFeedback(""); }} aria-invalid={Boolean(errors.password)} aria-describedby={errors.password ? "password-error" : undefined} /></div>
            {errors.password && <p className="field-error" id="password-error">{errors.password}</p>}
          </div>
          <div className="login-options"><label className="remember-access"><input type="checkbox" checked={remember} onChange={(event) => { setRemember(event.target.checked); void saveRememberedUser(event.target.checked, username); }} /> <span>Lembrar acesso</span></label><button className="text-button" type="button" onClick={() => setFeedback("A recuperação de senha ainda não está disponível nesta demonstração.")}>Esqueci minha senha</button></div>
          <button className="sign-in-button" type="submit">Entrar</button>
          <button className="biometric-button" type="button" onClick={() => setFeedback("A biometria ainda não está disponível nesta demonstração.")}><FingerprintIcon />Acessar com biometria</button>
          <p className="login-feedback" role="status" aria-live="polite">{feedback}</p>
        </form>
      </div>
      <footer className="login-footer"><ShieldIcon /><span>Demonstração acadêmica. Sem dados reais.</span></footer>
    </main>
  );
}

