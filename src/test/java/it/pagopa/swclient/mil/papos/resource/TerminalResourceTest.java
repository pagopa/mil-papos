package it.pagopa.swclient.mil.papos.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.BulkLoadStatusEntity;
import it.pagopa.swclient.mil.papos.dao.SolutionEntity;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.model.WorkstationsDto;
import it.pagopa.swclient.mil.papos.service.SolutionService;
import it.pagopa.swclient.mil.papos.service.TerminalService;
import it.pagopa.swclient.mil.papos.util.TestData;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static it.pagopa.swclient.mil.papos.util.TestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

@QuarkusTest
@TestHTTPEndpoint(TerminalResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TerminalResourceTest {

    @InjectMock
    static TerminalService terminalService;

    @InjectMock
    static SolutionService solutionService;

    static ObjectMapper objectMapper;

    static TerminalDto terminalDto;

    static WorkstationsDto workstationsDto;

    static TerminalEntity terminalEntity;

    static BulkLoadStatusEntity bulkLoadStatusEntity;

    static SolutionEntity solutionEntity;

    @BeforeAll
    static void createTestObjects() {
        terminalDto = TestData.getCorrectTerminalDto();
        workstationsDto = TestData.getCorrectWorkstationDto();
        terminalEntity = TestData.getCorrectTerminalEntity();
        bulkLoadStatusEntity = TestData.getCorrectBulkLoadStatusEntity();
        solutionEntity = TestData.getCorrectSolutionEntity();
        objectMapper = new ObjectMapper();
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "TMIL0101")
    })
    void testCreateTerminalEndpoint_201() {
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

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
            @Claim(key = "sub", value = "TMIL0101")
    })
    void testCreateTerminalError_500() {
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

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
            @Claim(key = "sub", value = "AsdID_01")
    })
    void testCreateTerminalEndpoint_401() {
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

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

        Assertions.assertEquals(401, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "TMIL0101")
    })
    void testCreateTerminalEndpoint_404() {
        solutionEntity = null;
        Mockito.when(solutionService.findById(any(String.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(terminalDto)
                .when()
                .post("/")
                .then()
                .extract().response();

        solutionEntity = TestData.getCorrectSolutionEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testBulkLoadTerminals_Success() {
        String fileContent = "[{\"pspId\": \"AGID_01\", \"terminalId\": \"term1\"}, {\"pspId\": \"AGID_01\", \"terminalId\": \"term2\"}]";
        InputStream fileInputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

        Mockito.when(terminalService.processBulkLoad(mockedListTerminalDto()))
                .thenReturn(Uni.createFrom().item(new BulkLoadStatusEntity()));

        Response response = given()
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .multiPart("file", "file.json", fileInputStream, MediaType.APPLICATION_OCTET_STREAM)
                .when()
                .post("/bulkload")
                .then()
                .extract().response();

        Assertions.assertEquals(202, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testBulkLoadTerminals_FileNull() {
        Response response = given()
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .contentType("multipart/form-data")
                .when()
                .post("/bulkload")
                .then()
                .extract().response();

        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testBulkLoadTerminals_FileEmpty() {
        InputStream fileInputStream = new ByteArrayInputStream(new byte[0]);

        Response response = given()
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .multiPart("file", "file.json", fileInputStream, MediaType.APPLICATION_OCTET_STREAM)
                .when()
                .post("/bulkload")
                .then()
                .extract().response();

        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testBulkLoadTerminals_ServiceError() {
        byte[] fileContent = "malformed content".getBytes();
        InputStream fileInputStream = new ByteArrayInputStream(fileContent);

        Mockito.when(terminalService.processBulkLoad(mockedListTerminalDto()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Service error")));

        Response response = given()
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .multiPart("file", "file.json", fileInputStream, MediaType.APPLICATION_OCTET_STREAM)
                .when()
                .post("/bulkload")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testBulkLoadTerminals_InternalServerErrorException() {
        String fileContent = "[{\"pspId\": \"AGID_01\", \"terminalId\": \"term1\"}, {\"pspId\": \"AGID_01\", \"terminalId\": \"term2\"}]";
        InputStream fileInputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

        Mockito.when(terminalService.processBulkLoad(anyList()))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.MULTIPART)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .multiPart("file", "file.json", fileInputStream, MediaType.APPLICATION_OCTET_STREAM)
                .when()
                .post("/bulkload")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.getStatusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testGetBulkLoadingStatusFile_200() {
        Mockito.when(terminalService.findBulkLoadStatus(any(String.class)))
                .thenReturn(Uni.createFrom().item(TestData.getCorrectBulkLoadStatusEntity()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .when()
                .get("/bulkload/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testGetBulkLoadingStatusFile_500() {
        Mockito.when(terminalService.findBulkLoadStatus(any(String.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .when()
                .get("/bulkload/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testGetBulkLoadingStatusFile_404() {
        bulkLoadStatusEntity = null;
        Mockito.when(terminalService.findBulkLoadStatus(any(String.class)))
                .thenReturn(Uni.createFrom().item(bulkLoadStatusEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .when()
                .get("/bulkload/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        bulkLoadStatusEntity = TestData.getCorrectBulkLoadStatusEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "payeeCode")
    })
    void testFindByPayeeCode_200() {
        Mockito.when(solutionService.findAllByLocationOrPsp(any(String.class), any(String.class)))
                .thenReturn(Uni.createFrom().item(mockedListSolution()));

        Mockito.when(terminalService.countBySolutionIds(Collections.singletonList("payeeCode")))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(terminalService.findBySolutionIds(Collections.singletonList("payeeCode"), 0, 10))
                .thenReturn(Uni.createFrom().item(mockedList()));

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
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByPspId_200() {
        Mockito.when(solutionService.findAllByLocationOrPsp(any(String.class), any(String.class)))
                .thenReturn(Uni.createFrom().item(mockedListSolution()));

        Mockito.when(terminalService.countBySolutionIds(Collections.singletonList("pspId")))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(terminalService.findBySolutionIds(Collections.singletonList("pspId"), 0, 10))
                .thenReturn(Uni.createFrom().item(mockedList()));

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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "payeeCode")
    })
    void testFindByPayeeCode_500FABL() {
        Mockito.when(solutionService.findAllByLocationOrPsp(any(String.class), any(String.class)))
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "payeeCode")
    })
    void testFindByPayeeCode_404() {
        List<SolutionEntity> empty = new ArrayList<>();
        Mockito.when(solutionService.findAllByLocationOrPsp(any(String.class), any(String.class)))
                .thenReturn(Uni.createFrom().item(empty));

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

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "payeeCode")
    })
    void testFindByPayeeCode_500CBS() {
        List<SolutionEntity> mockedSolutions = mockedListSolution();
        Mockito.when(solutionService.findAllByLocationOrPsp(any(String.class), any(String.class)))
                .thenReturn(Uni.createFrom().item(mockedSolutions));

        List<String> solutionIds = mockedSolutions.stream()
                .map(solution -> solution.id.toString())
                .toList();

        Mockito.when(terminalService.countBySolutionIds(solutionIds))
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "payeeCode")
    })
    void testFindByPayeeCode_500FBS() {
        List<SolutionEntity> mockedSolutions = mockedListSolution();
        Mockito.when(solutionService.findAllByLocationOrPsp(any(String.class), any(String.class)))
                .thenReturn(Uni.createFrom().item(mockedSolutions));

        List<String> solutionIds = mockedSolutions.stream()
                .map(solution -> solution.id.toString())
                .toList();

        Mockito.when(terminalService.countBySolutionIds(solutionIds))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(terminalService.findBySolutionIds(solutionIds, 0, 10))
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByWorkstation_200() {
        Mockito.when(terminalService.getTerminalCountByWorkstation("workstation"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(terminalService.getTerminalListPagedByWorkstation("workstation", 0, 10))
                .thenReturn(Uni.createFrom().item(new ArrayList<>()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("workstation", "workstation")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByWorkstation")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByWorkstation_500() {
        Mockito.when(terminalService.getTerminalCountByWorkstation("workstation"))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("workstation", "workstation")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByWorkstation")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByWorkstation_500TLP() {
        Mockito.when(terminalService.getTerminalCountByWorkstation("workstation"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(terminalService.getTerminalListPagedByWorkstation("workstation", 0, 10))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("workstation", "workstation")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByWorkstation")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testUpdateWorkstations_204() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Mockito.when(terminalService.updateWorkstations(any(WorkstationsDto.class), any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(workstationsDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa/updateWorkstations")
                .then()
                .extract().response();

        Assertions.assertEquals(204, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testUpdateWorkstations_404() {
        terminalEntity = null;
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(workstationsDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa/updateWorkstations")
                .then()
                .extract().response();

        terminalEntity = TestData.getCorrectTerminalEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testUpdateWorkstations_500FT() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(workstationsDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa/updateWorkstations")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testUpdateWorkstations_500UT() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Mockito.when(terminalService.updateWorkstations(any(WorkstationsDto.class), any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(workstationsDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa/updateWorkstations")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
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
            @Claim(key = "sub", value = "AGID_01")
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

        terminalEntity = TestData.getCorrectTerminalEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
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
            @Claim(key = "sub", value = "AGID_01")
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
            @Claim(key = "sub", value = "AGID_01")
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
            @Claim(key = "sub", value = "AGID_01")
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

        terminalEntity = TestData.getCorrectTerminalEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
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
            @Claim(key = "sub", value = "AGID_01")
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