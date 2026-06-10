package com.enterprise.platform.user.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenApi() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("User Service API")
                                .description(
                                        "Enterprise Platform User Management Service"
                                )
                                .version("v1")
                                .contact(
                                        new Contact()
                                                .name("Enterprise Platform")
                                )
                )
                .externalDocs(
                        new ExternalDocumentation()
                                .description("User Service Documentation")
                );
    }
}