package it.pagopa.swclient.mil.papos.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.SolutionEntity;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.model.SolutionDto;
import it.pagopa.swclient.mil.papos.service.SolutionService;
import it.pagopa.swclient.mil.papos.service.TerminalService;
import it.pagopa.swclient.mil.papos.util.TestData;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;

@QuarkusTest
@TestHTTPEndpoint(SolutionResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SolutionResourceTest {

    @InjectMock
    static SolutionService solutionService;

    @InjectMock
    static TerminalService terminalService;

    static SolutionEntity solutionEntity;

    static TerminalEntity terminalEntity;

    static SolutionDto solutionDto;

    @BeforeAll
    static void createTestObjects() {
        solutionEntity = TestData.getCorrectSolutionEntity();
        terminalEntity = TestData.getCorrectTerminalEntity();
        solutionDto = TestData.getCorrectSolutionDto();
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    void testCreateSolutionEndpoint_201() {
        Mockito.when(terminalService.findTerminal(anyString()))
                        .thenReturn(Uni.createFrom().item(terminalEntity));

        Mockito.when(solutionService.createSolution(any(SolutionDto.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .post("/")
                .then()
                .extract().response();

        Assertions.assertEquals(201, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    void testCreateSolutionError_500() {
        Mockito.when(terminalService.findTerminal(anyString()))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Mockito.when(solutionService.createSolution(solutionDto))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .post("/")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    void testFindByIdSolutionEndpoint_201() {
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .get("/66a79a4624356b00da07cfbf")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    void testFindByIdSolutionEndpoint_500() {
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .get("/66a79a4624356b00da07cfbf")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    void testFindByIdSolutionEndpoint_404() {
        solutionEntity = null;
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .get("/66a79a4624356b00da07cfbf")
                .then()
                .extract().response();

        solutionEntity = TestData.getCorrectSolutionEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    void testFindAll_200() {
        Mockito.when(solutionService.getSolutionsCount())
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(solutionService.findSolutions(anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().item(new ArrayList<>()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    void testFindSolutionsEndpoint_500TC() {
        Mockito.when(solutionService.getSolutionsCount())
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    void testFindSolutionsEndpoint_500TLP() {
        Mockito.when(solutionService.getSolutionsCount())
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(solutionService.findSolutions(anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "pos_service_provider" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByPspId_200() {
        Mockito.when(solutionService.getSolutionCountByAttribute("AGID_01", "AGID_01"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(solutionService.getSolutionsListPagedByAttribute("AGID_01", "AGID_01", 0, 10))
                .thenReturn(Uni.createFrom().item(new ArrayList<>()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByPspId_500TC() {
        Mockito.when(solutionService.getSolutionCountByAttribute(anyString(), anyString()))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByPspId_500TLP() {
        Mockito.when(solutionService.getSolutionCountByAttribute(anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(solutionService.getSolutionsListPagedByAttribute(anyString(), anyString(), anyInt(),
                anyInt()))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "public_administration" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "locationCode")
    })
    void testFindByLocationCode_200() {
        Mockito.when(solutionService.getSolutionCountByAttribute("locationCode", "locationCode"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(solutionService.getSolutionsListPagedByAttribute("locationCode", "locationCode", 0, 10))
                .thenReturn(Uni.createFrom().item(new ArrayList<>()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("locationCode", "locationCode")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByLocationCode")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }
    @Test
    @TestSecurity(user = "testUser", roles = { "public_administration" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "locationCrode")
    })
    void testFindByLocationCode_401() {
        Mockito.when(solutionService.getSolutionCountByAttribute("locationCode", "locationCode"))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("locationCode", "locationCode")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByLocationCode")
                .then()
                .extract().response();

        Assertions.assertEquals(401, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "locationCode")
    })
    void testFindByLocationCode_500TC() {
        Mockito.when(solutionService.getSolutionCountByAttribute("locationCode", "locationCode"))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("locationCode", "locationCode")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByLocationCode")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "locationCode")
    })
    void testFindByLocationCode_500TLP() {
        Mockito.when(solutionService.getSolutionCountByAttribute("locationCode", "locationCode"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(solutionService.getSolutionsListPagedByAttribute("locationCode", "locationCode", 0, 10))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("locationCode", "locationCode")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByLocationCode")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testDeleteSolution_204() {
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Mockito.when(solutionService.deleteSolution(any(SolutionEntity.class)))
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
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testDeleteSolution_404() {
        solutionEntity = null;
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .delete("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        solutionEntity = TestData.getCorrectSolutionEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testDeleteSolution_500FT() {
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .delete("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testDeleteSolution_500UT() {
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Mockito.when(solutionService.deleteSolution(any(SolutionEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .delete("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testUpdateSolution_204() {
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Mockito.when(solutionService.updateSolution(anyString(), any(SolutionDto.class), any(SolutionEntity.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .put("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(204, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testUpdateSolution_500SI() {

        Mockito.when(solutionService.findById(anyString()))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .put("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testUpdateSolution_500UT() {

        Mockito.when(solutionService.findById(anyString()))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Mockito.when(solutionService.updateSolution(anyString(), any(SolutionDto.class), any(SolutionEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(solutionDto)
                .when()
                .put("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

}
