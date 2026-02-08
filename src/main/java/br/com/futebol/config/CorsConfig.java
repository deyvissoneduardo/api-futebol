package br.com.futebol.config;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Provider
@ApplicationScoped
@Priority(1)
public class CorsConfig implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    @ConfigProperty(name = "cors.allowed-origins", defaultValue = "*")
    String allowedOrigins;

    @Inject
    @ConfigProperty(name = "cors.allowed-methods", defaultValue = "GET,POST,PUT,PATCH,OPTIONS,DELETE")
    String allowedMethods;

    @Inject
    @ConfigProperty(name = "cors.allowed-headers", defaultValue = "*")
    String allowedHeaders;

    @Inject
    @ConfigProperty(name = "cors.exposed-headers", defaultValue = "*")
    String exposedHeaders;

    @Inject
    @ConfigProperty(name = "cors.allow-credentials", defaultValue = "false")
    boolean allowCredentials;

    @Inject
    @ConfigProperty(name = "cors.max-age", defaultValue = "3600")
    int maxAge;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            Response.ResponseBuilder responseBuilder = Response.ok();

            String origin = requestContext.getHeaderString("Origin");
            boolean allowAll = "*".equals(allowedOrigins != null ? allowedOrigins.trim() : "");
            if (origin != null && !origin.isEmpty()) {
                if (allowAll) {
                    responseBuilder.header("Access-Control-Allow-Origin", "*");
                } else if (isOriginAllowed(origin)) {
                    responseBuilder.header("Access-Control-Allow-Origin", origin);
                }
            } else if (allowAll) {
                responseBuilder.header("Access-Control-Allow-Origin", "*");
            }

            responseBuilder.header("Access-Control-Allow-Methods", allowedMethods);
            responseBuilder.header("Access-Control-Allow-Headers", allowedHeaders);
            responseBuilder.header("Access-Control-Expose-Headers", exposedHeaders);
            responseBuilder.header("Access-Control-Max-Age", maxAge);

            if (allowCredentials && !allowAll && origin != null && isOriginAllowed(origin)) {
                responseBuilder.header("Access-Control-Allow-Credentials", "true");
            }

            requestContext.abortWith(responseBuilder.build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        String origin = requestContext.getHeaderString("Origin");
        boolean allowAll = "*".equals(allowedOrigins != null ? allowedOrigins.trim() : "");

        if (allowAll) {
            headers.add("Access-Control-Allow-Origin", "*");
        } else if (origin != null && isOriginAllowed(origin)) {
            headers.add("Access-Control-Allow-Origin", origin);
        }

        headers.add("Access-Control-Allow-Methods", allowedMethods);
        headers.add("Access-Control-Allow-Headers", allowedHeaders);
        headers.add("Access-Control-Expose-Headers", exposedHeaders);
        headers.add("Access-Control-Max-Age", maxAge);

        if (allowCredentials && !allowAll) {
            headers.add("Access-Control-Allow-Credentials", "true");
        }

        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.setStatus(200);
        }
    }

    private boolean isOriginAllowed(String origin) {
        if (origin == null || origin.isEmpty() || allowedOrigins == null) {
            return false;
        }

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
