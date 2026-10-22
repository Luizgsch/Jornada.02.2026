# CASH Mobile

Projeto acadêmico de Engenharia de Software: extensão mobile do Cash, com frontend Next.js/Capacitor e API Spring Boot. Público: responsáveis financeiros e gestores de cartórios. Integração real com Escriba ainda precisa ser validada.

## Escopo atual autorizado

Reproduzir o Figma existente, tela por tela. **Início** é o login; **/menu** é o painel financeiro. Conferir referência e localhost entre etapas. Entrar abre o painel demonstrativo após validar preenchimento, sem autenticar. Autenticação padrão no Spring será integrada depois. Não simular recuperação enviada ou biometria configurada.

## Restrições

- Android e navegador compartilham a interface. Exportação estática deve continuar funcionando.
- Campos acessíveis, alvos de toque adequados, áreas seguras, zoom e teclado virtual.
- Somente dados fictícios. Não armazenar senha nem token em Preferences/localStorage.
- Fidelidade à referência sem copiar cortes de texto ou erros de escrita.
- Sem alegações de segurança ainda não implementadas.
