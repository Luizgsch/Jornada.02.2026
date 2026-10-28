package br.com.cashmobile.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI cashOpenApi() {
        return new OpenAPI().components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
                        .description("Token opaco retornado pelo login. Cole somente accessToken, sem o prefixo Bearer.")))
                .info(new Info()
                .title("CASH Mobile — API demonstrativa")
                .version("v1")
                .description("Projeto acadêmico. Partes 1 a 5: documentação, login, movimentações, financeiro, documentos, perfil e notificações. "
                        + "Conta pública demo@cash.local / Cash123!. Sessões duram 30 minutos por padrão. "
                        + "Sessões, movimentações, anexos, perfil, preferências e estados de leitura ficam em memória e são perdidos ao reiniciar. Indicadores usam registros ativos; contas, receitas por setor, orçamento e alertas são catálogos fictícios. Não há banco, pagamentos ou push. Frontend ainda não integrado à API de negócio. "
                        + "Recuperação por e-mail e biometria não fazem parte do escopo."));
    }
}
