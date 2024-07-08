package it.pagopa.swclient.mil.papos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.swclient.mil.papos.dao.BulkLoadStatusEntity;
import it.pagopa.swclient.mil.papos.dao.BulkLoadStatusRepository;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.dao.TerminalRepository;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.model.WorkstationsDto;
import it.pagopa.swclient.mil.papos.util.TerminalTestData;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TerminalServiceTest {

    @InjectMock
    static TerminalRepository terminalRepository;

    @InjectMock
    static BulkLoadStatusRepository bulkLoadStatusRepository;

    static ObjectMapper objectMapper;

    static TerminalDto terminalDto;

    static WorkstationsDto workstationsDto;

    static TerminalEntity terminalEntity;

    static BulkLoadStatusEntity bulkLoadStatusEntity;

    static TerminalService terminalService;

    @BeforeAll
    static void createTestObjects() {
        terminalDto = TerminalTestData.getCorrectTerminalDto();
        workstationsDto = TerminalTestData.getCorrectWorkstationDto();
        terminalEntity = TerminalTestData.getCorrectTerminalEntity();
        bulkLoadStatusEntity = TerminalTestData.getCorrectBulkLoadStatusEntity();
        objectMapper = new ObjectMapper();
        terminalService = new TerminalService(terminalRepository, bulkLoadStatusRepository, objectMapper);
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
    void testProcessBulkLoad_Success() throws IOException {
        List<TerminalDto> terminalDtos = List.of(terminalDto, terminalDto);
        byte[] fileContent = objectMapper.writeValueAsBytes(terminalDtos);

        Mockito.when(terminalRepository.persist(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().item(new TerminalEntity()));
        Mockito.when(bulkLoadStatusRepository.persist(any(BulkLoadStatusEntity.class)))
                .thenReturn(Uni.createFrom().item(new BulkLoadStatusEntity()));

        Uni<BulkLoadStatusEntity> result = terminalService.processBulkLoad(fileContent);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();
    }

    @Test
    void testProcessBulkLoad_CreateTerminalFailure() throws IOException {
        List<TerminalDto> terminalDtos = List.of(terminalDto, terminalDto);
        byte[] fileContent = objectMapper.writeValueAsBytes(terminalDtos);

        Mockito.when(terminalRepository.persist(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException()));
        Mockito.when(bulkLoadStatusRepository.persist(any(BulkLoadStatusEntity.class)))
                .thenReturn(Uni.createFrom().item(new BulkLoadStatusEntity()));

        Uni<BulkLoadStatusEntity> result = terminalService.processBulkLoad(fileContent);

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
    void testProcessBulkLoad_FileReadError() {
        byte[] fileContent = "malformed content".getBytes();

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> terminalService.processBulkLoad(fileContent).await().indefinitely());

        Assertions.assertTrue(exception.getMessage().contains("Error processing file"));
    }

    @Test
    void testProcessBulkLoad_PersistenceError() throws IOException {
        List<TerminalDto> terminalDtos = List.of(terminalDto, terminalDto);
        byte[] fileContent = objectMapper.writeValueAsBytes(terminalDtos);

        Mockito.when(terminalRepository.persist(any(TerminalEntity.class)))
                .thenReturn(Uni.createFrom().item(new TerminalEntity()));
        Mockito.when(bulkLoadStatusRepository.persist(any(BulkLoadStatusEntity.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Error persisting bulk load status")));

        Uni<BulkLoadStatusEntity> result = terminalService.processBulkLoad(fileContent);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(RuntimeException.class, "Error persisting bulk load status");
    }



    @Test
    void testGetTerminalCount_Success() {
        Mockito.when(terminalRepository.count("pspId", "pspId"))
                .thenReturn(Uni.createFrom().item(10L));

        var terminalCount = terminalService.getTerminalCountByAttribute("pspId", "pspId");

        terminalCount
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(10L);
    }

    @Test
    void testGetTerminalCountWorkstation_Success() {
        Mockito.when(terminalRepository.count("{ 'workstations': ?1 }", "workstation"))
                .thenReturn(Uni.createFrom().item(10L));

        var terminalCount = terminalService.getTerminalCountByAttribute("workstation", "workstation");

        terminalCount
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(10L);
    }

    @Test
    void testGetTerminalList_Success() {
        ReactivePanacheQuery<TerminalEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.page(anyInt(), anyInt())).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(mockedList()));
        Mockito.when(terminalRepository.find(String.format("%s = ?1", "pspId"), "pspId")).thenReturn(query);

        Uni<List<TerminalEntity>> result = terminalService.getTerminalListPagedByAttribute("pspId", "pspId", 0, 10);

        result.subscribe()
                .with(list -> Assertions.assertEquals(mockedList(), list));
    }

    @Test
    void testGetTerminalListWorkstation_Success() {
        ReactivePanacheQuery<TerminalEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.page(anyInt(), anyInt())).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(mockedList()));
        Mockito.when(terminalRepository.find("{ 'workstations': ?1 }", "workstation")).thenReturn(query);

        Uni<List<TerminalEntity>> result = terminalService.getTerminalListPagedByAttribute("workstation", "workstation", 0, 10);

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

    private List<TerminalEntity> mockedList() {
        TerminalEntity te1 = new TerminalEntity();
        te1.setTerminalUuid("uuid1");
        TerminalEntity te2 = new TerminalEntity();
        te2.setTerminalUuid("uuid2");

        return List.of(te1, te2);
    }
}
