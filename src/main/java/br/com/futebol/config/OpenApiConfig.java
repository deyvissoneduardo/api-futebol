package br.com.futebol.config;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

/**
 * Configuração do OpenAPI/Swagger.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "API Futebol",
                version = "1.0.0",
                description = "API para gerenciamento de futebol - Usuários e Autenticação JWT",
                contact = @Contact(
                        name = "Time de Desenvolvimento",
                        email = "dev@futebol.com"
                ),
                license = @License(
                        name = "MIT",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor de Desenvolvimento"),
                @Server(url = "https://api-futebol-06387567b79a.herokuapp.com/", description = "Servidor de Produção")
        }
)
@SecurityScheme(
        securitySchemeName = "jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Autenticação JWT - Insira o token obtido no login"
)
public class OpenApiConfig extends Application {
}

