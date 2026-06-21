package com.medical.appointment.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos de la documentación OpenAPI (Swagger UI en /swagger-ui.html). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI medisaludOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MediSalud API")
                .description("API REST de agendamiento de citas médicas (prueba técnica)")
                .version("v1")
                .license(new License().name("MIT")));
    }
}
