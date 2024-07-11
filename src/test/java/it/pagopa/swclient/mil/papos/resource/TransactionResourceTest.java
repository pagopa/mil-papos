package it.pagopa.swclient.mil.papos.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.model.TransactionDto;
import it.pagopa.swclient.mil.papos.service.TransactionService;
import it.pagopa.swclient.mil.papos.util.TestData;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

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

    @BeforeAll
    static void createTestObjects() {
        transactionEntity = TestData.getCorrectTransactionEntity();
        transactionDto = TestData.getCorrectTransactionDto();
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
}
