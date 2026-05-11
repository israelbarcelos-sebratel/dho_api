package br.com.sebratel.bff.dho.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DHO API")
                        .version("1.0")
                        .description("API de Gestão de Desenvolvimento Humano Organizacional (DHO) da Sebratel.")
                        .contact(new Contact()
                                .name("Suporte Sebratel")
                                .email("suporte@sebratel.com.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
