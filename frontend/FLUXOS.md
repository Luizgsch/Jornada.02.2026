> Atualização de 28/10/2026: modo API integrado ao Spring; armazenamento local descrito abaixo vale para local-demo. Estado atual e limites em [INTEGRACAO.md](INTEGRACAO.md). Recuperação por e-mail e biometria estão fora do escopo.

# Consulta de conta, pesquisa e recuperação demonstrativa

- `/financeiro/conta/`: abre pelo botão Ver conta da notificação. Preserva aluguel de R$ 4.200,00 e vencimento em 15/06/2025 do Figma; não confunde essa fixture com as contas de outubro de 2026. Não permite pagamento nem inventa fornecedor/histórico.
- `/pesquisa/`: pesquisa sem distinção de acento/maiúsculas em contas demonstrativas, alertas do protótipo, movimentações e anexos locais. Filtra por categoria e informa resultados vazios/carregamento/falha local. Resultados abrem a área correspondente; documentos abrem o anexo selecionado. Movimentações usam rota própria do Financeiro e selecionam o mês do registro encontrado.
- `/recuperar-senha/`: fluxo demonstrativo autorizado pelo usuário: valida e-mail, simula abertura do link e valida nova senha fictícia de pelo menos oito caracteres e confirmação correspondente. Não envia e-mails, não consulta existência de conta, não emite token nem altera/salva senhas. O formulário é limpo ao avançar. Integração real depende de autenticação e provedor de e-mail no backend.
- Biometria: apresenta “A biometria será implementada em breve.”

As novas telas preservam o sistema visual existente. Verificados: ligação da notificação à conta correta, aviso da biometria, divergência de senhas e conclusão da simulação, pesquisa por aluguel, busca sem acentos e abertura do PDF local pelo resultado. Telas móveis sem overflow horizontal. Revisão visual no próprio fluxo, sem revisor independente. Lint e build estático com prefixo do Pages fazem parte da validação.
