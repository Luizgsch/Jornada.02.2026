# Documentos no Android

O plugin local `CashDocuments`, registrado na MainActivity, implementa abertura externa (`ACTION_VIEW`) e compartilhamento (`ACTION_SEND`) para PDF, PNG e JPEG. O frontend lê o Blob local e envia seu conteúdo para a ponte Capacitor somente após tocar na ação correspondente.

Arquivos não vazios de até 5 MB são copiados para `cache/shared-documents` com nome sanitizado e prefixo aleatório. FileProvider publica somente esse diretório. O aplicativo de destino recebe uma URI content:// com permissão temporária de leitura; não há caminho externo arbitrário nem permissão ampla de armazenamento. Cópias temporárias com mais de 24 horas são removidas na próxima preparação de documento. O original permanece no IndexedDB.

Compartilhar abre a folha do Android; o usuário escolhe e confirma o envio no aplicativo de destino. Não existe envio automático. Abrir exige leitor compatível instalado; sua ausência retorna uma mensagem recuperável. No navegador, o download existente continua disponível.

## Build e estado da revisão

- Lint e exportação estática Android passaram.
- Compilação Java nativa e geração do APK debug passaram.
- Tarefa de testes unitários Gradle executada; isso não substitui teste do seletor/leitor em aparelho.
- Verificação dos assets de release passou: sem server.url/localhost persistido.
- APK de demonstração usa assets locais e configuração de desenvolvimento da API (10.0.2.2:8080), sem serviço real de produção. Não é um release assinado para distribuição na loja.

Na revisão de 27/10/2026, o emulador API 37 iniciou sem snapshot e recebeu o APK. O botão de documentos abriu um PDF fictício com texto renderizado no leitor do Android e acionou a folha de compartilhamento, sem selecionar destinatário ou enviar arquivo. Teclado na edição, retorno e telas atuais foram conferidos. O Launcher apresentou ANR durante a inicialização; o aviso foi fechado e a revisão do Cash prosseguiu. Testes em aparelho físico e outros leitores continuam recomendados. Detalhes em `ANDROID-QA.md`.
