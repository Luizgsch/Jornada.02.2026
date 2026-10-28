# CASH Mobile

Projeto acadêmico com frontend Next.js exportado, Android com Capacitor e API Spring Boot. As seis partes do backend estão implementadas, incluindo integração das telas com a API. Banco de dados e integração com o Cash da Escriba continuam pendentes.

## Executar no computador

Na raiz do VS Code, execute a tarefa **CASH: iniciar frontend e backend**. Os dois processos usam terminais separados. Ou execute, em dois terminais:

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

[Aplicativo](http://localhost:3000/) · [Swagger](http://localhost:8080/swagger-ui/index.html) · [Saúde](http://localhost:8080/actuator/health).

Conta pública de teste: **demo@cash.local / Cash123!**. Não use dados reais.

## Modos de dados

- **API**, padrão no localhost e Android: login validado no Spring, sessão temporária, movimentações e anexos na memória do backend, perfil e notificações compartilhados entre sessões da mesma conta. Reiniciar o backend apaga dados e sessões.
- **local-demo**, definido explicitamente no workflow do Pages: demonstração independente com IndexedDB e Preferences. Não se conecta ao Spring e não representa autenticação segura. Dados locais antigos são preservados e não são enviados automaticamente à API.

`NEXT_PUBLIC_DATA_MODE=api|local-demo` e `NEXT_PUBLIC_API_URL` são incorporados no build. Alterações exigem reiniciar o servidor Next ou reconstruir o aplicativo. Erros da API nunca ativam uma demonstração local automaticamente.

Token permanece somente na memória JavaScript, expira em 30 minutos e é revogado no logout. Recarregar a página ou reiniciar o aplicativo exige novo login. “Lembrar acesso” guarda somente o identificador público. As rotas da interface têm bloqueio de navegação; a autorização efetiva é aplicada pelo Spring em cada consulta.

Documentação de integração e validação: [frontend/INTEGRACAO.md](frontend/INTEGRACAO.md). Contratos e limites: [backend/README.md](backend/README.md). Progresso: [ETAPAS.md](ETAPAS.md).

## Estrutura e práticas

Um Git na raiz, com `frontend/` e `backend/` independentes para dependências, builds e execução. Next usa porta 3000; Spring, 8080. O aplicativo acessa o banco somente por meio do backend. Repositórios em memória no Spring poderão ser substituídos por implementações de banco.

Next usa `output: export`, sem Server Actions ou rotas de API Next. Detalhes de registros usam rotas estáticas com identificador no fragmento. Android carrega assets locais; consultas exigem API acessível. Não existe sincronização offline.

Caches, SDK, Maven, Gradle, temporários e artefatos de desenvolvimento deste ambiente ficam em `D:\Projetos\Ferramentas`. As tarefas do VS Code já fornecem os caminhos. Node 24 e Java 21 são as versões usadas na validação.

## GitHub Pages

[Demo pública](https://luizgsch.github.io/Jornada.02.2026/). O workflow aplica `PAGES_BASE_PATH=/Jornada.02.2026` e `NEXT_PUBLIC_DATA_MODE=local-demo`. Pages hospeda somente arquivos estáticos; não hospeda Spring, banco ou Swagger. A publicação do frontend mantém esse modo independente; a API Spring continua local.

Recuperação por e-mail e biometria estão fora do escopo. A troca de senha existente é apenas simulação, sem alterar a credencial pública.

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

Lint, exportacao estatica, sincronizacao dos plugins/assets e testes Spring foram verificados. APK debug compilado, instalado e conectado à API no emulador API 37. Login, perfil compartilhado, contador de alertas e indicador de movimentações conferidos.

Auditoria inicial: zero vulnerabilidades nas dependencias de producao. Oito alertas nas ferramentas de desenvolvimento (cinco altos na cadeia ESLint/braces e tres moderados na cadeia Capacitor CLI/xcode/uuid). Nao foi aplicado downgrade automatico nem `audit fix --force`; reavaliar atualizacoes compativeis.

Um unico Git na raiz; frontend e backend mantem dependencias, lockfile/wrappers, portas e ciclos de execucao separados. Nenhum envio ao GitHub foi realizado nesta adaptacao.
