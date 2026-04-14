package br.com.futebol.interfaces.game;

import br.com.futebol.application.game.WorstPlayerVotingService;
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

@Path("/api/games/{gameId}/worst-player-voting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Votacao Pior do Jogo", description = "Operacoes da votacao de pior do jogo")
@SecurityScheme(
        securitySchemeName = "jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class WorstPlayerVotingResource {

    @Inject
    WorstPlayerVotingService worstPlayerVotingService;

    @Inject
    JsonWebToken jwt;

    @PUT
    @Path("/open")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Abrir votacao de pior do jogo", description = "Abre a votacao de pior do jogo para um jogo especifico")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Votacao aberta com sucesso",
                    content = @Content(schema = @Schema(implementation = WorstPlayerVotingStatusResponse.class))),
            @APIResponse(responseCode = "401", description = "Nao autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Jogo nao encontrado"),
            @APIResponse(responseCode = "409", description = "Votacao ja aberta ou encerrada")
    })
    public Response open(@PathParam("gameId") UUID gameId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return Response.ok(worstPlayerVotingService.openVoting(gameId, userId)).build();
    }

    @GET
    @Path("/status")
    @RolesAllowed({"ADMIN", "JOGADOR"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Consultar status da votacao", description = "Retorna o estado atual da votacao de pior do jogo")
    public Response status(@PathParam("gameId") UUID gameId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return Response.ok(worstPlayerVotingService.getVotingStatus(gameId, userId)).build();
    }

    @GET
    @Path("/candidates")
    @RolesAllowed({"ADMIN", "JOGADOR"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Listar jogadores elegiveis", description = "Lista os jogadores confirmados e elegiveis para receber voto de pior do jogo")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Lista de elegiveis",
                    content = @Content(schema = @Schema(implementation = WorstPlayerCandidateResponse.class))),
            @APIResponse(responseCode = "401", description = "Nao autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Jogo nao encontrado")
    })
    public Response candidates(@PathParam("gameId") UUID gameId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<WorstPlayerCandidateResponse> candidates = worstPlayerVotingService.listCandidates(gameId, userId);
        return Response.ok(candidates).build();
    }

    @POST
    @Path("/votes")
    @RolesAllowed({"ADMIN", "JOGADOR"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Votar no pior do jogo", description = "Permite que ADMIN ou JOGADOR votem uma unica vez no pior do jogo")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Voto registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = WorstPlayerVoteResponse.class))),
            @APIResponse(responseCode = "400", description = "Dados invalidos"),
            @APIResponse(responseCode = "401", description = "Nao autorizado"),
            @APIResponse(responseCode = "403", description = "Votacao nao aberta"),
            @APIResponse(responseCode = "404", description = "Jogo ou confirmacao nao encontrados"),
            @APIResponse(responseCode = "409", description = "Usuario ja votou")
    })
    public Response vote(@PathParam("gameId") UUID gameId, @Valid WorstPlayerVoteRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return Response.status(Response.Status.CREATED).entity(worstPlayerVotingService.vote(gameId, request, userId)).build();
    }

    @PUT
    @Path("/close")
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Encerrar votacao de pior do jogo", description = "Encerra a votacao de pior do jogo e bloqueia novos votos")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Votacao encerrada com sucesso",
                    content = @Content(schema = @Schema(implementation = WorstPlayerVotingStatusResponse.class))),
            @APIResponse(responseCode = "401", description = "Nao autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Jogo nao encontrado"),
            @APIResponse(responseCode = "409", description = "Votacao nao aberta")
    })
    public Response close(@PathParam("gameId") UUID gameId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return Response.ok(worstPlayerVotingService.closeVoting(gameId, userId)).build();
    }

    @GET
    @Path("/ranking")
    @RolesAllowed({"ADMIN", "JOGADOR"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Ranking do pior do jogo", description = "Retorna o ranking de votos do pior do jogo para a partida")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Ranking retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = WorstPlayerRankingResponse.class))),
            @APIResponse(responseCode = "401", description = "Nao autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Jogo nao encontrado")
    })
    public Response ranking(@PathParam("gameId") UUID gameId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return Response.ok(worstPlayerVotingService.getGameRanking(gameId, userId)).build();
    }

    @GET
    @Path("/history")
    @RolesAllowed({"ADMIN", "JOGADOR"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Ranking historico de pior do jogo", description = "Retorna o ranking historico por intervalo de datas do jogo")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Ranking historico retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = WorstPlayerHistoricalRankingResponse.class))),
            @APIResponse(responseCode = "400", description = "Datas invalidas"),
            @APIResponse(responseCode = "401", description = "Nao autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado")
    })
    public Response history(@QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate, @PathParam("gameId") UUID gameId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return Response.ok(worstPlayerVotingService.getHistoricalRanking(startDate, endDate, userId)).build();
    }

    @GET
    @Path("/players/{confirmationId}/voters")
    @RolesAllowed({"ADMIN", "JOGADOR"})
    @SecurityRequirement(name = "jwt")
    @Operation(summary = "Listar nomes de quem votou em um jogador", description = "Retorna apenas os nomes dos usuarios que votaram no jogador informado")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Lista de votantes retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = WorstPlayerVotersResponse.class))),
            @APIResponse(responseCode = "401", description = "Nao autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado"),
            @APIResponse(responseCode = "404", description = "Jogo ou confirmacao nao encontrados")
    })
    public Response voters(@PathParam("gameId") UUID gameId, @PathParam("confirmationId") UUID confirmationId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return Response.ok(worstPlayerVotingService.getVotersByPlayer(gameId, confirmationId, userId)).build();
    }
}
