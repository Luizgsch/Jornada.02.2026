# Reprodução do Figma

## Sequência

- [x] Início (login) — reprodução visual concluída; autenticação pendente.
- [ ] /menu — painel financeiro.
- [ ] /movimentacao — nova movimentação.
- [ ] /notificações — lista.
- [ ] /notificações-2 — detalhe.

## Início

- [x] Inspecionar Figma ao vivo e medir fonte, cores e espaçamentos.
- [x] Reproduzir marca, título, descrição e campos.
- [x] Conferir primeira etapa no localhost.
- [x] Completar opções, ações, rodapé e estados acessíveis.
- [x] Conferir resultado mobile e desktop e corrigir diferenças.
- [x] Validar lint e exportação estática.

Verificado em larguras de 320 e 375 px e na janela desktop. Sem rolagem horizontal. Campos com 50 px, ações com pelo menos 44 px. Usuário lembrado reaparece após recarregar; senha permanece vazia. Dados de teste removidos. Recuperação e biometria mostram avisos honestos.

## Limites funcionais

Entrar deve validar os campos e informar que autenticação está pendente. Recuperação e biometria devem explicar sua indisponibilidade. Lembrar acesso guarda somente o identificador de demonstração. Nenhuma senha é persistida. Integração de autenticação no Spring não faz parte desta reprodução visual inicial.
