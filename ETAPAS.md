# Próximas etapas do CASH Mobile

## Parte 1 — Início

- [x] Conectar os quatro indicadores às consultas correspondentes.
- [x] Calcular contas a pagar e pendências com a mesma base do Financeiro.
- [x] Acrescentar consulta de recebíveis fictícios, separada das receitas realizadas.
- [x] Calcular saldo demonstrativo com saldo inicial zero mais todas as entradas locais menos todas as saídas locais.
- [x] Calcular movimentado do mês como entradas mais saídas locais de outubro de 2026.
- [x] Exibir gráfico de seis meses (maio–outubro de 2026) com a mesma base local e tabela acessível.
- [x] Tratar carregamento, ausência de registros e falha de armazenamento sem apresentar falha como valor zero.
- [x] Atualizar checklist e registrar os limites da demonstração.

As partes de frontend abaixo registram a fase local anterior. No modo API implementado no backend 6, os dados de negócio vêm do Spring; detalhes em `frontend/INTEGRACAO.md`.

O período é fixado em outubro de 2026, identificado na tela. Contas são exemplos; gráfico e saldo usam o dispositivo. Nenhum recebível altera o saldo automaticamente. Não há integração bancária nem autenticação real.

## Parte 2 — Movimentações

- [x] Editar registros e anexos locais com validação.
- [x] Excluir registros com confirmação clara, lixeira restaurável e atualização das consultas.
- [x] Conferir consistência dos totais após alterações.

## Parte 3 — Android

- [x] Integrar abertura externa e compartilhamento de PDFs, com leitor e seletor conferidos no emulador.
- [x] Conferir as telas atuais no emulador, incluindo teclado, retorno e áreas seguras.
- [x] Gerar APK debug atualizado e verificar assets locais.
- [x] Instalar e validar o APK no emulador API 37, iniciado sem snapshot. O Launcher apresentou ANR na inicialização; o Cash respondeu durante a revisão.

Revisão registrada em `frontend/ANDROID-QA.md`. Corrigido o carregamento direto de rotas exportadas do Next no servidor local do Capacitor.

## Parte 4 — Backend

- Plano do backend dividido em seis partes em `backend/README.md`.
- [x] Backend 1: base, ambientes locais e Swagger acessível. Testes e build aprovados; execução GET pelo Swagger conferida.
- [x] Backend 2: login, expiração, logout e proteção por token, sem banco. Nove testes aprovados; login, consulta e logout conferidos no Swagger.
- [x] Backend 3: movimentações em memória, validação, filtros, paginação, lixeira e restauração. Quatorze testes e build aprovados; ciclo HTTP completo conferido e contrato disponível no Swagger.
- [x] Backend 4: indicadores, catálogos financeiros demonstrativos e anexos em memória. Dezenove testes e build aprovados; upload e indicadores executados pelo Swagger, contrato final conferido por HTTP.
- [x] Backend 5: perfil, preferências e leitura de notificações por conta, em memória. Vinte e quatro testes e build aprovados; perfil conferido no Swagger e alterações/leitura verificadas na instância final.
- [x] Validar conta de demonstração no Spring e proteger consultas.
- [x] Definir endpoints, erros e validações; documentar com OpenAPI/Swagger.
- [x] Backend 6: frontend e Android integrados à API, com autorização, erros explícitos e modo local-demo separado para Pages.
- [x] Integrar o frontend à API e testar autorização.

## Parte 5 — Integração

- [ ] Conectar ao banco fornecido pelo colega por meio do backend.
- [ ] Implementar troca de senha e notificações conforme o escopo demonstrativo definido.
- [ ] Validar contratos e permissões com a Escriba antes de integrar o legado.

Recuperação por e-mail e biometria estão fora do escopo demonstrativo, conforme solicitado em 28/10/2026.
