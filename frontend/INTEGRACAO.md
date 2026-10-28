# Integração — backend parte 6

Atualizado em 28/10/2026.

## Modos

`NEXT_PUBLIC_DATA_MODE=api` é o padrão. `NEXT_PUBLIC_API_URL=http://localhost:8080` atende o navegador local. O build Android força modo API e usa `http://10.0.2.2:8080` no emulador. O workflow do Pages define `local-demo` explicitamente: dados continuam no IndexedDB/Preferences, sem Spring. Não há fallback automático nem migração de dados antigos.

Variáveis `NEXT_PUBLIC_*` são públicas e incorporadas durante o build. Não colocar tokens ou segredos nelas. Alterar configuração exige reiniciar Next/reconstruir assets.

## Sessão

Login envia a conta pública `demo@cash.local / Cash123!` ao Spring. Token opaco permanece somente na memória JavaScript, com expiração de 30 minutos. Logout tenta revogar o token no servidor e encerra a sessão da interface mesmo se a conexão falhar. Em caso de falha nessa revogação, o token do servidor continua válido até expirar, embora seja descartado pelo cliente.

Recarregar a página, reiniciar o aplicativo ou atualizar módulos que forçam recarga exige novo login. Lembrar acesso salva somente o identificador. Resposta 401 invalida a sessão e retorna ao login. Bloqueio de rotas no cliente evita navegação indevida; segurança efetiva depende da autorização no Spring.

## Dados conectados

- Início: indicadores e gráfico consultam `/finance/summary`; movimentações recentes usam os registros da conta.
- Financeiro: contas, recebíveis, receitas e orçamento vêm dos catálogos da API. Filtros da interface operam sobre as páginas carregadas. Referência fictícia permanece em 22/10/2026.
- Movimentações: cadastro, edição, lixeira e restauração usam API. IDs são definidos no servidor. Se o lançamento salvar mas upload falhar, o formulário conserva o ID para repetir a atualização sem criar outro registro.
- Documentos: listagem carrega metadados; conteúdo é baixado com Bearer somente ao abrir ou compartilhar. Links de arquivo usam Blob temporário, revogado ao sair do detalhe. Limite: 5 MiB por anexo.
- Perfil: dados pessoais e preferências são consultas/alterações distintas. Se preferências falharem depois dos dados pessoais, o aviso informa sucesso parcial. E-mail de contato não altera o login.
- Notificações: lista, contador, leitura individual e leitura de todos são persistidos no servidor por conta. Detalhes mostram o alerta selecionado e a conta correspondente. Marcar como lida é ação explícita.
- Pesquisa: usa os registros e catálogos do modo selecionado; links abrem detalhes correspondentes.

Erro de conexão não é apresentado como resultado vazio ou valor zero. Erros de API recebem aviso e opção de repetir nas consultas principais. Valores inteiros fora da precisão segura de JavaScript são rejeitados, evitando totais incorretos.

## Limites

Dados de negócio, anexos e sessões ficam em memória no Spring e somem ao reiniciar. Sessões da mesma conta pública compartilham dados de teste. Banco, contratos com Escriba, pagamentos, envio de e-mail, biometria e push não foram integrados. Troca de senha permanece apenas simulação identificada.

Não existe transação única entre salvar lançamento e enviar anexo, nem entre perfil e preferências. A interface informa sucesso parcial. Uma perda de conexão durante cadastro pode deixar resultado incerto; consulte os registros antes de repetir manualmente. Não há repetição automática de escritas.

## Validação realizada

- `npm run lint`, `npm run test:api`, exportação API/Android e exportação local-demo com base path do Pages.
- 24 testes Spring aprovados. Os testes dos adaptadores cobrem Bearer, erros, resposta 401, precisão numérica, paginação, sucesso parcial no upload, repetição sem cadastro duplicado e download sob demanda.
- Navegador: senha incorreta rejeitada; login público aceito; cadastro de R$ 125,50 com PNG; edição para R$ 150,00 preservando arquivo; lixeira e restauração; download autenticado; totais recalculados; receitas e orçamento; perfil e preferências; leitura de alerta e conta de 23/10/2026; logout e rota sem sessão.
- Backend desligado: aviso de falha sem catálogo local. Backend reiniciado: token antigo recebeu 401 e interface voltou ao login. Reinício também limpou os dados fictícios utilizados na revisão.
- Início conferido em 320, 375 e 430 px, sem transbordamento horizontal. Não representa nova auditoria visual completa de todas as telas.
- APK debug compilado e instalado no emulador API 37. Login pela API mostrou nome de perfil alterado no PC, alerta lido e total de R$ 150,00. Launcher e sistema apresentaram avisos de lentidão durante a inicialização; após aguardar, o Cash respondeu. Não testado em aparelho físico nesta etapa.

Comandos:

```powershell
npm run lint
npm run test:api
npm run android:sync:dev
cd android
.\gradlew.bat :app:assembleDebug --offline --console=plain
```

Artefatos locais em `D:\Projetos\Ferramentas\Codex\CashMobile`: logs dos builds/testes e capturas em `outputs/`. APK de teste: `outputs/CASH-Mobile-teste.apk`. O workflow do Pages publica somente o modo local-demo. O código Spring fica no repositório, mas a API não é hospedada pelo Pages.
