package br.com.futebol.integration.game;

import br.com.futebol.interfaces.auth.LoginRequest;
import br.com.futebol.interfaces.game.ConfirmNameRequest;
import br.com.futebol.interfaces.game.CreateGameRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@DisplayName("GameConfirmationResource - Testes de Integração")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameConfirmationResourceIT {

    private static String adminToken;
    private static String jogadorToken;
    private static String releasedGameId;
    private static String notReleasedGameId;
    private static String startedGameId;

    @BeforeAll
    static void setUp() {
        // Realiza login como ADMIN
        LoginRequest adminLoginRequest = LoginRequest.builder()
                .email("admin@futebol.com")
                .password("admin123")
                .build();

        adminToken = given()
                .contentType(ContentType.JSON)
                .body(adminLoginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        // Criar jogos para os testes
        // Jogo liberado no futuro
        CreateGameRequest releasedGameRequest = CreateGameRequest.builder()
                .gameDate(OffsetDateTime.now().plusDays(7))
                .build();

        releasedGameId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(releasedGameRequest)
                .when()
                .post("/api/games")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Liberar o jogo
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .put("/api/games/" + releasedGameId + "/release")
                .then()
                .statusCode(200);

        // Jogo não liberado
        CreateGameRequest notReleasedGameRequest = CreateGameRequest.builder()
                .gameDate(OffsetDateTime.now().plusDays(7))
                .build();

        notReleasedGameId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(notReleasedGameRequest)
                .when()
                .post("/api/games")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Jogo já iniciado (no passado)
        CreateGameRequest startedGameRequest = CreateGameRequest.builder()
                .gameDate(OffsetDateTime.now().minusHours(1))
                .build();

        startedGameId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(startedGameRequest)
                .when()
                .post("/api/games")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Liberar o jogo que já iniciou
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .put("/api/games/" + startedGameId + "/release")
                .then()
                .statusCode(200);
    }

    @Nested
    @DisplayName("POST /api/games/{gameId}/confirmations")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ConfirmNameTests {

        @Test
        @Order(1)
        @DisplayName("Deve confirmar nome com sucesso quando lista estiver liberada")
        void deveConfirmarNomeComSucessoQuandoListaEstiverLiberada() {
            ConfirmNameRequest request = ConfirmNameRequest.builder()
                    .confirmedName("João Silva")
                    .build();

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/games/" + releasedGameId + "/confirmations")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("gameId", is(releasedGameId))
                    .body("confirmedName", is("João Silva"))
                    .body("confirmedAt", notNullValue());
        }

        @Test
        @Order(2)
        @DisplayName("Deve retornar 403 quando lista não estiver liberada")
        void deveRetornar403QuandoListaNaoEstiverLiberada() {
            ConfirmNameRequest request = ConfirmNameRequest.builder()
                    .confirmedName("Maria Santos")
                    .build();

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/games/" + notReleasedGameId + "/confirmations")
                    .then()
                    .statusCode(403)
                    .body("message", containsString("Lista não está liberada"));
        }

        @Test
        @Order(3)
        @DisplayName("Deve retornar 400 quando jogo já iniciou")
        void deveRetornar400QuandoJogoJaIniciou() {
            ConfirmNameRequest request = ConfirmNameRequest.builder()
                    .confirmedName("Pedro Costa")
                    .build();

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/games/" + startedGameId + "/confirmations")
                    .then()
                    .statusCode(400)
                    .body("message", containsString("Lista encerrada"));
        }

        @Test
        @Order(4)
        @DisplayName("Deve retornar 409 quando nome já estiver confirmado")
        void deveRetornar409QuandoNomeJaEstiverConfirmado() {
            ConfirmNameRequest request = ConfirmNameRequest.builder()
                    .confirmedName("João Silva")
                    .build();

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/games/" + releasedGameId + "/confirmations")
                    .then()
                    .statusCode(409)
                    .body("message", containsString("Nome já confirmado"));
        }

        @Test
        @Order(5)
        @DisplayName("Deve retornar 409 quando usuário já confirmou")
        void deveRetornar409QuandoUsuarioJaConfirmou() {
            // Criar novo jogo para este teste
            CreateGameRequest newGameRequest = CreateGameRequest.builder()
                    .gameDate(OffsetDateTime.now().plusDays(7))
                    .build();

            String newGameId = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(newGameRequest)
                    .when()
                    .post("/api/games")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            // Liberar o jogo
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .put("/api/games/" + newGameId + "/release")
                    .then()
                    .statusCode(200);

            // Primeira confirmação
            ConfirmNameRequest firstRequest = ConfirmNameRequest.builder()
                    .confirmedName("Teste User")
                    .build();

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(firstRequest)
                    .when()
                    .post("/api/games/" + newGameId + "/confirmations")
                    .then()
                    .statusCode(201);

            // Tentar confirmar novamente
            ConfirmNameRequest secondRequest = ConfirmNameRequest.builder()
                    .confirmedName("Teste User 2")
                    .build();

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(secondRequest)
                    .when()
                    .post("/api/games/" + newGameId + "/confirmations")
                    .then()
                    .statusCode(409)
                    .body("message", containsString("já confirmou"));
        }
    }

    @Nested
    @DisplayName("GET /api/games/{gameId}/confirmations")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ListConfirmationsTests {

        @Test
        @Order(1)
        @DisplayName("Deve listar confirmações quando usuário for ADMIN")
        void deveListarConfirmacoesQuandoUsuarioForAdmin() {
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/api/games/" + releasedGameId + "/confirmations")
                    .then()
                    .statusCode(200)
                    .body("gameId", is(releasedGameId))
                    .body("confirmations", notNullValue())
                    .body("total", notNullValue());
        }

        @Test
        @Order(2)
        @DisplayName("Deve retornar 404 quando jogo não existir")
        void deveRetornar404QuandoJogoNaoExistir() {
            UUID nonExistentId = UUID.randomUUID();
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/api/games/" + nonExistentId + "/confirmations")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /api/games/{gameId}/confirmations/me")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FindMyConfirmationTests {

        @Test
        @Order(1)
        @DisplayName("Deve retornar confirmação quando usuário tiver confirmado")
        void deveRetornarConfirmacaoQuandoUsuarioTiverConfirmado() {
            // Criar novo jogo e confirmar nome
            CreateGameRequest newGameRequest = CreateGameRequest.builder()
                    .gameDate(OffsetDateTime.now().plusDays(7))
                    .build();

            String newGameId = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(newGameRequest)
                    .when()
                    .post("/api/games")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            // Liberar o jogo
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .put("/api/games/" + newGameId + "/release")
                    .then()
                    .statusCode(200);

            // Confirmar nome
            ConfirmNameRequest request = ConfirmNameRequest.builder()
                    .confirmedName("Minha Confirmação")
                    .build();

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/games/" + newGameId + "/confirmations")
                    .then()
                    .statusCode(201);

            // Buscar minha confirmação
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/api/games/" + newGameId + "/confirmations/me")
                    .then()
                    .statusCode(200)
                    .body("id", notNullValue())
                    .body("gameId", is(newGameId))
                    .body("confirmedName", is("Minha Confirmação"));
        }

        @Test
        @Order(2)
        @DisplayName("Deve retornar 404 quando usuário não tiver confirmado")
        void deveRetornar404QuandoUsuarioNaoTiverConfirmado() {
            // Criar novo jogo sem confirmar
            CreateGameRequest newGameRequest = CreateGameRequest.builder()
                    .gameDate(OffsetDateTime.now().plusDays(7))
                    .build();

            String newGameId = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(newGameRequest)
                    .when()
                    .post("/api/games")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            // Tentar buscar confirmação que não existe
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/api/games/" + newGameId + "/confirmations/me")
                    .then()
                    .statusCode(404)
                    .body("message", containsString("ainda não confirmou"));
        }
    }
}

