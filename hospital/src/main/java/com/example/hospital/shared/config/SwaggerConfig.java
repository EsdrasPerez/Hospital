package com.example.hospital.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Hospital API", version = "1.0", description = "API REST para la gestión del hospital: pacientes, citas, consultas, emergencias, "
                + "hospitalizaciones, farmacia y facturación.\n\n"
                + "**Cómo autenticarse:** 1) Llama a `POST /api/auth/login` con tu usuario y contraseña. "
                + "2) Copia el `token` de la respuesta. 3) Pulsa **Authorize** y pégalo. "
                + "Cada endpoint indica en su descripción qué roles pueden usarlo."),
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {
}
