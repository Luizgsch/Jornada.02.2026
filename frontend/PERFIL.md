> Atualização de 28/10/2026: modo API integrado ao Spring; armazenamento local descrito abaixo vale para local-demo. Estado atual e limites em [INTEGRACAO.md](INTEGRACAO.md). Recuperação por e-mail e biometria estão fora do escopo.

# Perfil demonstrativo

`/perfil/` reúne dados fictícios editáveis: nome completo, e-mail, telefone opcional e cartório. O perfil de acesso é um exemplo de responsável financeiro, não uma permissão real. A navegação Perfil e o avatar do painel abrem essa página.

Dados são validados e salvos em Preferences pela chave `cash-demo:profile`. O contexto compartilhado atualiza nome e iniciais do painel. Preferências implementadas: ocultar saldo inicialmente no Início e reduzir animações/transições no aplicativo. O valor bancário continua não informado nesta demonstração.

A seção Segurança oferece troca de senha demonstrativa com senha atual fictícia, nova senha de pelo menos oito caracteres, confirmação e rejeição de repetição da senha atual. Não há autenticação, comparação com credencial real ou armazenamento de senha. Biometria é identificada como implementação futura. Sair retorna ao login e preserva os registros locais.

Verificação: edição de nome/e-mail, persistência após recarga, atualização de saudação e iniciais, preferência de saldo, redução de transição para 0s, divergência de confirmação e sucesso da simulação de senha. Inspeção em desktop e 320/375/430 px sem overflow horizontal. Detector sem achados e lint/build estático com prefixo do Pages. Revisão visual feita no próprio fluxo, sem revisor independente. Sistema visual existente preservado.
