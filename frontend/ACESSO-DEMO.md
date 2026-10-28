# Acesso demonstrativo

Usuário público: **demo@cash.local**. Senha pública: **Cash123!**. A tela permite preencher esses dados automaticamente. Não use credenciais pessoais.

No modo API, Spring valida o login e autoriza as consultas com token temporário de 30 minutos. Token fica somente na memória JavaScript e é revogado ao sair; atualizar a página ou reiniciar o aplicativo exige entrar novamente. Preferências de lembrar acesso armazenam somente o identificador.

No Pages, o modo explícito local-demo oferece navegação demonstrativa e exige o mesmo par público na interface, sem autenticação segura de servidor.

No modo API, registros, anexos, perfil e notificações ficam na memória do backend e são compartilhados entre sessões da conta pública. Reiniciar Spring apaga esses dados. No local-demo, dados ficam no dispositivo. Não há migração automática entre modos.

Recuperação por e-mail e biometria estão fora do escopo. Troca de senha no perfil é simulação identificada e não modifica a senha pública.
