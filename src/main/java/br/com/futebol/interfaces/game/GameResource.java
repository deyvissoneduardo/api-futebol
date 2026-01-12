package br.com.futebol.interfaces.game;

import br.com.futebol.application.game.GameService;
import br.com.futebol.interfaces.game.CreateGameResponse;
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

import java.util.List;
import java.util.UUID;

/**
 * Resource para operações de jogos.
 */
@Path("/api/games")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Jogos", description = "Operações de gerenciamento de jogos")
@SecurityScheme(
        securitySchemeName = "jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class GameResource {

    @Inject
    GameService gameService;

    @Inject
    JsonWebToken jwt;

    @POST
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Criar jogo", description = "Cria um novo jogo (apenas ADMIN/SUPER_ADMIN)")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Jogo criado com sucesso",
                    content = @Content(schema = @Schema(implementation = CreateGameResponse.class))),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado")
    })
    public Response create(@Valid CreateGameRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        CreateGameResponse game = gameService.create(request, userId);
        return Response.status(Response.Status.CREATED).entity(game).build();
    }

    @GET
    @RolesAllowed({"JOGADOR", "ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Listar jogos", description = "Retorna lista de todos os jogos ordenados por data")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Lista de jogos",
                    content = @Content(schema = @Schema(implementation = GameResponse.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado")
    })
    public Response findAll() {
        List<GameResponse> games = gameService.findAll();
        return Response.ok(games).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"JOGADOR", "ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Buscar jogo", description = "Retorna um jogo pelo ID")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Jogo encontrado",
                    content = @Content(schema = @Schema(implementation = GameResponse.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public Response findById(@PathParam("id") UUID id) {
        GameResponse game = gameService.findById(id);
        return Response.ok(game).build();
    }

    @PUT
    @Path("/{id}/release")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Iniciar jogo", description = "Inicia o jogo, bloqueando novas confirmações (apenas ADMIN/SUPER_ADMIN)")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Jogo iniciado com sucesso",
                    content = @Content(schema = @Schema(implementation = GameResponse.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public Response release(@PathParam("id") UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        GameResponse game = gameService.releaseGame(id, userId);
        return Response.ok(game).build();
    }
}

