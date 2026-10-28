# Checklist atual do CASH Mobile

## Telas e navegação

- [x] Login responsivo com exemplos públicos de acesso e preenchimento rápido.
- [x] Início, indicadores, gráfico conectado ao modo de dados e movimentações recentes.
- [x] Financeiro: contas a pagar, recebíveis, movimentações, receitas e orçamento.
- [x] Notificações, leitura na API ou local-demo, detalhe e consulta da conta do alerta.
- [x] Documentos: pesquisa, filtros, prévia de imagens e download no navegador.
- [x] Pesquisa global nos dados do modo selecionado.
- [x] Troca de senha demonstrativa, sem alteração de credenciais; recuperação fora do escopo.
- [x] Perfil editável e preferências conectados à API.
- [x] Biometria identificada como fora do escopo.

## Parte 1 — Início

- [x] A pagar: R$ 5.240,00, soma das três contas não pagas de outubro de 2026.
- [x] A receber: R$ 26.500,00 em duas contas fictícias, consultáveis separadamente.
- [x] Pendências: as mesmas três contas não pagas, incluindo vencidas.
- [x] Movimentado: entradas mais saídas locais de outubro de 2026.
- [x] Saldo demonstrativo: saldo inicial zero mais entradas menos saídas de todos os registros locais.
- [x] Gráfico de maio a outubro de 2026, com entradas/saídas locais e tabela acessível.
- [x] Carregamento, vazio e erro explícitos; tentativa de recarregar dados locais.
- [x] Valores iniciais sem lançamentos são zero, não uma série financeira inventada.

## Limites

Login API validado pelo Spring, token temporário em memória, rotas protegidas e logout com revogação. Movimentações e anexos usam backend no modo API; IndexedDB permanece somente no modo explícito local-demo do Pages. Notificações e contas compartilham referência de outubro de 2026. Sem banco, legado, recuperação por e-mail, biometria, push ou pagamentos. Nenhuma senha é armazenada pelo frontend.

Android: abertura de PDF no leitor e compartilhamento pelo seletor foram conferidos no emulador. Telas atuais, teclado na edição e retorno entre páginas foram revisados. Corrigido o carregamento direto/recarregamento de rotas no APK. O Launcher do emulador apresentou ANR na inicialização; a revisão do Cash prosseguiu após fechar o aviso. Não substitui testes em aparelho físico. Evidências e limites em `ANDROID-QA.md`.

Plano de execução: `../ETAPAS.md`. Detalhes por área: `FINANCEIRO.md`, `DOCUMENTOS.md`, `FLUXOS.md`, `PERFIL.md` e `ACESSO-DEMO.md`.
