package br.com.futebol.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Configuração centralizada de CORS (Cross-Origin Resource Sharing).
 * 
 * Esta classe resolve problemas de CORS permitindo requisições de origens específicas,
 * métodos HTTP e headers personalizados.
 * 
 * As configurações podem ser definidas no application.properties:
 * - cors.allowed-origins: Lista de origens permitidas (separadas por vírgula)
 * - cors.allowed-methods: Métodos HTTP permitidos (separados por vírgula)
 * - cors.allowed-headers: Headers permitidos (separados por vírgula)
 * - cors.exposed-headers: Headers expostos ao cliente (separados por vírgula)
 * - cors.allow-credentials: Se permite credenciais (true/false)
 * - cors.max-age: Tempo de cache do preflight em segundos
 */
@Provider
@ApplicationScoped
public class CorsConfig implements ContainerResponseFilter {

    @Inject
    @ConfigProperty(name = "cors.allowed-origins", defaultValue = "http://localhost:3000,http://localhost:3001,http://127.0.0.1:3000,https://confirm-name-today.vercel.app")
    String allowedOrigins;

    @Inject
    @ConfigProperty(name = "cors.allowed-methods", defaultValue = "GET,POST,PUT,DELETE,PATCH,OPTIONS")
    String allowedMethods;

    @Inject
    @ConfigProperty(name = "cors.allowed-headers", defaultValue = "Content-Type,Authorization,X-Requested-With,Accept,Origin")
    String allowedHeaders;

    @Inject
    @ConfigProperty(name = "cors.exposed-headers", defaultValue = "Authorization")
    String exposedHeaders;

    @Inject
    @ConfigProperty(name = "cors.allow-credentials", defaultValue = "true")
    boolean allowCredentials;

    @Inject
    @ConfigProperty(name = "cors.max-age", defaultValue = "3600")
    int maxAge;

    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) {
        
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        
        // Obtém a origem da requisição
        String origin = requestContext.getHeaderString("Origin");
        
        // Verifica se a origem está na lista de origens permitidas
        if (origin != null && isOriginAllowed(origin)) {
            headers.add("Access-Control-Allow-Origin", origin);
        } else if (allowedOrigins.contains("*")) {
            // Permite todas as origens se configurado com "*"
            headers.add("Access-Control-Allow-Origin", "*");
        }
        
        // Headers CORS padrão
        headers.add("Access-Control-Allow-Methods", allowedMethods);
        headers.add("Access-Control-Allow-Headers", allowedHeaders);
        headers.add("Access-Control-Expose-Headers", exposedHeaders);
        headers.add("Access-Control-Max-Age", maxAge);
        
        // Permite credenciais (cookies, authorization headers, etc.)
        if (allowCredentials) {
            headers.add("Access-Control-Allow-Credentials", "true");
        }
        
        // Responde imediatamente para requisições OPTIONS (preflight)
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.setStatus(200);
        }
    }
    
    /**
     * Verifica se a origem está na lista de origens permitidas.
     * 
     * @param origin A origem da requisição
     * @return true se a origem está permitida, false caso contrário
     */
    private boolean isOriginAllowed(String origin) {
        if (origin == null || origin.isEmpty() || allowedOrigins == null) {
            return false;
        }
        
        // Se estiver configurado como "*", permite todas as origens
        if ("*".equals(allowedOrigins.trim())) {
            return true;
        }
        
        String[] allowedOriginsList = allowedOrigins.split(",");
        for (String allowedOrigin : allowedOriginsList) {
            if (allowedOrigin.trim().equals(origin)) {
                return true;
            }
        }
        
        return false;
    }
}

