package br.com.futebol.interfaces.health;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;


@Path("/api/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health", description = "Verificacao de saude da API")
public class HealthResource {

    @GET
    @Operation(summary = "Status da API", description = "Retorna o status atual da API")
    @APIResponse(responseCode = "200", description = "API esta funcionando")
    public Response status() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", OffsetDateTime.now().toString());
        response.put("service", "api-futebol");

        return Response.ok(response).build();
    }
}

