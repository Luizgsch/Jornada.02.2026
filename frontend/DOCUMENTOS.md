# Documentos demonstrativos

A rota `/documentos/` reúne os anexos já salvos nas movimentações locais. Mantém Inter, amarelo #FDC700, fundo #F5F5F4, coluna de até 384 px, raios de 12–16 px e navegação compartilhada. Não há outra base de arquivos nem dados fictícios inseridos automaticamente.

## Recursos

- Pesquisa por nome de arquivo, descrição da movimentação ou categoria.
- Filtros PDF/imagens e mês da movimentação, com aplicação e limpeza explícitas.
- Nome, tipo, tamanho, data e movimentação vinculada na listagem.
- Detalhe com prévia de PNG/JPG, metadados financeiros e nome original do arquivo para download no navegador.
- PDFs são baixados para o leitor do usuário; não dependem de plugins de prévia do navegador.
- Cadastro usa o fluxo existente de nova movimentação e seu limite de PDF/PNG/JPG de até 5 MB.
- Estados de carregamento, erro com nova tentativa, lista vazia e busca sem resultado.

## Limites e integração

Não há upload, sincronização, emissão de relatórios, documentos fiscais, auditoria ou acesso ao legado. Arquivos ficam no IndexedDB deste navegador/dispositivo. O documento técnico menciona relatórios e prestação de contas, mas não define um catálogo documental ou um contrato de anexos. Esses pontos precisam ser validados com a Escriba.

Android: imagens têm prévia local. Download, abertura externa e compartilhamento de PDF exigem integração nativa futura, explicitada na tela. O aplicativo não apresenta um botão de download web que simule suporte nativo.

## Verificação

Lint e exportação estática com prefixo do Pages. Busca sem resultado, filtro PDF, limpeza de filtros, mês sem dados e retorno, prévia de imagem com dimensão natural confirmada, metadados do lançamento e download real de PDF fictício pelo navegador. Verificação em desktop e 320/375/430 px, sem overflow horizontal e com controles de pelo menos 44 px. Detector sem achados. Revisão visual no próprio fluxo, sem revisor independente.
