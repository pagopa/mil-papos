package it.pagopa.swclient.mil.papos.service;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.dao.TerminalRepository;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.util.TerminalTestData;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TerminalServiceTest {

    @InjectMock
    static TerminalRepository terminalRepository;

    static TerminalDto terminalDto;

    static TerminalEntity terminalEntity;

    static TerminalService terminalService;

    @BeforeAll
    static void createTestObjects() {
        terminalDto = TerminalTestData.getCorrectTerminalDto();
        terminalEntity = TerminalTestData.getCorrectTerminalEntity();
        terminalService = new TerminalService(terminalRepository);
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
}
