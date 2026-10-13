package com.asyncprocessing.platform.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Async Processing API",
                version = "1.0.0",
                description = """
                        API REST para crear y consultar tareas
                        procesadas de forma asíncrona.
                        Utiliza RabbitMQ para la mensajería,
                        PostgreSQL para la persistencia y
                        Redis para la caché.
                        """
        ),
        tags = {
                @Tag(
                        name = "Tareas",
                        description = "Creación, consulta y seguimiento de tareas"
                )
        }
)
public class OpenApiConfig {
}
