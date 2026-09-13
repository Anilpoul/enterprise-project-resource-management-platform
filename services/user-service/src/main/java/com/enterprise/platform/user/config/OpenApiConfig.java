package com.enterprise.platform.user.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {

        return new OpenAPI()

                .info(
                        new Info()
                                .title(
                                        "Enterprise Platform User Service API"
                                )
                                .description(
                                        """
                                        User Management Service APIs
                                        
                                        Supports:
                                        - User Profile Management
                                        - Department Management
                                        - Designation Management
                                        - User Search
                                        - Employee Lifecycle
                                        """
                                )
                                .version("v1")
                                .contact(
                                        new Contact()
                                                .name(
                                                        "Enterprise Platform Team"
                                                )
//                                                .email(
//                                                        "engineering@enterprise-platform.com"
//                                                )
                                )
                                .license(
                                        new License()
                                                .name(
                                                        "Internal Enterprise License"
                                                )
                                )
                )

                .externalDocs(
                        new ExternalDocumentation()
                                .description(
                                        "Project Documentation"
                                )
//                                .url(
//                                        "https://enterprise-platform.local/docs"
//                                )
                );
    }
}