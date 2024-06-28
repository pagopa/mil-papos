package it.pagopa.swclient.mil.papos;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.resource.TerminalResource;
import it.pagopa.swclient.mil.papos.service.TerminalService;
import it.pagopa.swclient.mil.papos.util.TerminalTestData;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
@TestHTTPEndpoint(TerminalResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TerminalResourceTest {

    @InjectMock
    static TerminalService terminalService;

    static TerminalDto terminalDto;

    static TerminalEntity terminalEntity;

    @BeforeAll
    static void createTestObjects() {
        terminalDto = TerminalTestData.getCorrectTerminalDto();
        terminalEntity = TerminalTestData.getCorrectTerminalEntity();
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "1234567890")
    })
    void testCreateTerminalEndpoint_201() {
        Mockito.when(terminalService.createTerminal(any(TerminalDto.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(terminalDto)
                .when()
                .post("/")
                .then()
                .extract().response();

        Assertions.assertEquals(201, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "1234567890")
    })
    void testCreateTerminalError_500() {
        Mockito.when(terminalService.createTerminal(any(TerminalDto.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(terminalDto)
                .when()
                .post("/")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "1234567890")
    })
    void testUpdateTerminal_204() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Mockito.when(terminalService.updateTerminal(any(String.class), any(TerminalDto.class), any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(terminalDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(204, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "1234567890")
    })
    void testUpdateTerminal_404() {
        terminalEntity = null;
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(terminalDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        terminalEntity = TerminalTestData.getCorrectTerminalEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "1234567890")
    })
    void testUpdateTerminal_500FT() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(terminalDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "1234567890")
    })
    void testUpdateTerminal_500UT() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Mockito.when(terminalService.updateTerminal(any(String.class), any(TerminalDto.class), any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(terminalDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "1234567890")
    })
    void testDeleteTerminal_204() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Mockito.when(terminalService.deleteTerminal(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().voidItem());

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .when()
                .delete("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(204, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "1234567890")
    })
    void testDeleteTerminal_404() {
        terminalEntity = null;
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(terminalDto)
                .when()
                .delete("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        terminalEntity = TerminalTestData.getCorrectTerminalEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "1234567890")
    })
    void testDeleteTerminal_500FT() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(terminalDto)
                .when()
                .delete("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "1234567890")
    })
    void testDeleteTerminal_500UT() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Mockito.when(terminalService.deleteTerminal(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(terminalDto)
                .when()
                .delete("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }
}