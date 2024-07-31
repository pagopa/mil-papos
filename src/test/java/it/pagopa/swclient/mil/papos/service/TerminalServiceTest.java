package it.pagopa.swclient.mil.papos.service;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.swclient.mil.papos.dao.*;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.model.WorkstationsDto;
import it.pagopa.swclient.mil.papos.util.TestData;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static it.pagopa.swclient.mil.papos.util.TestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TerminalServiceTest {

    @InjectMock
    static TerminalRepository terminalRepository;

    @InjectMock
    static BulkLoadStatusRepository bulkLoadStatusRepository;

    static TerminalDto terminalDto;

    static WorkstationsDto workstationsDto;

    static TerminalEntity terminalEntity;

    static BulkLoadStatusEntity bulkLoadStatusEntity;

    static TerminalService terminalService;

    @BeforeAll
    static void createTestObjects() {
        terminalDto = TestData.getCorrectTerminalDto();
        workstationsDto = TestData.getCorrectWorkstationDto();
        terminalEntity = TestData.getCorrectTerminalEntity();
        bulkLoadStatusEntity = TestData.getCorrectBulkLoadStatusEntity();
        terminalService = new TerminalService(terminalRepository, bulkLoadStatusRepository);
    }

    @Test
    void testCreateTerminal_Success() {
        Mockito.when(terminalRepository.persist(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Uni<TerminalEntity> result = terminalService.createTerminal(terminalDto);

        result.subscribe()
                .with(entity -> Assertions.assertEquals(terminalEntity, entity));
    }

    @Test
    void testCreateTerminal_Failure() {
        Mockito.when(terminalRepository.persist(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));

        Uni<TerminalEntity> result = terminalService.createTerminal(terminalDto);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(InternalServerErrorException.class);
    }

    @Test
    void testProcessBulkLoad_Success() {
        Mockito.when(terminalRepository.persist(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().item(new TerminalEntity()));
        Mockito.when(bulkLoadStatusRepository.persist(any(BulkLoadStatusEntity.class)))
                .thenReturn(Uni.createFrom().item(new BulkLoadStatusEntity()));

        Uni<BulkLoadStatusEntity> result = terminalService.processBulkLoad(mockedListTerminalDto());

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();
    }

    @Test
    void testProcessBulkLoad_CreateTerminalFailure() {
        Mockito.when(terminalRepository.persist(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException()));
        Mockito.when(bulkLoadStatusRepository.persist(any(BulkLoadStatusEntity.class)))
                .thenReturn(Uni.createFrom().item(new BulkLoadStatusEntity()));

        Uni<BulkLoadStatusEntity> result = terminalService.processBulkLoad(mockedListTerminalDto());

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(RuntimeException.class);
    }

    @Test
    void testFindBulkLoadStatus_Success() {
        ReactivePanacheQuery<BulkLoadStatusEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.firstResult()).thenReturn(Uni.createFrom().item(bulkLoadStatusEntity));
        Mockito.when(bulkLoadStatusRepository.find("bulkLoadingId = ?1", "bulkLoadingId")).thenReturn(query);

        Uni<BulkLoadStatusEntity> terminalEntityUni = terminalService.findBulkLoadStatus("bulkLoadingId");

        terminalEntityUni
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(bulkLoadStatusEntity);
    }

    @Test
    void testProcessBulkLoad_PersistenceError() {
        Mockito.when(terminalRepository.persist(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().item(new TerminalEntity()));
        Mockito.when(bulkLoadStatusRepository.persist(any(BulkLoadStatusEntity.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Error persisting bulk load status")));

        Uni<BulkLoadStatusEntity> result = terminalService.processBulkLoad(mockedListTerminalDto());

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(RuntimeException.class, "Error persisting bulk load status");
    }

    @Test
    void testGetTerminalCountWorkstation_Success() {
        Mockito.when(terminalRepository.count("{ 'workstations': ?1 }", "workstation"))
                .thenReturn(Uni.createFrom().item(10L));

        var terminalCount = terminalService.getTerminalCountByWorkstation("workstation");

        terminalCount
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(10L);
    }

    @Test
    void testGetTerminalListWorkstation_Success() {
        ReactivePanacheQuery<TerminalEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.page(anyInt(), anyInt())).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(mockedList()));
        Mockito.when(terminalRepository.find("{ 'workstations': ?1 }", "workstation")).thenReturn(query);

        Uni<List<TerminalEntity>> result = terminalService.getTerminalListPagedByWorkstation("workstation", 0, 10);

        result.subscribe()
                .with(list -> Assertions.assertEquals(mockedList(), list));
    }

    @Test
    void testFindTerminal_Success() {
        ReactivePanacheQuery<TerminalEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.firstResult()).thenReturn(Uni.createFrom().item(terminalEntity));
        Mockito.when(terminalRepository.find("terminalUuid = ?1", "terminalUuid")).thenReturn(query);

        Uni<TerminalEntity> terminalEntityUni = terminalService.findTerminal("terminalUuid");

        terminalEntityUni
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(terminalEntity);
    }

    @Test
    void testUpdateWorkstations_Success() {
        Mockito.when(terminalRepository.update(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Uni<TerminalEntity> result = terminalService.updateWorkstations(workstationsDto, terminalEntity);

        result.subscribe()
                .with(entity -> Assertions.assertEquals(terminalEntity, entity));
    }

    @Test
    void testUpdateWorkstations_Failure() {
        Mockito.when(terminalRepository.update(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Uni<TerminalEntity> result = terminalService.updateWorkstations(workstationsDto, terminalEntity);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(WebApplicationException.class);
    }

    @Test
    void testUpdateTerminal_Success() {
        Mockito.when(terminalRepository.update(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().item(terminalEntity));

        Uni<TerminalEntity> result = terminalService.updateTerminal("terminalUuid", terminalDto, terminalEntity);

        result.subscribe()
                .with(entity -> Assertions.assertEquals(terminalEntity, entity));
    }

    @Test
    void testUpdateTerminal_Failure() {
        Mockito.when(terminalRepository.update(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Uni<TerminalEntity> result = terminalService.updateTerminal("terminalUuid", terminalDto, terminalEntity);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(WebApplicationException.class);
    }

    @Test
    void testDeleteTerminal_Success() {
        Mockito.when(terminalRepository.delete(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().voidItem());

        Uni<Void> result = terminalService.deleteTerminal(terminalEntity);

        result.subscribe()
                .with(Assertions::assertNull);
    }

    @Test
    void testDeleteTerminal_Failure() {
        Mockito.when(terminalRepository.delete(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Uni<Void> result = terminalService.deleteTerminal(terminalEntity);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(WebApplicationException.class);
    }

    @Test
    void testCountBySolutionIds_Success() {
        Mockito.when(terminalRepository.count("solutionId in (?1)", Arrays.asList("66a79a4624356b00da07cfbf", "16a79a4624356b00da07cfbf")))
                .thenReturn(Uni.createFrom().item(10L));

        var terminalCount = terminalService.countBySolutionIds(Arrays.asList("66a79a4624356b00da07cfbf", "16a79a4624356b00da07cfbf"));

        terminalCount
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(10L);
    }

    @Test
    void testFindBySolutionIds_Success() {
        ReactivePanacheQuery<TerminalEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.page(anyInt(), anyInt())).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(mockedList()));
        Mockito.when(terminalRepository.find("solutionId in ?1", Arrays.asList("66a79a4624356b00da07cfbf", "16a79a4624356b00da07cfbf"))).thenReturn(query);

        Uni<List<TerminalEntity>> result = terminalService.findBySolutionIds(Arrays.asList("66a79a4624356b00da07cfbf", "16a79a4624356b00da07cfbf"), 0, 10);

        result.subscribe()
                .with(list -> Assertions.assertEquals(mockedList(), list));
    }
}
