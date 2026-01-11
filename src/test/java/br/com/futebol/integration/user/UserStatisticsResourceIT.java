package br.com.futebol.integration.user;

import br.com.futebol.domain.user.UserProfile;
import br.com.futebol.interfaces.auth.LoginRequest;
import br.com.futebol.interfaces.user.CreateUserRequest;
import br.com.futebol.interfaces.user.UpdateStatisticsRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@DisplayName("UserStatisticsResource - Testes de Integração")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserStatisticsResourceIT {

    private static String superAdminToken;
    private static String adminToken;
    private static String jogadorToken;
    private static String adminUserId;
    private static String jogadorUserId;
    private static String createdAdminId;
    private static String createdJogadorId;

    @BeforeAll
    static void setUp() {
        // Login como SUPER_ADMIN
        LoginRequest superAdminLogin = LoginRequest.builder()
                .email("admin@futebol.com")
                .password("admin123")
                .build();

        superAdminToken = given()
                .contentType(ContentType.JSON)
                .body(superAdminLogin)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        // Criar usuário ADMIN para testes
        CreateUserRequest adminRequest = CreateUserRequest.builder()
                .fullName("Admin Teste")
                .email("adminteste" + System.currentTimeMillis() + "@test.com")
                .password("senha123")
                .profile(UserProfile.ADMIN)
                .build();

        Response adminResponse = given()
                .contentType(ContentType.JSON)
                .body(adminRequest)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .extract()
                .response();

        createdAdminId = adminResponse.path("id");
        String adminEmail = adminResponse.path("email");

        // Login como ADMIN criado
        LoginRequest adminLogin = LoginRequest.builder()
                .email(adminEmail)
                .password("senha123")
                .build();

        adminToken = given()
                .contentType(ContentType.JSON)
                .body(adminLogin)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        adminUserId = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        // Criar usuário JOGADOR para testes
        CreateUserRequest jogadorRequest = CreateUserRequest.builder()
                .fullName("Jogador Teste")
                .email("jogadorteste" + System.currentTimeMillis() + "@test.com")
                .password("senha123")
                .profile(UserProfile.JOGADOR)
                .build();

        Response jogadorResponse = given()
                .contentType(ContentType.JSON)
                .body(jogadorRequest)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .extract()
                .response();

        createdJogadorId = jogadorResponse.path("id");
        String jogadorEmail = jogadorResponse.path("email");

        // Login como JOGADOR criado
        LoginRequest jogadorLogin = LoginRequest.builder()
                .email(jogadorEmail)
                .password("senha123")
                .build();

        jogadorToken = given()
                .contentType(ContentType.JSON)
                .body(jogadorLogin)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        jogadorUserId = given()
                .header("Authorization", "Bearer " + jogadorToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    @Nested
    @DisplayName("GET /api/users/me/statistics")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetMyStatisticsTests {

        @Test
        @Order(1)
        @DisplayName("Deve retornar estatísticas padrão para ADMIN")
        void deveRetornarEstatisticasPadraoParaAdmin() {
            given()
                .header("Authorization", "Bearer " + adminToken)
            .when()
                .get("/api/users/me/statistics")
            .then()
                .statusCode(200)
                .body("userId", is(adminUserId))
                .body("minutesPlayed", is("00:00:00"))
                .body("goals", is(0))
                .body("complaints", is(0))
                .body("victories", is(0))
                .body("draws", is(0))
                .body("defeats", is(0));
        }

        @Test
        @Order(2)
        @DisplayName("Deve retornar estatísticas padrão para JOGADOR")
        void deveRetornarEstatisticasPadraoParaJogador() {
            given()
                .header("Authorization", "Bearer " + jogadorToken)
            .when()
                .get("/api/users/me/statistics")
            .then()
                .statusCode(200)
                .body("userId", is(jogadorUserId))
                .body("minutesPlayed", is("00:00:00"))
                .body("goals", is(0));
        }

        @Test
        @Order(3)
        @DisplayName("Deve retornar 401 sem token")
        void deveRetornar401SemToken() {
            given()
            .when()
                .get("/api/users/me/statistics")
            .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}/statistics")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetUserStatisticsTests {

        @Test
        @Order(1)
        @DisplayName("ADMIN deve poder consultar próprias estatísticas")
        void adminDevePoderConsultarPropriasEstatisticas() {
            given()
                .header("Authorization", "Bearer " + adminToken)
            .when()
                .get("/api/users/" + adminUserId + "/statistics")
            .then()
                .statusCode(200)
                .body("userId", is(adminUserId));
        }

        @Test
        @Order(2)
        @DisplayName("JOGADOR deve poder consultar próprias estatísticas")
        void jogadorDevePoderConsultarPropriasEstatisticas() {
            given()
                .header("Authorization", "Bearer " + jogadorToken)
            .when()
                .get("/api/users/" + jogadorUserId + "/statistics")
            .then()
                .statusCode(200)
                .body("userId", is(jogadorUserId));
        }

        @Test
        @Order(3)
        @DisplayName("ADMIN não deve poder consultar estatísticas de outro usuário")
        void adminNaoDevePoderConsultarEstatisticasDeOutroUsuario() {
            given()
                .header("Authorization", "Bearer " + adminToken)
            .when()
                .get("/api/users/" + jogadorUserId + "/statistics")
            .then()
                .statusCode(403);
        }

        @Test
        @Order(4)
        @DisplayName("JOGADOR não deve poder consultar estatísticas de outro usuário")
        void jogadorNaoDevePoderConsultarEstatisticasDeOutroUsuario() {
            given()
                .header("Authorization", "Bearer " + jogadorToken)
            .when()
                .get("/api/users/" + adminUserId + "/statistics")
            .then()
                .statusCode(403);
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{userId}/statistics")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdateStatisticsTests {

        @Test
        @Order(1)
        @DisplayName("ADMIN deve poder atualizar minutos jogados")
        void adminDevePoderAtualizarMinutosJogados() {
            UpdateStatisticsRequest request = UpdateStatisticsRequest.builder()
                    .minutesPlayed("0:06:00")
                    .build();

            given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .put("/api/users/" + adminUserId + "/statistics")
            .then()
                .statusCode(200)
                .body("minutesPlayed", is("00:06:00"));
        }

        @Test
        @Order(2)
        @DisplayName("ADMIN deve poder somar minutos acumulativamente")
        void adminDevePoderSomarMinutosAcumulativamente() {
            UpdateStatisticsRequest request = UpdateStatisticsRequest.builder()
                    .minutesPlayed("0:05:30")
                    .build();

            given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .put("/api/users/" + adminUserId + "/statistics")
            .then()
                .statusCode(200)
                .body("minutesPlayed", is("00:11:30"));
        }

        @Test
        @Order(3)
        @DisplayName("ADMIN deve poder atualizar gols")
        void adminDevePoderAtualizarGols() {
            UpdateStatisticsRequest request = UpdateStatisticsRequest.builder()
                    .goals(7)
                    .build();

            given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .put("/api/users/" + adminUserId + "/statistics")
            .then()
                .statusCode(200)
                .body("goals", is(7));
        }

        @Test
        @Order(4)
        @DisplayName("ADMIN deve poder atualizar todas as estatísticas")
        void adminDevePoderAtualizarTodasEstatisticas() {
            UpdateStatisticsRequest request = UpdateStatisticsRequest.builder()
                    .minutesPlayed("0:10:00")
                    .goals(10)
                    .complaints(5)
                    .victories(8)
                    .draws(2)
                    .defeats(1)
                    .build();

            given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .put("/api/users/" + adminUserId + "/statistics")
            .then()
                .statusCode(200)
                .body("goals", is(10))
                .body("complaints", is(5))
                .body("victories", is(8))
                .body("draws", is(2))
                .body("defeats", is(1));
        }

        @Test
        @Order(5)
        @DisplayName("ADMIN deve poder atualizar estatísticas de JOGADOR")
        void adminDevePoderAtualizarEstatisticasDeJogador() {
            UpdateStatisticsRequest request = UpdateStatisticsRequest.builder()
                    .goals(5)
                    .build();

            given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .put("/api/users/" + jogadorUserId + "/statistics")
            .then()
                .statusCode(200)
                .body("goals", is(5));
        }

        @Test
        @Order(6)
        @DisplayName("JOGADOR não deve poder atualizar próprias estatísticas")
        void jogadorNaoDevePoderAtualizarPropriasEstatisticas() {
            UpdateStatisticsRequest request = UpdateStatisticsRequest.builder()
                    .goals(10)
                    .build();

            given()
                .header("Authorization", "Bearer " + jogadorToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .put("/api/users/" + jogadorUserId + "/statistics")
            .then()
                .statusCode(403);
        }

        @Test
        @Order(7)
        @DisplayName("ADMIN deve poder subtrair minutos")
        void adminDevePoderSubtrairMinutos() {
            UpdateStatisticsRequest request = UpdateStatisticsRequest.builder()
                    .minutesPlayed("-0:03:00")
                    .build();

            given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .put("/api/users/" + adminUserId + "/statistics")
            .then()
                .statusCode(200)
                .body("minutesPlayed", notNullValue());
        }

        @Test
        @Order(8)
        @DisplayName("Deve retornar 400 para formato de minutos inválido")
        void deveRetornar400ParaFormatoMinutosInvalido() {
            UpdateStatisticsRequest request = UpdateStatisticsRequest.builder()
                    .minutesPlayed("invalid")
                    .build();

            given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .put("/api/users/" + adminUserId + "/statistics")
            .then()
                .statusCode(400);
        }

        @Test
        @Order(9)
        @DisplayName("Deve retornar 400 para valores negativos em campos numéricos")
        void deveRetornar400ParaValoresNegativos() {
            UpdateStatisticsRequest request = UpdateStatisticsRequest.builder()
                    .goals(-1)
                    .build();

            given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .put("/api/users/" + adminUserId + "/statistics")
            .then()
                .statusCode(400);
        }
    }
}

