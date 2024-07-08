package it.pagopa.swclient.mil.papos;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.BulkLoadStatusEntity;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.model.WorkstationsDto;
import it.pagopa.swclient.mil.papos.resource.TerminalResource;
import it.pagopa.swclient.mil.papos.service.TerminalService;
import it.pagopa.swclient.mil.papos.util.TerminalTestData;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
@TestHTTPEndpoint(TerminalResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TerminalResourceTest {

    @InjectMock
    static TerminalService terminalService;

    static TerminalDto terminalDto;

    static WorkstationsDto workstationsDto;

    static TerminalEntity terminalEntity;

    static BulkLoadStatusEntity bulkLoadStatusEntity;

    @BeforeAll
    static void createTestObjects() {
        terminalDto = TerminalTestData.getCorrectTerminalDto();
        workstationsDto = TerminalTestData.getCorrectWorkstationDto();
        terminalEntity = TerminalTestData.getCorrectTerminalEntity();
        bulkLoadStatusEntity = TerminalTestData.getCorrectBulkLoadStatusEntity();
    }

    @Test
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
    void testBulkLoadTerminals_Success() {
        byte[] fileContent = "test content".getBytes();
        InputStream fileInputStream = new ByteArrayInputStream(fileContent);

        Mockito.when(terminalService.processBulkLoad(any(byte[].class)))
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
    void testBulkLoadTerminals_ServiceError() {
        byte[] fileContent = "test content".getBytes();
        InputStream fileInputStream = new ByteArrayInputStream(fileContent);

        Mockito.when(terminalService.processBulkLoad(any(byte[].class)))
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
    void testGetBulkLoadingStatusFile_200() {
        Mockito.when(terminalService.findBulkLoadStatus(any(String.class)))
                .thenReturn(Uni.createFrom().item(TerminalTestData.getCorrectBulkLoadStatusEntity()));

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

        bulkLoadStatusEntity = TerminalTestData.getCorrectBulkLoadStatusEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void testFindByPayeeCode_200() {
        Mockito.when(terminalService.getTerminalCountByAttribute("payeeCode", "payeeCode"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(terminalService.getTerminalListPagedByAttribute("payeeCode", "payeeCode", 0, 10))
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
    void testFindByPspId_200() {
        Mockito.when(terminalService.getTerminalCountByAttribute("pspId", "pspId"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(terminalService.getTerminalListPagedByAttribute("pspId", "pspId", 0, 10))
                .thenReturn(Uni.createFrom().item(new ArrayList<>()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "pspId")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    void testFindByWorkstation_200() {
        Mockito.when(terminalService.getTerminalCountByAttribute("workstation", "workstation"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(terminalService.getTerminalListPagedByAttribute("workstation", "workstation", 0, 10))
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
    void testFindByPayeeCode_500TC() {
        Mockito.when(terminalService.getTerminalCountByAttribute("payeeCode", "payeeCode"))
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
    void testFindByPayeeCode_500TLP() {
        Mockito.when(terminalService.getTerminalCountByAttribute("payeeCode", "payeeCode"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(terminalService.getTerminalListPagedByAttribute("payeeCode", "payeeCode", 0, 10))
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

        terminalEntity = TerminalTestData.getCorrectTerminalEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
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