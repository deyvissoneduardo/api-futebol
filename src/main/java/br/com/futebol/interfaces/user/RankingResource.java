package br.com.futebol.interfaces.user;

import br.com.futebol.application.user.UserStatisticsService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Resource para operações de ranking de estatísticas.
 */
@Path("/api/ranking")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Rankings", description = "Operações de ranking de estatísticas de usuários")
@SecurityRequirement(name = "jwt")
public class RankingResource {

    @Inject
    UserStatisticsService userStatisticsService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/goals")
    @RolesAllowed({"ADMIN", "JOGADOR", "SUPER_ADMIN"})
    @Operation(
            summary = "Ranking de Gols",
            description = "Retorna o ranking de gols ordenado do maior para o menor. Apenas ADMIN e JOGADOR podem consultar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Ranking retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = RankingResponse.class))
            ),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado")
    })
    public Response getRankingByGoals() {
        RankingResponse response = userStatisticsService.getRankingByGoals();
        return Response.ok(response).build();
    }

    @GET
    @Path("/complaints")
    @RolesAllowed({"ADMIN", "JOGADOR", "SUPER_ADMIN"})
    @Operation(
            summary = "Ranking de Reclamações",
            description = "Retorna o ranking de reclamações ordenado do maior para o menor. Apenas ADMIN e JOGADOR podem consultar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Ranking retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = RankingResponse.class))
            ),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado")
    })
    public Response getRankingByComplaints() {
        RankingResponse response = userStatisticsService.getRankingByComplaints();
        return Response.ok(response).build();
    }

    @GET
    @Path("/victories")
    @RolesAllowed({"ADMIN", "JOGADOR", "SUPER_ADMIN"})
    @Operation(
            summary = "Ranking de Vitórias",
            description = "Retorna o ranking de vitórias ordenado do maior para o menor. Apenas ADMIN e JOGADOR podem consultar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Ranking retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = RankingResponse.class))
            ),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado")
    })
    public Response getRankingByVictories() {
        RankingResponse response = userStatisticsService.getRankingByVictories();
        return Response.ok(response).build();
    }

    @GET
    @Path("/draws")
    @RolesAllowed({"ADMIN", "JOGADOR", "SUPER_ADMIN"})
    @Operation(
            summary = "Ranking de Empates",
            description = "Retorna o ranking de empates ordenado do maior para o menor. Apenas ADMIN e JOGADOR podem consultar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Ranking retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = RankingResponse.class))
            ),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado")
    })
    public Response getRankingByDraws() {
        RankingResponse response = userStatisticsService.getRankingByDraws();
        return Response.ok(response).build();
    }

    @GET
    @Path("/defeats")
    @RolesAllowed({"ADMIN", "JOGADOR", "SUPER_ADMIN"})
    @Operation(
            summary = "Ranking de Derrotas",
            description = "Retorna o ranking de derrotas ordenado do maior para o menor. Apenas ADMIN e JOGADOR podem consultar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Ranking retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = RankingResponse.class))
            ),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado")
    })
    public Response getRankingByDefeats() {
        RankingResponse response = userStatisticsService.getRankingByDefeats();
        return Response.ok(response).build();
    }

    @GET
    @Path("/minutes-played")
    @RolesAllowed({"ADMIN", "JOGADOR", "SUPER_ADMIN"})
    @Operation(
            summary = "Ranking de Minutos Jogados",
            description = "Retorna o ranking de minutos jogados ordenado do maior para o menor. Apenas ADMIN e JOGADOR podem consultar."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Ranking retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = RankingResponse.class))
            ),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "403", description = "Acesso negado")
    })
    public Response getRankingByMinutesPlayed() {
        RankingResponse response = userStatisticsService.getRankingByMinutesPlayed();
        return Response.ok(response).build();
    }
}

