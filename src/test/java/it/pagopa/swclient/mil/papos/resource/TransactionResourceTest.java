package it.pagopa.swclient.mil.papos.resource;

import io.quarkus.panache.common.Sort;
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
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.model.TransactionDto;
import it.pagopa.swclient.mil.papos.model.UpdateTransactionDto;
import it.pagopa.swclient.mil.papos.service.SolutionService;
import it.pagopa.swclient.mil.papos.service.TerminalService;
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
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.*;

@QuarkusTest
@TestHTTPEndpoint(TransactionResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionResourceTest {

    @InjectMock
    static TransactionService transactionService;

    @InjectMock
    static TerminalService terminalService;

    @InjectMock
    static SolutionService solutionService;

    static TransactionEntity transactionEntity;

    static TerminalEntity terminalEntity;

    static SolutionEntity solutionEntity;

    static TransactionDto transactionDto;

    static UpdateTransactionDto updateTransactionDto;

    @BeforeAll
    static void createTestObjects() {
        transactionEntity = TestData.getCorrectTransactionEntity();
        terminalEntity = TestData.getCorrectTerminalEntity();
        solutionEntity = TestData.getCorrectSolutionEntity();
        transactionDto = TestData.getCorrectTransactionDto();
        updateTransactionDto = TestData.getCorrectUpdateTransactionDto();
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
    void testCreateTransactionEndpoint_204() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
    void testCreateTransactionEndpoint_500FT() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
    void testCreateTransactionError_500() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AID_01")
    })
    void testCreateTransactionError_401() {
        Mockito.when(terminalService.findTerminal(any(String.class)))
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

        Assertions.assertEquals(401, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
    void testCreateTransactionError_404() {
        terminalEntity = null;
        Mockito.when(terminalService.findTerminal(any(String.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .body(transactionDto)
                .when()
                .post("/")
                .then()
                .extract().response();

        terminalEntity = TestData.getCorrectTerminalEntity();
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
    void testFindByPayeeCode_200() {
        Mockito.when(transactionService.getTransactionCountByAttribute("payeeCode", "06534340721"))
                .thenReturn(Uni.createFrom().item(10L));
        Sort sort = Sort.by("creationTimestamp", Sort.Direction.Descending);

        Mockito.when(transactionService.getTransactionListPagedByAttribute("payeeCode", "06534340721",
                        Utility.convertStringToDate("2023-01-01", true),
                        Utility.convertStringToDate("2023-12-31", false),
                        sort, 0, 10))
                .thenReturn(Uni.createFrom().item(new ArrayList<>()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "06534340721")
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
    void testFindByPayeeCode_500TC() {
        Mockito.when(transactionService.getTransactionCountByAttribute("payeeCode", "06534340721"))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "06534340721")
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
    void testFindByPayeeCode_404() {
        Mockito.when(transactionService.getTransactionCountByAttribute("payeeCode", "06534340721"))
                .thenReturn(Uni.createFrom().item(0L));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "06534340721")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "asc")
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
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByPayeeCode_500TLP() {
        Mockito.when(transactionService.getTransactionCountByAttribute("payeeCode", "AGID_01"))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(transactionService.getTransactionListPagedByAttribute(any(String.class), any(String.class),
                        any(Date.class), any(Date.class), any(Sort.class), any(Integer.class), any(Integer.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "AGID_01")
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
    void testFindByPayeeCode_DateParsingError() {
        Mockito.when(transactionService.getTransactionCountByAttribute("payeeCode", "payeeCode"))
                .thenReturn(Uni.createFrom().item(10L));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("payeeCode", "06534340721")
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
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "TMIL0101")
    })
    void testFindByPspId_200() {
        Mockito.when(solutionService.findAllByLocationOrPsp("pspId", "TMIL0101"))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        List<String> solutionIds = List.of("66a79a4624356b00da07cfbf", "66a79a4624346b20da01cfbf");
        Mockito.when(terminalService.findAllBySolutionIds(solutionIds))
                .thenReturn(Uni.createFrom().item(TestData.mockedList()));

        List<String> terminalUuids = TestData.mockedList().stream()
                .map(TerminalEntity::getTerminalUuid)
                .toList();
        Mockito.when(transactionService.getTransactionCountByTerminals(terminalUuids))
                .thenReturn(Uni.createFrom().item(10L));

        Sort sort = Sort.by("creationTimestamp", Sort.Direction.Ascending);
        Mockito.when(transactionService.getTransactionListPagedByTerminals(
                        terminalUuids,
                        Utility.convertStringToDate("2023-01-01", true),
                        Utility.convertStringToDate("2023-12-31", false),
                        sort, 0, 10))
                .thenReturn(Uni.createFrom().item(TestData.mockedListTransaction()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "TMIL0101")
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
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByPspId_500FABLP() {
        Mockito.when(solutionService.findAllByLocationOrPsp(anyString(), anyString()))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
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
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByPspId_404FABLP() {
        List<SolutionEntity> empty = new ArrayList<>();
        Mockito.when(solutionService.findAllByLocationOrPsp("pspId", "pspId"))
                .thenReturn(Uni.createFrom().item(empty));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "asc")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByPspId_500FABSI() {
        Mockito.when(solutionService.findAllByLocationOrPsp(anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        List<String> solutionIds = List.of("66a79a4624356b00da07cfbf", "66a79a4624346b20da01cfbf");
        Mockito.when(terminalService.findAllBySolutionIds(solutionIds))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
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
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByPspId_404FABSI() {
        Mockito.when(solutionService.findAllByLocationOrPsp(anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        List<String> solutionIds = List.of("66a79a4624356b00da07cfbf", "66a79a4624346b20da01cfbf");
        List<TerminalEntity> empty = new ArrayList<>();
        Mockito.when(terminalService.findAllBySolutionIds(solutionIds))
                .thenReturn(Uni.createFrom().item(empty));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "asc")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "TMIL0101")
    })
    void testFindByPspId_500TCBT() {
        Mockito.when(solutionService.findAllByLocationOrPsp(anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        List<String> solutionIds = List.of("66a79a4624356b00da07cfbf", "66a79a4624346b20da01cfbf");
        Mockito.when(terminalService.findAllBySolutionIds(solutionIds))
                .thenReturn(Uni.createFrom().item(TestData.mockedList()));

        List<String> terminalUuids = TestData.mockedList().stream()
                .map(TerminalEntity::getTerminalUuid)
                .toList();
        Mockito.when(transactionService.getTransactionCountByTerminals(terminalUuids))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "TMIL0101")
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
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindByPspId_404() {
        Mockito.when(solutionService.findAllByLocationOrPsp(anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        List<String> solutionIds = List.of("66a79a4624356b00da07cfbf", "66a79a4624346b20da01cfbf");
        Mockito.when(terminalService.findAllBySolutionIds(solutionIds))
                .thenReturn(Uni.createFrom().item(TestData.mockedList()));

        List<String> terminalUuids = TestData.mockedList().stream()
                .map(TerminalEntity::getTerminalUuid)
                .toList();
        Mockito.when(transactionService.getTransactionCountByTerminals(terminalUuids))
                .thenReturn(Uni.createFrom().item(0L));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "asc")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "TMIL0101")
    })
    void testFindByPspId_500TLPBA() {
        Mockito.when(solutionService.findAllByLocationOrPsp(anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        Mockito.when(terminalService.findAllBySolutionIds(anyList()))
                .thenReturn(Uni.createFrom().item(TestData.mockedList()));

        Mockito.when(transactionService.getTransactionCountByTerminals(anyList()))
                .thenReturn(Uni.createFrom().item(10L));

        Mockito.when(transactionService.getTransactionListPagedByTerminals(
                        anyList(),
                        any(Date.class),
                        any(Date.class),
                        any(Sort.class),
                        anyInt(),
                        anyInt()))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "TMIL0101")
                .queryParam("startDate", "2023-01-01")
                .queryParam("endDate", "2023-12-31")
                .queryParam("sortStrategy", "desc")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/findByPspId")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
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
    @TestSecurity(user = "testUser", roles = {"public_administration"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "06534340721")
    })
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

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testFindTransaction_200() {
        Mockito.when(transactionService.findTransaction(any(String.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .and()
                .when()
                .get("/d43d21a5-f8a7-4a68-8320-60b8f342c4aa")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testLatestTransaction_200() {
        Mockito.when(solutionService.findAllByPspId(anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        Mockito.when(terminalService.findAllBySolutionIdAndTerminalId(anyList(), anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedList()));

        Mockito.when(transactionService.findLatestByTerminalUuidAndStatus(anyList(), anyString(), any(Sort.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("terminalId", "34523860")
                .queryParam("status", "CLOSE_OK")
                .and()
                .when()
                .get("/latest")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testLatestTransaction_500FABP() {
        Mockito.when(solutionService.findAllByPspId(anyString()))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("terminalId", "34523860")
                .queryParam("status", "CLOSE_OK")
                .and()
                .when()
                .get("/latest")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testLatestTransaction_404() {
        List<SolutionEntity> empty = new ArrayList<>();
        Mockito.when(solutionService.findAllByPspId(anyString()))
                .thenReturn(Uni.createFrom().item(empty));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("terminalId", "34523860")
                .queryParam("status", "CLOSE_OK")
                .and()
                .when()
                .get("/latest")
                .then()
                .extract().response();

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testLatestTransaction_500FABST() {
        Mockito.when(solutionService.findAllByPspId(anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        Mockito.when(terminalService.findAllBySolutionIdAndTerminalId(anyList(), anyString()))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("terminalId", "34523860")
                .queryParam("status", "CLOSE_OK")
                .and()
                .when()
                .get("/latest")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testLatestTransaction_404FABST() {
        Mockito.when(solutionService.findAllByPspId(anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        List<TerminalEntity> empty = new ArrayList<>();
        Mockito.when(terminalService.findAllBySolutionIdAndTerminalId(anyList(), anyString()))
                .thenReturn(Uni.createFrom().item(empty));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("terminalId", "34523860")
                .queryParam("status", "CLOSE_OK")
                .and()
                .when()
                .get("/latest")
                .then()
                .extract().response();

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testLatestTransaction_500FLBTUS() {
        Mockito.when(solutionService.findAllByPspId(anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        Mockito.when(terminalService.findAllBySolutionIdAndTerminalId(anyList(), anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedList()));

        Mockito.when(transactionService.findLatestByTerminalUuidAndStatus(anyList(), anyString(), any(Sort.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("terminalId", "34523860")
                .queryParam("status", "CLOSE_OK")
                .and()
                .when()
                .get("/latest")
                .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"pos_service_provider"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = "AGID_01")
    })
    void testLatestTransaction_404FLBTUS() {
        Mockito.when(solutionService.findAllByPspId(anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedListSolution()));

        Mockito.when(terminalService.findAllBySolutionIdAndTerminalId(anyList(), anyString()))
                .thenReturn(Uni.createFrom().item(TestData.mockedList()));

        transactionEntity = null;
        Mockito.when(transactionService.findLatestByTerminalUuidAndStatus(anyList(), anyString(), any(Sort.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .header("RequestId", "1a2b3c4d-5e6f-789a-bcde-f0123456789a")
                .queryParam("pspId", "AGID_01")
                .queryParam("terminalId", "34523860")
                .queryParam("status", "CLOSE_OK")
                .and()
                .when()
                .get("/latest")
                .then()
                .extract().response();

        transactionEntity = TestData.getCorrectTransactionEntity();
        Assertions.assertEquals(404, response.statusCode());
    }
}
