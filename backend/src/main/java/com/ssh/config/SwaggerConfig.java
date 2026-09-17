package com.ssh.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI Configuration
 *
 * Purpose: Enable API documentation and testing interface for SSH.
 *
 * WHY SSH needs this:
 *   - Frontend developers can see all available endpoints
 *   - Test endpoints directly from the browser
 *   - JWT authentication can be applied in Swagger UI
 *
 * URL after startup: http://localhost:8080/swagger-ui.html
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "SSH API",
        version = "1.0",
        description = "SSH (Shared Services Hub) — Youth Economic Participation Platform",
        contact = @Contact(
            name = "SSH Team",
            email = "info@ssh.co.za",
            url = "https://ssh.co.za"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        )
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class SwaggerConfig {
    // No @Bean methods — annotations alone configure Swagger.
    //
    // The @OpenAPIDefinition sets the API title, version, and contact info.
    // The @SecurityScheme tells Swagger UI that every endpoint expects a JWT
    // in the Authorization header, so an "Authorize" button appears.
}
