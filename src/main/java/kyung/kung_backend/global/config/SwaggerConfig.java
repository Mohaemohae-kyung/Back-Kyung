package kyung.kung_backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String JWT_SCHEME_NAME = "BearerToken";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(securityRequirement())
                .components(components())
                .servers(List.of(prodServer(), localServer()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Kung Backend API")
                .description("Kyung 서비스 API Documentation")
                .version("v1.0.0");
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList(JWT_SCHEME_NAME);
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes(JWT_SCHEME_NAME, jwtSecurityScheme());
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name(JWT_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }

    private Server prodServer() {
        return new Server()
                .url("https://can-fly.shop")
                .description("Kung Production Server");
    }

    private Server localServer() {
        return new Server()
                .url("http://localhost:8080")
                .description("Kung Local Server");
    }

    @Bean
    public GroupedOpenApi allGroup() {
        return GroupedOpenApi.builder()
                .group("All APIs")
                .pathsToMatch("/**")
                .build();
    }
}