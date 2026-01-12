package br.com.futebol.integration.game;

import br.com.futebol.interfaces.auth.LoginRequest;
import br.com.futebol.interfaces.game.CreateGameRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@DisplayName("GameResource - Testes de Integração")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameResourceIT {

    private static String adminToken;
    private static String jogadorToken;
    private static String createdGameId;

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

        // Realiza login como JOGADOR (criar usuário primeiro se necessário)
        // Por padrão, admin@futebol.com é SUPER_ADMIN, então vamos usar um JOGADOR criado
        // Por enquanto, vamos testar apenas com ADMIN
    }

    @Nested
    @DisplayName("POST /api/games")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateGameTests {

        @Test
        @Order(1)
        @DisplayName("Deve criar jogo com sucesso quando usuário for ADMIN")
        void deveCriarJogoComSucessoQuandoUsuarioForAdmin() {
            CreateGameRequest request = CreateGameRequest.builder()
                    .gameDate(OffsetDateTime.now().plusDays(7))
                    .build();

            createdGameId = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/games")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("gameDate", notNullValue())
                    .body("released", is(false))
                    .extract()
                    .path("id");
        }

        @Test
        @Order(2)
        @DisplayName("Deve retornar 401 sem token")
        void deveRetornar401SemToken() {
            CreateGameRequest request = CreateGameRequest.builder()
                    .gameDate(OffsetDateTime.now().plusDays(7))
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/games")
                    .then()
                    .statusCode(401);
        }

        @Test
        @Order(3)
        @DisplayName("Deve retornar 400 quando data for inválida")
        void deveRetornar400QuandoDataForInvalida() {
            CreateGameRequest request = CreateGameRequest.builder()
                    .gameDate(null)
                    .build();

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/games")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /api/games")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ListGamesTests {

        @Test
        @Order(1)
        @DisplayName("Deve listar jogos com token válido")
        void deveListarJogosComTokenValido() {
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/api/games")
                    .then()
                    .statusCode(200)
                    .body("$", notNullValue());
        }

        @Test
        @Order(2)
        @DisplayName("Deve retornar 401 sem token")
        void deveRetornar401SemToken() {
            given()
                    .when()
                    .get("/api/games")
                    .then()
                    .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/games/{id}")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetGameByIdTests {

        @Test
        @Order(1)
        @DisplayName("Deve buscar jogo por ID com sucesso")
        void deveBuscarJogoPorIdComSucesso() {
            if (createdGameId != null) {
                given()
                        .header("Authorization", "Bearer " + adminToken)
                        .when()
                        .get("/api/games/" + createdGameId)
                        .then()
                        .statusCode(200)
                        .body("id", is(createdGameId))
                        .body("gameDate", notNullValue())
                        .body("released", notNullValue());
            }
        }

        @Test
        @Order(2)
        @DisplayName("Deve retornar 404 quando jogo não existir")
        void deveRetornar404QuandoJogoNaoExistir() {
            UUID nonExistentId = UUID.randomUUID();
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/api/games/" + nonExistentId)
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("PUT /api/games/{id}/release")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReleaseGameTests {

        @Test
        @Order(1)
        @DisplayName("Deve liberar lista com sucesso quando usuário for ADMIN")
        void deveLiberarListaComSucessoQuandoUsuarioForAdmin() {
            if (createdGameId != null) {
                given()
                        .header("Authorization", "Bearer " + adminToken)
                        .when()
                        .put("/api/games/" + createdGameId + "/release")
                        .then()
                        .statusCode(200)
                        .body("id", is(createdGameId))
                        .body("released", is(true));
            }
        }

        @Test
        @Order(2)
        @DisplayName("Deve retornar 404 quando jogo não existir")
        void deveRetornar404QuandoJogoNaoExistir() {
            UUID nonExistentId = UUID.randomUUID();
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .put("/api/games/" + nonExistentId + "/release")
                    .then()
                    .statusCode(404);
        }
    }
}

