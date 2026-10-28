> Atualização de 28/10/2026: modo API integrado ao Spring; armazenamento local descrito abaixo vale para local-demo. Estado atual e limites em [INTEGRACAO.md](INTEGRACAO.md). Recuperação por e-mail e biometria estão fora do escopo.

# Financeiro: consulta gerencial demonstrativa

Base: documento técnico consolidado Cash–Escriba fornecido pelo usuário, especialmente seções 2.1, 2.4 e 5.2. A interface amplia o aplicativo existente sem alterar sua identidade visual. Não existe tela Financeiro definida no Figma inspecionado.

## Conteúdo implementado

- Contas a pagar: totais de compromissos em aberto e vencidos, pesquisa por descrição/fornecedor, situação e detalhe com fornecedor, categoria, vencimento e conta.
- Movimentações: leitura do IndexedDB existente, entradas, saídas, resultado do período, pesquisa, tipo, detalhes e acesso ao cadastro demonstrativo já existente.
- Receitas: totais por setor ou funcionário e filtro de setor.
- Orçamento: comparação mensal de despesas previstas e realizadas, diferença total e por categoria.
- Período mensal comum, aplicado explicitamente pelo formulário nativo. Exemplos de consulta em outubro de 2026, posição de 22/10/2026. Outros períodos não herdam os valores de exemplo.

## Limites

Contas, receitas e orçamento são fixtures fictícias em `src/lib/financial.ts`. Movimentações são registros locais do usuário em `src/lib/movements.ts`, separados desses exemplos. O resultado local é entradas menos saídas, não saldo bancário. O Início agora usa as mesmas contas e recebíveis de exemplo, enquanto movimentado, saldo e gráfico são calculados dos registros locais. O saldo inicial demonstrativo é zero; contas a pagar/receber não alteram o saldo automaticamente. O período demonstrativo é outubro de 2026.

Não há execução de pagamentos, baixa de títulos, fechamento de caixa, autorização por perfil, auditoria, emissão fiscal, integração bancária ou consulta ao legado. O documento recomenda começar por consulta gerencial; essas operações não foram adicionadas. A integração real, os catálogos, regras de situação, perfis e contratos de API precisam ser validados com a Escriba. O login atual permanece demonstrativo.

## Visual e verificação

Sistema preservado: Inter, amarelo #FDC700, texto #0A0A0A, fundo #F5F5F4, coluna de até 384 px, campos de 50 px, navegação inferior compartilhada, cartões de 12–16 px e áreas seguras.

Verificado no navegador: filtro de vencidos, detalhe de conta, busca por software, receita por funcionário, filtro Notas (R$ 18.000,00), orçamento (R$ 6.300,00 previsto / R$ 6.130,00 realizado), movimentação persistida (R$ 125,50), estado vazio de saídas e troca de outubro para novembro e retorno. Telas inspecionadas em desktop e 320, 375 e 430 px, sem overflow horizontal. Alvos interativos conferidos com mínimo de 44 px. Detector mecânico sem achados. Revisão visual feita no próprio fluxo, sem revisor independente.
