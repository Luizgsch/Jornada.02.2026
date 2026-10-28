# CASH Mobile — backend

API acadêmica em Java 21 e Spring Boot 4.1.1. O backend mantém sessões, movimentações, anexos, perfil, preferências e leitura de notificações em memória. Frontend ainda independente: sua conexão aos endpoints de negócio acontece na parte 6. Não há banco nem sincronização de dados do dispositivo nesta etapa.

## Parte 1: base e documentação

- `config/`: configuração do OpenAPI e CORS do ambiente local.
- `system/`: endpoint público de status, sem dados pessoais.
- `auth/`: conta demonstrativa, emissão/validação/revogação de sessões e endpoints de autenticação.
- `movement/`: contrato HTTP, regras de negócio e armazenamento em memória atrás da interface `MovementRepository`.
- `finance/`: indicadores calculados e catálogos fictícios somente para consulta.
- `document/`: validação de upload, listagem de metadados e download autenticado.
- `profile/`: dados fictícios e preferências, com repositório separado.
- `notification/`: catálogo demonstrativo e estado de leitura por proprietário.
- `application.properties`: configuração comum; documentação desligada por padrão, saúde sem detalhes internos e respostas de erro do Spring sem stack trace.
- `application-local.properties`: Swagger e origens locais para navegador e WebView Android.
- Endpoints versionados em `/api/v1`. Novos módulos serão organizados por funcionalidade, conforme forem implementados, sem misturar controllers, regras de negócio e armazenamento.

Foi adotado springdoc-openapi 3.2.0, compatível com Spring Boot 4.1 segundo a [matriz oficial](https://springdoc.org/faq.html). Swagger documenta apenas endpoints existentes. Autenticação usa Spring Security com [requisições sem sessão HTTP](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/session-management.html); as sessões da demonstração são tokens opacos guardados em memória.

## Executar no Windows

Com Java 21 configurado, na pasta `backend`:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

No VS Code, a tarefa **Backend: desenvolvimento** executa esse comando. **CASH: iniciar frontend e backend** inicia ambos. As tarefas do workspace já direcionam ferramentas e caches para o D: nesta máquina.

No terminal, Maven usa por padrão `~/.m2/repository`. Nesta máquina, `~/.m2` está redirecionado para `D:\Projetos\Ferramentas\Maven`. Para determinar o repositório explicitamente:

```powershell
.\mvnw.cmd "-Dmaven.repo.local=D:/Projetos/Ferramentas/Maven/repository" test
```

Não configure esses caminhos pessoais no `pom.xml` nem versione credenciais. O projeto permanece portável para o colega.

## Acessos locais

| Recurso | Endereço no PC |
| --- | --- |
| Swagger interativo | http://localhost:8080/swagger-ui/index.html |
| Atalho do Swagger | http://localhost:8080/swagger-ui.html |
| Contrato OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Contrato OpenAPI YAML | http://localhost:8080/v3/api-docs.yaml |
| Estado da demonstração | http://localhost:8080/api/v1/status |
| Disponibilidade | http://localhost:8080/actuator/health |

No Swagger, abra **Sistema → GET /api/v1/status → Try it out → Execute**. O retorno informa `authenticationEnabled: true` e `storage: memoria`: autenticação e movimentações disponíveis no backend, sem integração bancária.

## Parte 2: login e sessão

| Método | Endpoint | Acesso e resultado |
| --- | --- | --- |
| POST | `/api/v1/auth/login` | Público; valida e-mail/senha e retorna token, expiração e usuário fictício |
| GET | `/api/v1/auth/me` | Bearer obrigatório; consulta usuário e expiração, sem devolver o token |
| POST | `/api/v1/auth/logout` | Bearer obrigatório; revoga apenas a sessão atual e responde 204 |

Credencial pública: **demo@cash.local / Cash123!**. Não use dados pessoais. O corpo do login é:

```json
{"email":"demo@cash.local","password":"Cash123!"}
```

Para testar pelo Swagger:

1. Execute **Autenticação → POST /api/v1/auth/login** com os dados acima.
2. Copie `accessToken` do retorno. Clique em **Authorize** e cole somente o token, sem `Bearer`.
3. Execute **GET /api/v1/auth/me**: deve retornar 200.
4. Execute **POST /api/v1/auth/logout**: deve retornar 204.
5. Execute `/me` novamente com o mesmo token: deve retornar 401. Limpe o token no Authorize antes de fazer outro login.

Tokens aleatórios de 256 bits, sem conteúdo de usuário embutido, com expiração fixa de 30 minutos por padrão. Não são JWTs. O servidor guarda somente seu hash SHA-256; reiniciar invalida todas as sessões. Logout de um token não encerra os demais. Até 1.000 sessões simultâneas por padrão; entradas expiradas são removidas ao validar ou emitir sessões. Ao atingir o limite, novo login recebe 503 até liberar capacidade.

A senha pública é comparada por BCrypt. Respostas de login não são armazenáveis em cache. Falhas de credenciais/sessão respondem 401 com `application/problem+json`, sem indicar qual credencial errou. Dados ausentes/JSON inválido respondem 400. Acesso negado por permissão responde 403. Os demais endpoints `/api/**` exigem autenticação por padrão; isso já vale para futuros módulos.

Não há cookie de autenticação, formulário de login do Spring, HTTP Basic nem refresh token. Envie `Authorization: Bearer <accessToken>` em cada chamada protegida. CSRF está desativado porque cookies não autenticam a API; CORS continua explícito. No cliente futuro, evite persistir o token em URLs/logs. Fora do ambiente local, utilize HTTPS. Esta conta pública não representa autenticação de usuários reais.

No emulador Android, o endereço do PC é `http://10.0.2.2:8080`. A configuração do frontend/API permanece inalterada; sua integração de negócio acontece na parte 6. GitHub Pages não executa Java; esse Swagger depende do backend local ligado.

## Parte 3: movimentações em memória

Todos os endpoints abaixo exigem Bearer. Propriedade do registro vem da conta autenticada, nunca do corpo do pedido. Duas sessões da mesma conta compartilham os mesmos registros. O servidor começa vazio e reiniciar apaga sessões, registros ativos e lixeira.

| Método | Endpoint | Resultado |
| --- | --- | --- |
| POST | `/api/v1/movements` | Cria, retorna 201 com ID e cabeçalho Location |
| GET | `/api/v1/movements` | Lista paginada de ativos; `deleted=true` consulta somente a lixeira |
| GET | `/api/v1/movements/{id}` | Consulta ativo; inexistente, de outra conta ou excluído retorna 404 |
| PUT | `/api/v1/movements/{id}` | Substitui campos editáveis; preserva ID e createdAt; excluído retorna 409 |
| DELETE | `/api/v1/movements/{id}` | Exclusão reversível, retorna 204; repetir mantém a primeira data de exclusão |
| POST | `/api/v1/movements/{id}/restore` | Restaura e retorna 200; registro já ativo retorna 409 |

Exemplo de criação/edição:

```json
{
  "type": "entrada",
  "description": "Receita demonstrativa",
  "valueInCents": 12345,
  "date": "2026-10-28",
  "category": "Receitas diversas",
  "account": "Conta principal",
  "notes": "Exemplo fictício"
}
```

- Tipos: `entrada` ou `saida`. Valor positivo de 1 a 999999999999 centavos, sem frações de centavo; não se usa ponto flutuante para armazenar dinheiro.
- Descrição obrigatória, até 160 caracteres; observação opcional, até 2.000. Espaços nas extremidades são removidos.
- Categorias demonstrativas: `Receitas diversas`, `Despesas administrativas`, `Outros`. Conta: `Conta principal`.
- Data válida de 1900-01-01 a 9999-12-31. ID UUID e datas de auditoria são gerados pelo servidor.
- Filtros: `type`, `q` (descrição/categoria, sem distinção de maiúsculas), `from` e `to` inclusivos, `deleted`. Intervalo invertido retorna 400.
- Paginação: `page=0` e `size=20` por padrão; tamanho entre 1 e 100. Retorno: `items`, `total`, `page`, `size`, `totalPages`. Página fora do resultado tem lista vazia. Ordenação por data decrescente, criação decrescente e ID como desempate.
- Memória limitada a 10.000 registros por padrão, contando a lixeira. Limite atingido responde 503. Não há exclusão definitiva nem descarte automático de registros.
- Alterações são atômicas no repositório em memória; edição concorrente segue a última gravação. Registros excluídos não são reativados por edição.

No Swagger, faça login, autorize o token, crie um exemplo e copie seu `id` para consultar, editar, excluir e restaurar. Para ver a lixeira, use GET com `deleted=true`. O frontend continua usando seus registros locais até a integração.

## Parte 4: financeiro e documentos

Todos os endpoints abaixo exigem Bearer.

| Método | Endpoint | Função |
| --- | --- | --- |
| GET | `/api/v1/finance/summary?month=2026-10` | Saldo, entradas, saídas, movimentado, pendências e gráfico de seis meses |
| GET | `/api/v1/finance/accounts` | Contas fictícias com filtros e paginação |
| GET | `/api/v1/finance/accounts/{id}` | Detalhe de uma conta fictícia |
| GET | `/api/v1/finance/revenue?month=2026-10` | Receitas fictícias por setor/funcionário |
| GET | `/api/v1/finance/budget?month=2026-10` | Orçamento fictício previsto e realizado |
| GET | `/api/v1/documents` | Pesquisa e paginação dos metadados de anexos ativos |
| PUT | `/api/v1/movements/{id}/attachment` | Upload multipart do campo `file`, adicionando/substituindo o anexo |
| GET | `/api/v1/movements/{id}/attachment` | Download dos bytes com nome e tipo corretos |
| DELETE | `/api/v1/movements/{id}/attachment` | Remove somente o arquivo e libera sua memória |

### Regras financeiras

- `month` usa YYYY-MM, de 1900-01 a 9999-12; padrão outubro de 2026.
- Saldo tem saldo inicial zero e considera todas as movimentações ativas da conta, inclusive fora do mês escolhido. Entradas, saídas, movimentado e resultado mensal usam somente o mês escolhido. O gráfico mostra seis meses terminando no mês escolhido.
- Excluir uma movimentação retira seu valor dos indicadores; editar e restaurar recalculam as consultas. Anexos não alteram valores. Consultas usam um snapshot dos registros da conta.
- Contas têm referência fixa em **22/10/2026**, indicada no retorno, igual ao catálogo do frontend. Outubro tem R$ 5.240,00 a pagar em três pendências, das quais uma vencida, e R$ 26.500,00 a receber. Os valores dos endpoints estão em centavos `int64`.
- Contas não geram movimentações automaticamente. Não há endpoint de pagamento, quitação ou recebimento.
- Filtros de contas: `kind=payable|receivable`, `status=all|open|overdue|paid`, `q`, `month`, `page` e `size`. `open` inclui somente as não vencidas; `all` não restringe situação. `totalCents` soma todos os resultados do filtro, antes da paginação.
- Receitas por setor e orçamento têm `source=catalogo_ficticio`, separados das entradas/saídas cadastradas. Exemplos existem somente em outubro de 2026; outros meses retornam listas e totais zero.

### Regras dos anexos

- Um arquivo por movimentação ativa, até **5 MiB**. PDF, PNG e JPEG apenas. Confere MIME, assinatura básica e cabeçalho/dimensões de imagens (até 20 milhões de pixels); não realiza análise completa do conteúdo de PDFs.
- Limite global de **50 MiB** de binários, incluindo os de movimentações na lixeira. Acima disso retorna 503. Arquivo grande retorna 413; tipo não permitido, 415; conteúdo vazio/incompatível, 400.
- Nome é sanitizado e extensão ajustada ao tipo aceito. Download envia `Content-Disposition: attachment`, `Content-Type`, tamanho e `Cache-Control: no-store`. A URL sozinha não autentica o cliente; o frontend futuro deve enviar Authorization ao buscar o Blob.
- Upload substitui o anterior somente após validar conteúdo, propriedade, estado e capacidade. Atualização do registro preserva o anexo. Operações de arquivo e exclusão/restauração usam o mesmo repositório sincronizado.
- Lixeira oculta o documento e bloqueia upload/download; restauração torna o mesmo anexo acessível novamente. Remoção explícita do anexo não tem lixeira própria, é irreversível e não exclui a movimentação. DELETE repetido retorna 204 para registro ativo conhecido.
- Lista `/documents` aceita `q` (nome, descrição, categoria), `kind=all|pdf|image`, `month` (data da movimentação), `page` e `size` (1–100). Retorna metadados e caminho autenticado, sem bytes. Proprietário vem da sessão; outro proprietário recebe 404.
- Binários finais permanecem exclusivamente em memória. Parser multipart pode usar temporários; `CASH_UPLOAD_TEMP` configura o local, por padrão o diretório temporário da JVM. A tarefa VS Code nesta máquina direciona o upload para o D:.
- Reiniciar apaga sessões, movimentações e anexos. Não há persistência, integração com arquivos locais do aplicativo, armazenamento externo ou migração automática de dados existentes.

No Swagger: faça login/Authorize, cadastre uma movimentação, copie seu ID e execute PUT do anexo pelo seletor de arquivo. GET documents lista os metadados; GET do anexo baixa os bytes. Consulte summary antes/depois de editar, excluir ou restaurar para acompanhar os indicadores.

## Parte 5: perfil, preferências e notificações

Todos os endpoints exigem Bearer. Proprietário vem da sessão e não de parâmetros enviados pelo cliente. Duas sessões da mesma conta compartilham perfil, preferências e leitura. Logout não apaga esses dados; reiniciar o servidor repõe os padrões.

| Método | Endpoint | Função |
| --- | --- | --- |
| GET | `/api/v1/profile` | Perfil fictício e identidade de login somente para leitura |
| PUT | `/api/v1/profile` | Substitui nome, e-mail de contato, telefone opcional e cartório |
| GET | `/api/v1/profile/preferences` | Consulta ocultação de saldo e redução de movimento |
| PUT | `/api/v1/profile/preferences` | Substitui ambas as opções |
| GET | `/api/v1/notifications` | Lista paginada e filtrada de exemplos |
| GET | `/api/v1/notifications/unread-count` | Contagem global de não lidas da conta |
| GET | `/api/v1/notifications/{id}` | Detalhe, sem alterar leitura |
| PATCH | `/api/v1/notifications/{id}/read` | Define leitura com `{"read":true}` ou `{"read":false}` |
| POST | `/api/v1/notifications/read-all` | Marca todas como lidas, retornando quantidade alterada |

### Perfil

Padrões alinhados à demonstração visual: Fulano de Lima, fulano@example.com, telefone vazio e Cartório de demonstração. `hideBalance=true`, `reduceMotion=false`.

Nome obrigatório até 100 caracteres; contato com e-mail válido até 254; cartório obrigatório até 120; telefone opcional até 25, aceitando dígitos, espaços, `+`, parênteses, ponto e hífen. Espaços externos são removidos. Perfil e preferências são alterados atomicamente, preservando os campos do outro grupo.

`email` é somente contato fictício. `loginEmail=demo@cash.local`, senha de teste e `role=DEMO` continuam fixos; enviar campos extras não muda a identidade nem cria permissões. `/auth/me` representa a sessão, enquanto `/profile` fornece os dados de apresentação editáveis. Não existe alteração real de senha nesta parte.

Exemplo de perfil:

```json
{"name":"Fulano de Lima","email":"fulano@example.com","phone":"","office":"Cartório de demonstração"}
```

Exemplo de preferências:

```json
{"hideBalance":true,"reduceMotion":false}
```

### Notificações

Quatro exemplos: `aluguel`, `pagamento`, `movimentacao` e `documento`. As duas primeiras iniciam não lidas; as demais, lidas. O backend usa referência explícita em **22/10/2026**, diferente das datas originais de junho de 2025 do Figma ainda presentes no frontend. Links `accountPath` de aluguel e software correspondem ao catálogo financeiro de outubro de 2026. Alertas de movimentação/documento são identificados como exemplos e não criam registros ou arquivos fictícios ocultos.

Filtros: `category=financeira|sistema`, `read=true|false`, `q`, `page` (zero inicial) e `size` (1–100, padrão 20). Busca em título/descrição sem diferenciar maiúsculas. Ordenação por recebimento decrescente e ID. `total` respeita filtros; `unreadCount` é global e não depende da página. Datas de recebimento do catálogo são fixas, não notificações atuais.

GET de detalhe não modifica leitura. PATCH true registra `readAt`; repetir true preserva a primeira data. PATCH false limpa `readAt`. Ler todas retorna `updatedCount` e `unreadCount=0` no instante da operação; repetir retorna zero alterações. ID inexistente responde 404; campos/filtros inválidos, 400; sessão ausente, 401.

Não há envio, push, assinatura de notificações, e-mail, criação automática de alertas por eventos financeiros ou permissão do sistema operacional. As preferências serão aplicadas pela interface na parte 6. Recuperação por e-mail e biometria continuam fora do escopo.

## Configuração

| Variável | Uso |
| --- | --- |
| `PORT` | Porta HTTP; padrão 8080 |
| `SPRING_PROFILES_ACTIVE=local` | Habilita documentação e CORS de desenvolvimento |
| `CASH_ALLOWED_ORIGINS` | Origens separadas por vírgula no perfil local; padrão inclui localhost:3000, 127.0.0.1:3000, https://localhost e 10.0.2.2:3000 |
| `CASH_SESSION_TTL` | Duração ISO-8601 positiva da sessão; padrão `PT30M` |
| `CASH_MAX_SESSIONS` | Limite de sessões simultâneas; padrão `1000` |
| `CASH_MOVEMENTS_MAX_RECORDS` | Limite positivo de registros em memória, incluindo lixeira; padrão `10000` |
| `CASH_DOCUMENTS_MAX_TOTAL_BYTES` | Limite global positivo de bytes de anexos; padrão `52428800` |
| `CASH_UPLOAD_TEMP` | Diretório temporário existente e gravável para multipart; padrão `${java.io.tmpdir}` |

CORS limita origens de navegador; não substitui autenticação. Status, saúde e login são públicos. Sem perfil local, o Swagger/OpenAPI não são publicados por padrão.

## Validação

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Os testes iniciam o servidor em portas aleatórias e conferem status, contrato gerado, interface Swagger, saúde, preflight de origens permitidas/rejeitadas e documentação desativada fora do perfil local.

Verificação de 28/10/2026: quatro testes aprovados e `package` concluído. Swagger aberto no navegador, GET de status executado com HTTP 200; atalho, contrato YAML e saúde também responderam HTTP 200.

Parte 2, 28/10/2026: nove testes aprovados, incluindo credenciais incorretas, validação de corpo, token ausente/inventado, ausência de autenticação por cookie, duas sessões independentes, logout, expiração determinística, limite de sessões, invalidação por reinício e contrato Bearer. Pacote final gerado após encerrar o processo que mantinha o JAR aberto no Windows. No Swagger final, login e `/me` responderam 200, logout respondeu 204 e o mesmo token passou a receber 401. Token de teste revogado e removido da interface. Frontend não modificado nesta parte.

Parte 3, 28/10/2026: quatorze testes e build aprovados. Cobertura inclui CRUD, lixeira, restauração, valores fracionários/negativos/fora do limite, datas inválidas, filtros, paginação, isolamento entre proprietários e limite de memória incluindo excluídos. Na instância final, conferidos cadastro 201, edição 200, exclusão 204, restauração 200 e preservação do valor editado. HTML do Swagger respondeu 200 e OpenAPI publicou as três famílias de rotas de movimentações. A automação do navegador ficou indisponível nesta revisão; a nova documentação foi validada por HTTP e testes do contrato, sem afirmar execução visual dos novos endpoints no Swagger. Registro fictício usado na conferência foi deixado na lixeira e seu token revogado. Relatório local: `D:\Projetos\Ferramentas\Codex\CashMobile\backend-parte3-check.json`.

Parte 4, 28/10/2026: dezenove testes e build aprovados. Incluídos recálculo após edição/exclusão/restauração, catálogo coerente com frontend, filtros inválidos, upload de PDF/PNG/JPEG, download com os mesmos bytes, substituição, proteção de propriedade, preservação de anexo na lixeira, rejeição de arquivos vazios/incompatíveis/grandes e limite global de memória. No Swagger foram executados login, criação fictícia, upload PNG de 68 bytes (200) e summary (200), que refletiu os 12.345 centavos cadastrados. Corrigidos o schema de centavos como integer e respostas de sucesso explícitas no OpenAPI. Refeito o pacote, reiniciada a instância final e conferidos Swagger HTTP 200, summary e schemas publicados. Fixtures e tokens anteriores foram descartados no reinício; sessão usada na última consulta HTTP revogada. Frontend permanece sem integração de negócio. Relatório: `D:\Projetos\Ferramentas\Codex\CashMobile\backend-parte4-check.json`.

Parte 5, 28/10/2026: vinte e quatro testes e build aprovados. Cobertura de edição de perfil, preservação cruzada de preferências, credenciais e papel imutáveis, validação, compartilhamento entre sessões da mesma conta, isolamento entre proprietários e reinício. Incluídos filtros/paginação de alertas, detalhe sem efeito colateral, leitura individual idempotente, leitura de todos e links financeiros coerentes. Perfil consultado pelo Swagger com HTTP 200. Na instância final, PUT de perfil/preferências e PATCH de leitura responderam 200; contador passou de 2 para 1 e a conta vinculada correspondeu ao aluguel de 23/10/2026. Dados e leitura de teste restaurados; sessões de conferência revogadas. Relatório: `D:\Projetos\Ferramentas\Codex\CashMobile\backend-parte5-check.json`. Integração com a interface continua reservada à parte 6.

## Partes do backend

1. **Base e Swagger:** configuração, status e documentação interativa.
2. **Login e sessão:** conta demonstrativa, encerramento de sessão e proteção dos endpoints.
3. **Movimentações:** CRUD, lixeira/restauração, validação e armazenamento em memória.
4. **Financeiro e documentos:** indicadores, contas e anexos vinculados.
5. **Perfil e notificações:** dados fictícios, preferências e leitura de alertas.
6. **Integração e testes:** Next.js/Android, autorização e separação do armazenamento para futura conexão ao banco do colega.

Recuperação por e-mail e biometria estão fora do escopo. Dados de negócio em memória são temporários e perdidos ao reiniciar.

Parte 6, 28/10/2026: interface Next.js e APK Android conectados aos endpoints de negócio. Token temporário somente em memória JavaScript; logout, bloqueio de rotas e resposta 401 conferidos. API desligada mostra erro sem fallback local. Modo local-demo preservado explicitamente para Pages. Banco continua pendente. Ver `../frontend/INTEGRACAO.md` para cobertura e limites da revisão.
