package br.com.futebol.interfaces.game;

import br.com.futebol.application.game.GameConfirmationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

/**
 * Resource para operações de confirmação de nomes em jogos.
 */
@Path("/api/games/{gameId}/confirmations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Confirmações de Jogos", description = "Operações de confirmação de nomes em jogos")
@SecurityScheme(
        securitySchemeName = "jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class GameConfirmationResource {

    @Inject
    GameConfirmationService gameConfirmationService;

    @Inject
    JsonWebToken jwt;

    @POST
    @RolesAllowed({"JOGADOR", "ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Confirmar nome", description = "Confirma um nome para o jogo. Se isGuest=true, cria um UUID único para o convidado, permitindo estatísticas separadas.")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Nome confirmado com sucesso",
                    content = @Content(schema = @Schema(implementation = GameConfirmationResponse.class))),
            @APIResponse(responseCode = "400", description = "Dados inválidos ou jogo já iniciou"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Lista não está liberada"),
            @APIResponse(responseCode = "409", description = "Nome já confirmado para este jogo")
    })
    public Response confirmName(@PathParam("gameId") UUID gameId, @Valid ConfirmNameRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        GameConfirmationResponse confirmation = gameConfirmationService.confirmName(gameId, request, userId);
        return Response.status(Response.Status.CREATED).entity(confirmation).build();
    }

    @GET
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Listar confirmações", description = "Retorna lista completa de confirmações do jogo (apenas ADMIN/SUPER_ADMIN)")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Lista de confirmações",
                    content = @Content(schema = @Schema(implementation = GameConfirmationListResponse.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public Response listConfirmations(@PathParam("gameId") UUID gameId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        GameConfirmationListResponse response = gameConfirmationService.listConfirmations(gameId, userId);
        return Response.ok(response).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"JOGADOR", "ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Minhas confirmações", description = "Retorna todas as confirmações relacionadas ao usuário logado: próprias e de convidados confirmados por ele")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Lista de confirmações do usuário",
                    content = @Content(schema = @Schema(implementation = GameConfirmationResponse.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public Response findMyConfirmations(@PathParam("gameId") UUID gameId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        java.util.List<GameConfirmationResponse> confirmations = gameConfirmationService.findMyConfirmations(gameId, userId);
        return Response.ok(confirmations).build();
    }
}

