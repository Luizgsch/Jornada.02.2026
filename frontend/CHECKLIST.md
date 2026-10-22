# Reprodução do Figma

## Sequência

- [x] Início (login) — reprodução visual concluída; autenticação pendente.
- [x] /menu — painel financeiro demonstrativo.
- [x] /movimentacao — nova movimentação e salvamento local.
- [x] /notificações — lista, em `/notificacoes/` no aplicativo.
- [x] /notificações-2 — detalhe do primeiro alerta, em `/notificacoes/detalhe/`.

## Início

- [x] Inspecionar Figma ao vivo e medir fonte, cores e espaçamentos.
- [x] Reproduzir marca, título, descrição e campos.
- [x] Conferir primeira etapa no localhost.
- [x] Completar opções, ações, rodapé e estados acessíveis.
- [x] Conferir resultado mobile e desktop e corrigir diferenças.
- [x] Validar lint e exportação estática.

Verificado em larguras de 320 e 375 px e na janela desktop. Sem rolagem horizontal. Campos com 50 px, ações com pelo menos 44 px. Usuário lembrado reaparece após recarregar; senha permanece vazia. Dados de teste removidos. Recuperação e biometria mostram avisos honestos.

## Limites funcionais

Entrar valida os campos e abre `/menu/` no modo demonstrativo. Não existe autenticação real nem proteção de rota nesta fase. Recuperação e biometria explicam sua indisponibilidade. Lembrar acesso guarda somente o identificador de demonstração. Nenhuma senha é persistida. Integração de autenticação no Spring será uma etapa própria.

## Menu

- [x] Reabrir Figma e medir cabeçalho, saldo, indicadores e resumo financeiro.
- [x] Navegar do login ao Menu após preencher campos.
- [x] Reproduzir cabeçalho, avatar, pesquisa, notificações e saldo oculto.
- [x] Reproduzir indicadores com valores fictícios da referência.
- [x] Reproduzir área de gráfico, legenda e meses sem inventar séries.
- [x] Adicionar movimentações recentes com estado vazio explícito.
- [x] Fixar navegação e botão Nova movimentação com espaço para rolagem.
- [x] Adicionar avisos às ações de telas ainda não implementadas.
- [x] Adicionar saída da demonstração no perfil.
- [x] Conferir adaptação mobile, navegação e build final.

Conferência no localhost: larguras de 320 e 375 px sem overflow horizontal; barra inferior fixada; login → Menu e perfil → login funcionando. Saldo alterna entre oculto e não informado. Avisos das ações futuras abrem e fecham. Lint e exportação estática aprovados.

O Figma não informa saldo numérico nem desenha séries no gráfico. Mostrar saldo revela “Não informado”. A data, saudação e quatro indicadores são exemplos do protótipo. Área recente não fabrica movimentações. Pesquisa, áreas Financeiro/Documentos, detalhamentos e cadastro de movimentação aguardam suas próximas etapas. Sino e Alertas agora abrem notificações.

## Notificações

- [x] Inspecionar lista e detalhe no Figma ao vivo.
- [x] Conectar sino e Alertas à lista.
- [x] Reproduzir os quatro exemplos fornecidos, sem novos eventos fictícios.
- [x] Implementar filtros Todas, Financeiras e Sistema.
- [x] Abrir o primeiro alerta no detalhe, conforme ligação existente no protótipo.
- [x] Marcar o primeiro alerta como lido somente ao abrir o detalhe.
- [x] Persistir leitura localmente e compartilhar contador com sino e navegação.
- [x] Voltar do detalhe à lista e da lista ao Menu.
- [x] Mostrar aviso honesto ao clicar em Ver conta, cuja tela ainda não existe.
- [x] Conferir larguras reais de 320 e 375 px sem overflow horizontal.
- [x] Verificar filtros (2 itens por categoria), leitura persistida após reload e contador 2 → 1.
- [x] Validar lint e build final com caminho do Pages; lista e detalhe exportados.

Os demais três cartões permanecem estáticos, como na referência: não foram inventadas telas de detalhe. Todos os textos, valores e datas são exemplos históricos do Figma, sem consulta à API ou notificações push. Inicialmente dois avisos são não lidos; estado muda conforme uso local.

## Movimentação

- [x] Inspecionar formulário no Figma: campos, seletor de tipo, anexo e ações.
- [x] Conectar Nova movimentação do Menu à rota `/movimentacao/`.
- [x] Reproduzir formulário e retorno/cancelamento ao Menu.
- [x] Validar descrição, valor positivo em centavos, data e categoria.
- [x] Oferecer categorias de demonstração e Conta principal, pois opções reais não constam no protótipo.
- [x] Aceitar PDF, PNG ou JPG não vazio, até 5 MB, com troca e remoção antes de salvar.
- [x] Guardar registro e arquivo juntos no IndexedDB local, sem upload.
- [x] Mostrar sucesso apenas após concluir gravação; preservar formulário em caso de falha.
- [x] Mostrar registros locais no Menu; Ver todas expande a lista.
- [x] Testar valor brasileiro, zero, negativos, precisão e limites.
- [x] Testar gravação com anexo e persistência após recarregar.
- [x] Conferir mobile em 320 e 375 px sem overflow; lint e exportação estática aprovados.

Foi criado no navegador de teste um registro identificado como “Teste de demonstração”, R$ 125,50, com `anexo-demonstracao.png` (imagem do próprio protótipo). Não representa operação real. Totais do painel permanecem exemplos independentes do Figma; cálculo agregado e integração financeira dependem do contrato da API. Consulta/download de anexos salvos e edição/exclusão de registros ainda não fazem parte das telas fornecidas.
