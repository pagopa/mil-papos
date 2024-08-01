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
import it.pagopa.swclient.mil.papos.model.SolutionDto;
import it.pagopa.swclient.mil.papos.service.SolutionService;
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

    static SolutionEntity solutionEntity;

    static SolutionDto solutionDto;

    @BeforeAll
    static void createTestObjects() {
        solutionEntity = TestData.getCorrectSolutionEntity();
        solutionDto = TestData.getCorrectSolutionDto();
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    void testCreateSolutionEndpoint_201() {
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

        Mockito.when(solutionService.findSolutions(anyString(), anyInt(), anyInt()))
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

        Mockito.when(solutionService.findSolutions(anyString(), anyInt(), anyInt()))
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
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "GID")
    })
    void testFindByPspId_401() {

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

        Assertions.assertEquals(401, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "payeeCode")
    })
    void testFindByPayeeCode_200() {
        Mockito.when(solutionService.getSolutionCountByAttribute("payeeCode", "payeeCode"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(solutionService.getSolutionsListPagedByAttribute("payeeCode", "payeeCode", 0, 10))
                .thenReturn(Uni.createFrom().item(new ArrayList<>()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "payeeCode")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPayeeCode")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "payeeCode")
    })
    void testFindByPayeeCode_500TC() {
        Mockito.when(solutionService.getSolutionCountByAttribute("payeeCode", "payeeCode"))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "payeeCode")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPayeeCode")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = { "mil_papos_admin" })
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "payeeCode")
    })
    void testFindByPayeeCode_500TLP() {
        Mockito.when(solutionService.getSolutionCountByAttribute("payeeCode", "payeeCode"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(solutionService.getSolutionsListPagedByAttribute("payeeCode", "payeeCode", 0, 10))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "payeeCode")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPayeeCode")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

}
