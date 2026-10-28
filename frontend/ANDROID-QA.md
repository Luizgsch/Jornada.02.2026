# Revisão Android — 27/10/2026

APK debug com assets locais, emulador Medium Phone API 37, resolução 1080 × 2400 e viewport WebView de 411 CSS px. Inicialização sem snapshot, renderização SwiftShader, 2 GB de RAM e dois núcleos. Houve ANR do Pixel Launcher na inicialização; o Cash respondeu após fechar o aviso. Não houve validação em aparelho físico.

## Conferido

- Login demonstrativo e entrada pelo botão Entrar.
- Início, Financeiro e consultas de pendências, recebíveis, movimentações e conta.
- Abas Receitas e Orçamento.
- Documentos, notificações, detalhe de notificação, perfil, pesquisa e recuperação demonstrativa.
- Nova movimentação, edição com registro fictício, estado de registro inexistente e lixeira vazia.
- As 15 rotas internas carregaram com seus títulos e sem transbordamento horizontal na largura do emulador. Capturas visuais conferidas; barras do sistema e navegação não encobriram os cabeçalhos.
- Teclado virtual na descrição da edição: campo visível; primeiro Voltar fecha o teclado sem abandonar a página. Configuração de exibição de teclado com teclado físico restaurada ao valor anterior após o teste.
- Voltar do Android retorna de Nova movimentação ao Início.
- Documento fictício associado a registro local: botão de abertura entrega o PDF ao leitor, que renderiza seu texto. Botão de compartilhamento abre o seletor com o arquivo. Nenhum destinatário foi selecionado e nenhum arquivo foi enviado.
- Registro de teste de R$ 123,45 refletiu nos totais e documentos. Fixture criada diretamente no IndexedDB para isolar a ponte nativa; isso não constitui teste de seleção de arquivo pelo formulário.

## Correção encontrada na revisão

O fallback SPA do Capacitor devolvia o HTML do login ao carregar uma rota interna diretamente. A MainActivity agora entrega o `index.html` exportado da rota para navegações GET locais do documento principal, mantendo a injeção da ponte Capacitor. Assets, chamadas externas e live reload seguem o cliente padrão. A correção foi conferida no APK final em todas as rotas internas.

## Validação e limites

Lint e exportação Next passaram. `:app:assembleDebug --offline` passou e o APK final foi reinstalado. A tarefa genérica `assembleDebug` também tentou montar bibliotecas e falhou por ausência de `lint-gradle:31.13.0` no cache offline; a tarefa específica do aplicativo não depende dessa tarefa de publicação de biblioteca. Não foi necessário baixar novos componentes.

Configuração empacotada sem `server.url`, usando assets locais; API continua demonstrativa. O teste não comprova autenticação, integração com banco, envio de e-mail, push, pagamentos ou compatibilidade com todos os leitores. Não foi realizada comparação visual nova com o Figma nem uma matriz de aparelhos físicos, orientações e tamanhos de fonte.

Capturas e inventário de rotas: `D:\Projetos\Ferramentas\Codex\CashMobile\qa-android`. APK: `D:\Projetos\Ferramentas\Codex\CashMobile\outputs\CASH-Mobile-teste.apk`.

SHA-256 do APK: `1A7B9507FDA91BFAF80CFE2830F57F364C8BBFE0AA1233C99E4C4F22BAEDCB8D`.
