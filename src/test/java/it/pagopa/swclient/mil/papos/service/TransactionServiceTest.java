package it.pagopa.swclient.mil.papos.service;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.dao.TransactionRepository;
import it.pagopa.swclient.mil.papos.model.TransactionDto;
import it.pagopa.swclient.mil.papos.model.UpdateTransactionDto;
import it.pagopa.swclient.mil.papos.util.TestData;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionServiceTest {

    @InjectMock
    static TransactionRepository transactionRepository;

    static TransactionEntity transactionEntity;

    static TransactionDto transactionDto;

    static UpdateTransactionDto updateTransactionDto;

    static TransactionService transactionService;

    @BeforeAll
    static void createTestObjects() {
        transactionEntity = TestData.getCorrectTransactionEntity();
        transactionDto = TestData.getCorrectTransactionDto();
        updateTransactionDto = TestData.getCorrectUpdateTransactionDto();
        transactionService = new TransactionService(transactionRepository);
    }

    @Test
    void testCreateTransaction_Success() {
        Mockito.when(transactionRepository.persist(any(TransactionEntity.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Uni<TransactionEntity> result = transactionService.createTransaction(transactionDto);

        result.subscribe()
                .with(entity -> Assertions.assertEquals(transactionEntity, entity));
    }

    @Test
    void testCreateTransaction_Failure() {
        Mockito.when(transactionRepository.persist(any(TransactionEntity.class)))
                .thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));

        Uni<TransactionEntity> result = transactionService.createTransaction(transactionDto);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(InternalServerErrorException.class);
    }

    @Test
    void testGetTransactionCountByAttribute_Success() {
        Mockito.when(transactionRepository.count(anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(10L));

        Uni<Long> result = transactionService.getTransactionCountByAttribute("pspId", "psp1");

        result.subscribe()
                .with(count -> Assertions.assertEquals(10L, count));
    }

    @Test
    void testGetTransactionListPagedByAttribute_Success() throws ParseException {
        ReactivePanacheQuery<TransactionEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.page(anyInt(), anyInt())).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(TestData.mockedListTransaction()));

        String queryStr = "{ transactionId: ?1, creationTimestamp: { $gte: ?2, $lte: ?3 } }";
        Sort sort = Sort.by("creationTimestamp", Sort.Direction.Ascending);

        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-01");
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2024-12-31");

        Mockito.when(transactionRepository.find(queryStr, sort,"transactionId", startDate, endDate))
                .thenReturn(query);

        Uni<List<TransactionEntity>> result = transactionService.getTransactionListPagedByAttribute(
                "transactionId",
                "transactionId",
                startDate,
                endDate,
                sort,
                0,
                10);

        result.subscribe().with(list -> Assertions.assertEquals(TestData.mockedListTransaction(), list));
    }

    @Test
    void testFindTransaction_Success() {
        ReactivePanacheQuery<TransactionEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.firstResult()).thenReturn(Uni.createFrom().item(transactionEntity));
        Mockito.when(transactionRepository.find("{_id: ObjectId(?1)}", "transactionId")).thenReturn(query);

        Uni<TransactionEntity> result = transactionService.findTransaction("transactionId");

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(transactionEntity);
    }

    @Test
    void testDeleteTransaction_Success() {
        Mockito.when(transactionRepository.delete(any(TransactionEntity.class)))
                .thenReturn(Uni.createFrom().voidItem());

        Uni<Void> result = transactionService.deleteTransaction(transactionEntity);

        result.subscribe()
                .with(Assertions::assertNull);
    }

    @Test
    void testDeleteTransaction_Failure() {
        Mockito.when(transactionRepository.delete(any(TransactionEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Uni<Void> result = transactionService.deleteTransaction(transactionEntity);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(WebApplicationException.class);
    }

    @Test
    void testUpdateTransaction_Success() {
        Mockito.when(transactionRepository.update(any(TransactionEntity.class)))
                .thenReturn(Uni.createFrom().item(transactionEntity));

        Uni<TransactionEntity> result = transactionService.updateTransaction("transactionId", updateTransactionDto, transactionEntity);

        result.subscribe()
                .with(entity -> Assertions.assertEquals(transactionEntity, entity));
    }

    @Test
    void testUpdateTransaction_Failure() {
        Mockito.when(transactionRepository.update(any(TransactionEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Uni<TransactionEntity> result = transactionService.updateTransaction("transactionId", updateTransactionDto, transactionEntity);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(WebApplicationException.class);
    }

    @Test
    void testLatestByTerminalUuidAndStatus_Success() {
        ReactivePanacheQuery<TransactionEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.firstResult()).thenReturn(Uni.createFrom().item(transactionEntity));
        Sort sort = Sort.by("creationTimestamp", Sort.Direction.Descending);
        Mockito.when(transactionRepository.find("terminalUuid in ?1 and status = ?2", sort, List.of("16a79a4624356b00da07cfbf"), "status")).thenReturn(query);

        Uni<TransactionEntity> result = transactionService.findLatestByTerminalUuidAndStatus(List.of("16a79a4624356b00da07cfbf"), "status", sort);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(transactionEntity);
    }

    @Test
    void testCountByTerminals_Success() {
        Mockito.when(transactionRepository.count("terminalUuid in ?1", Arrays.asList("66a79a4624356b00da07cfbf", "16a79a4624356b00da07cfbf")))
                .thenReturn(Uni.createFrom().item(10L));

        var terminalCount = transactionService.getTransactionCountByTerminals(Arrays.asList("66a79a4624356b00da07cfbf", "16a79a4624356b00da07cfbf"));

        terminalCount
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(10L);
    }

    @Test
    void testGetTransactionListPagedByTerminals_Success() throws ParseException {
        List<String> terminalUuids = Arrays.asList("66a79a4624356b00da07cfbf", "c7a1b24b0583477292ebdbaa");
        ReactivePanacheQuery<TransactionEntity> query = Mockito.mock(ReactivePanacheQuery.class);

        Mockito.when(query.page(anyInt(), anyInt())).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(TestData.mockedListTransaction()));

        String queryStr = "{ 'creationTimestamp': { '$gte': ?2, '$lte': ?3 }, 'terminalUuid': { '$in': [?1] } }";
        Sort sort = Sort.by("creationTimestamp", Sort.Direction.Ascending);

        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-01");
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2024-12-31");

        Mockito.when(transactionRepository.find(queryStr, sort,terminalUuids, startDate, endDate))
                .thenReturn(query);

        Uni<List<TransactionEntity>> result = transactionService.getTransactionListPagedByTerminals(
                terminalUuids,
                startDate,
                endDate,
                sort,
                0,
                10);

        result.subscribe().with(list -> Assertions.assertEquals(TestData.mockedListTransaction(), list));
    }
}
