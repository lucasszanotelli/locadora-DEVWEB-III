package com.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoOpenApi {
    @Bean
    public OpenAPI locadoraOpenApi() {
        return new OpenAPI().info(new Info()
            .title("API da Locadora")
            .version("1.0")
            .description("API para gerenciar atores, diretores, classes, títulos e itens da locadora."));
    }
}
