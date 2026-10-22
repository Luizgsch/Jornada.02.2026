# CASH Mobile

## Demonstracao no GitHub Pages

URL: https://luizgsch.github.io/Jornada.02.2026/

O workflow `.github/workflows/pages.yml` publica o frontend estatico a cada alteracao em `frontend/` na branch `main`. O prefixo `/Jornada.02.2026` e aplicado somente no build do Pages por `PAGES_BASE_PATH`; localhost e Android continuam na raiz. Spring nao e hospedado no Pages. A pagina publica e uma demonstracao visual, sem login real.

Projeto academico com interface Next.js exportada como arquivos estaticos, aplicativo Android com Capacitor e API Java/Spring Boot independente. A pagina Início reproduz visualmente o login do Figma, com validacao local e opcao de lembrar somente o usuario. Autenticacao no Spring, recuperacao, biometria e telas financeiras ainda estao pendentes. O progresso esta em `frontend/CHECKLIST.md`.

## Estrutura

```text
frontend/
  src/app/                 Interface responsiva
  src/components/          Integracao do ciclo de vida Android
  src/lib/api.ts           Acesso direto a API
  src/lib/demo-storage.ts  Dados ficticios locais via Preferences
  android/                 Projeto nativo para abrir no Android Studio
  capacitor.config.ts      Identidade e diretorio de assets do aplicativo
backend/
  src/main/java/br/com/cashmobile/
.vscode/tasks.json         Tarefas independentes e execucao simultanea
```

## Arquitetura e limites

- Browser ou aplicativo Android chama Spring Boot por HTTP(S). O dispositivo nunca acessa o banco diretamente.
- Next usa `output: export`. Nao usar Server Actions, cookies de servidor, endpoints Next nem rewrites. Rotas dinamicas futuras precisam funcionar com exportacao estatica; para registros criados em runtime, preferir rota estatica de detalhe com identificador em query string.
- O APK carrega `frontend/out` localmente. Nao depende de um servidor Next para exibir a interface. Consultas a API exigem conectividade; nao foi implementada sincronizacao offline.
- Login padrao sera implementado no Spring, fora do banco. Nenhuma credencial ou token deve entrar no bundle web, em variavel `NEXT_PUBLIC_*` ou em Preferences.
- `demoStorage` encapsula Preferences: armazenamento nativo no Android e fallback localStorage na web. Somente dados ficticios, sem garantia de criptografia. Dados podem ser apagados ao desinstalar ou limpar o aplicativo.
- Alvo Android: API minima 24; SDK de compilacao e alvo 36, conforme template Capacitor 8.5.3. Usar Android Studio compativel e seu JDK incorporado (21) para Gradle; nao usar automaticamente o JDK 25 global com Gradle 8.14.3.
- Backend compila para Java 21 e foi verificado com JDK 25. Maven Wrapper evita exigir Maven global.
- Sem JPA, banco, Swagger ou login nesta fase. Integraremos o banco do colega por meio da API depois de definir o contrato.

## Desenvolvimento web

Requisitos: Node 24 LTS e JDK compativel. Na raiz do VS Code, executar a tarefa **CASH: iniciar frontend e backend**. Ela abre dois terminais separados e inicia o Spring com perfil `local`.

Ou em dois terminais PowerShell:

```powershell
cd frontend
npm ci
Copy-Item .env.example .env.local
npm run dev
```

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Web: http://localhost:3000. API: http://localhost:8080. Saude: http://localhost:8080/actuator/health.

`NEXT_PUBLIC_API_URL` e publica e incorporada no build, nao um segredo. Alterar e reconstruir ao trocar de API. O antigo `BACKEND_URL` e o proxy `/api/backend` foram removidos.

## Android

Instalar Android Studio, SDK Platform 36, ferramentas de compilacao e emulador com imagem API 36. Configuracao sera guiada no chat em passos individuais. Abra `frontend/android` no Android Studio, nao a raiz do repositorio.

Para preparar assets de demonstracao:

```powershell
cd frontend
npm run android:sync:dev
npm run android:open
```

O build de demonstracao usa `http://10.0.2.2:8080`: no emulador Android, esse endereco representa o computador hospedeiro. Iniciar Spring com perfil `local`. `localhost` dentro do emulador aponta para o emulador, nao para o PC.

Executar variante **debug** no Android Studio. HTTP local e permitido somente no debug, para `10.0.2.2`, `localhost` e `127.0.0.1`. Release bloqueia HTTP. Nao foram desativadas validacoes de certificados.

Mudancas nas telas sem Live Reload: executar novamente `npm run android:sync:dev` e executar o aplicativo no Android Studio. No navegador com `npm run dev`, alteracoes aparecem automaticamente.

### Atualizacao ao vivo no emulador

Inicie a API com perfil `local` (reinicie depois de alterar configuracoes CORS). Em outro terminal, dentro de `frontend`, execute `npm run android:live`. O comando sincroniza a configuracao temporaria e inicia o Next na porta 3000. Execute **app / debug** uma vez no Android Studio para instalar a configuracao. Depois, salvar alteracoes nas telas atualiza o aplicativo pelo Fast Refresh, sem reconstruir o APK.

O emulador acessa a interface por `http://10.0.2.2:3000` e a API por `http://10.0.2.2:8080`. O servidor web fica vinculado ao loopback do PC; nao e necessario abrir firewall para a rede local. Nao iniciar `npm run dev` ao mesmo tempo, pois ambos usam porta 3000. Mudancas nativas e plugins continuam exigindo sincronizacao/recompilacao.

Mantenha o terminal aberto. Ctrl+C encerra o servidor e restaura o arquivo de configuracao para assets locais. A instalacao que ja esta no emulador continua apontando ao servidor ate executar novamente o app com assets locais. Se o terminal for encerrado a forca, execute `npm run android:sync:dev` para restaurar a configuracao. O build Android release recusa configuracao com URL de Live Reload.

Para aparelho fisico, e necessario um endereco acessivel para a API; preferir HTTPS. Nao usar `10.0.2.2`, que e especifico do emulador.

Build para API real:

```powershell
$env:ANDROID_API_URL='https://api.seu-dominio.example'
npm run android:sync
```

O comando recusa URL sem HTTPS. A URL acima e apenas exemplo, nao um servico existente. Depois, configurar assinatura e gerar APK/AAB no Android Studio. Nenhum release foi gerado ou publicado.

## Mobile e seguranca

- Viewport do dispositivo, zoom acessivel, areas seguras via variaveis Capacitor/CSS e altura dinamica.
- Botao Voltar nativo retorna pelo historico nas subpaginas e minimiza na raiz. Revisar junto aos futuros formularios para evitar perder alteracoes.
- `adjustResize` prepara a janela para teclado virtual. Validacao real de teclado, barras, rotacao e navegacao depende de emulador/aparelho.
- Sem backup automatico dos dados Android. Segredos, keystores, node_modules, assets copiados, builds e configuracoes locais do SDK ficam fora do Git.
- CORS local permite apenas `http://localhost:3000`, `http://127.0.0.1:3000`, `https://localhost` (origem do Capacitor Android) e `http://10.0.2.2:3000` (Live Reload no emulador). Fora do perfil local, definir politica de producao junto da autenticacao. CORS nao substitui autorizacao.

## Validacao

```powershell
cd frontend
npm run lint
npm run build
npm run android:sync:dev
npm audit --omit=dev
```

```powershell
cd backend
.\mvnw.cmd verify
```

Lint, exportacao estatica, sincronizacao dos plugins/assets e testes Spring foram verificados. APK ainda precisa ser compilado e executado apos instalar Android Studio/SDK.

Auditoria inicial: zero vulnerabilidades nas dependencias de producao. Oito alertas nas ferramentas de desenvolvimento (cinco altos na cadeia ESLint/braces e tres moderados na cadeia Capacitor CLI/xcode/uuid). Nao foi aplicado downgrade automatico nem `audit fix --force`; reavaliar atualizacoes compativeis.

Um unico Git na raiz; frontend e backend mantem dependencias, lockfile/wrappers, portas e ciclos de execucao separados. Nenhum envio ao GitHub foi realizado nesta adaptacao.
