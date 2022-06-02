package io.kontur.disasterninja.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;

import static io.kontur.disasterninja.config.WebSecurityConfiguration.JWT_AUTH_DISABLED;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI(Environment environment) {
        Server server = new Server();
        server.setUrl(environment.getProperty("server.servlet.context-path"));

        OpenAPI openAPI = new OpenAPI().info(new Info()
                        .title("Disaster Ninja"))
                .servers(Collections.singletonList(server));

        boolean isJwtAuthDisabledProfileActive =
                Arrays.asList(environment.getActiveProfiles()).contains(JWT_AUTH_DISABLED);

        if (!isJwtAuthDisabledProfileActive) {
            String securitySchemeName = "bearerAuth";

            openAPI.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                    .components(new Components()
                            .addSecuritySchemes(securitySchemeName,
                                    new SecurityScheme()
                                            .name(securitySchemeName)
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")));
        }

        return openAPI;
    }
}
