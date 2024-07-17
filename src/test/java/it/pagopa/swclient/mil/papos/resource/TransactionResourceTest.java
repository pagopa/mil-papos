package it.pagopa.swclient.mil.papos.resource;

import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.model.TransactionDto;
import it.pagopa.swclient.mil.papos.model.UpdateTransactionDto;
import it.pagopa.swclient.mil.papos.service.TransactionService;
import it.pagopa.swclient.mil.papos.util.TestData;
import it.pagopa.swclient.mil.papos.util.Utility;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
@TestHTTPEndpoint(TransactionResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionResourceTest {

    @InjectMock
    static TransactionService transactionService;

    static TransactionEntity transactionEntity;

    static TransactionDto transactionDto;

    static UpdateTransactionDto updateTransactionDto;

    @BeforeAll
    static void createTestObjects() {
        transactionEntity = TestData.getCorrectTransactionEntity();
        transactionDto = TestData.getCorrectTransactionDto();
        updateTransactionDto = TestData.getCorrectUpdateTransactionDto();
    }

    @Test
    void testCreateTransactionEndpoint_204() {
        Mockito.when(transactionService.createTransaction(any(TransactionDto.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(transactionDto)
                .when()
                .post("/")
                .then()
                .extract().response();

        Assertions.assertEquals(204, response.statusCode());
    }

    @Test
    void testCreateTerminalError_500() {
        Mockito.when(transactionService.createTransaction(any(TransactionDto.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(transactionDto)
                .when()
                .post("/")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void testFindByPayeeCode_200() {
        Mockito.when(transactionService.getTransactionCountByAttribute("payeeCode", "payeeCode"))
                .thenReturn(Uni.createFrom().item(10L));
        Sort sort = Sort.by("creationTimestamp", Sort.Direction.Descending);

        Mockito.when(transactionService.getTransactionListPagedByAttribute("payeeCode", "payeeCode",
                        Utility.convertStringToDate("2023-01-01", true),
                        Utility.convertStringToDate("2023-12-31", false),
                        sort, 0, 10))
                .thenReturn(Uni.createFrom().item(new ArrayList<>()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "payeeCode")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "desc")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPayeeCode")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    void testFindByPayeeCode_500TC() {
        Mockito.when(transactionService.getTransactionCountByAttribute("payeeCode", "payeeCode"))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "payeeCode")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "asc")
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
        Mockito.when(transactionService.getTransactionCountByAttribute("payeeCode", "payeeCode"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(transactionService.getTransactionListPagedByAttribute(any(String.class), any(String.class),
                        any(Date.class), any(Date.class), any(Sort.class), any(Integer.class), any(Integer.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "payeeCode")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "asc")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPayeeCode")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void testFindByPayeeCode_DateParsingError() {
        Mockito.when(transactionService.getTransactionCountByAttribute("payeeCode", "payeeCode"))
                .thenReturn(Uni.createFrom().item(10L));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "payeeCode")
                .queryParam("startDate", "invalid-date")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "asc")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPayeeCode")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void testFindByPspId_200() {
        Mockito.when(transactionService.getTransactionCountByAttribute("pspId", "pspId"))
                .thenReturn(Uni.createFrom().item(10L));
        Sort sort = Sort.by("creationTimestamp", Sort.Direction.Ascending);

        Mockito.when(transactionService.getTransactionListPagedByAttribute("pspId", "pspId",
                        Utility.convertStringToDate("2023-01-01", true),
                        Utility.convertStringToDate("2023-12-31", false),
                        sort, 0, 10))
                .thenReturn(Uni.createFrom().item(new ArrayList<>()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "pspId")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "asc")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    void testFindByPspId_500TC() {
        Mockito.when(transactionService.getTransactionCountByAttribute("pspId", "pspId"))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "pspId")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "asc")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void testFindByPspId_500TLP() {
        Mockito.when(transactionService.getTransactionCountByAttribute("pspId", "pspId"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(transactionService.getTransactionListPagedByAttribute(any(String.class), any(String.class),
                        any(Date.class), any(Date.class), any(Sort.class), any(Integer.class), any(Integer.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "pspId")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "asc")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void testDeleteTransaction_204() {
        Mockito.when(transactionService.findTransaction(any(String.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Mockito.when(transactionService.deleteTransaction(any(TransactionEntity.class)))
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
    void testDeleteTransaction_404() {
        transactionEntity = null;
        Mockito.when(transactionService.findTransaction(any(String.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(transactionDto)
                .when()
                .delete("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        transactionEntity = TestData.getCorrectTransactionEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void testDeleteTransaction_500FT() {
        Mockito.when(transactionService.findTransaction(any(String.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(transactionDto)
                .when()
                .delete("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void testDeleteTransaction_500UT() {
        Mockito.when(transactionService.findTransaction(any(String.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Mockito.when(transactionService.deleteTransaction(any(TransactionEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(transactionDto)
                .when()
                .delete("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void testUpdateTransaction_204() {
        Mockito.when(transactionService.findTransaction(any(String.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Mockito.when(transactionService.updateTransaction(any(String.class), any(UpdateTransactionDto.class), any(TransactionEntity.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(updateTransactionDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(204, response.statusCode());
    }

    @Test
    void testUpdateTransaction_404() {
        transactionEntity = null;
        Mockito.when(transactionService.findTransaction(any(String.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(updateTransactionDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        transactionEntity = TestData.getCorrectTransactionEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    void testUpdateTransaction_500FT() {
        Mockito.when(transactionService.findTransaction(any(String.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(updateTransactionDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    void testUpdateTransaction_500UT() {
        Mockito.when(transactionService.findTransaction(any(String.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Mockito.when(transactionService.updateTransaction(any(String.class), any(UpdateTransactionDto.class), any(TransactionEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(updateTransactionDto)
                .when()
                .patch("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }
}
