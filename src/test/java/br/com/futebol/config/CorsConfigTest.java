package br.com.futebol.config;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@QuarkusTest
public class CorsConfigTest {

    @Test
    public void testOptionsPreflightRequest() {
        RestAssured.given()
                .header(new Header("Origin", "http://localhost:3000"))
                .header(new Header("Access-Control-Request-Method", "GET"))
                .header(new Header("Access-Control-Request-Headers", "Content-Type"))
                .when()
                .options("/api/users")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", notNullValue())
                .header("Access-Control-Allow-Methods", notNullValue())
                .header("Access-Control-Allow-Headers", notNullValue())
                .header("Access-Control-Max-Age", notNullValue());
    }

    @Test
    public void testOptionsPreflightWithAllowedOrigin() {
        RestAssured.given()
                .header(new Header("Origin", "http://localhost:3000"))
                .header(new Header("Access-Control-Request-Method", "POST"))
                .when()
                .options("/api/users")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", anyOf(equalTo("http://localhost:3000"), equalTo("*")));
    }

    @Test
    public void testGetRequestWithCorsHeaders() {
        RestAssured.given()
                .header(new Header("Origin", "http://localhost:3000"))
                .when()
                .get("/q/health/ready")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", notNullValue());
    }

    @Test
    public void testOptionsPreflightForHealthEndpoint() {
        RestAssured.given()
                .header(new Header("Origin", "http://localhost:3000"))
                .header(new Header("Access-Control-Request-Method", "GET"))
                .when()
                .options("/q/health/ready")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", notNullValue())
                .header("Access-Control-Allow-Methods", notNullValue());
    }
}

