package it.pagopa.swclient.mil.papos.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.swclient.mil.papos.dao.SolutionEntity;
import it.pagopa.swclient.mil.papos.dao.SolutionRepository;
import it.pagopa.swclient.mil.papos.model.SolutionDto;
import it.pagopa.swclient.mil.papos.util.TestData;
import jakarta.ws.rs.InternalServerErrorException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SolutionServiceTest {

    @InjectMock
    static SolutionService solutionService;

    @InjectMock
    static SolutionRepository solutionRepository;

    static SolutionEntity solutionEntity;

    static SolutionDto solutionDto;

    @BeforeAll
    static void createTestObjects() {
        solutionEntity = TestData.getCorrectSolutionEntity();
        solutionDto = TestData.getCorrectSolutionDto();
        solutionService = new SolutionService(solutionRepository);
    }

    @Test
    void testCreateSolution_Success() {
        Mockito.when(solutionRepository.persist(any(SolutionEntity.class)))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Uni<SolutionEntity> result = solutionService.createSolution(solutionDto);

        result.subscribe()
                .with(entity -> Assertions.assertEquals(solutionEntity, entity));
    }

    @Test
    void testCreateSolution_Error() {
        Mockito.when(solutionRepository.persist(any(SolutionEntity.class)))
                .thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));

        Uni<SolutionEntity> result = solutionService.createSolution(solutionDto);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(InternalServerErrorException.class);
    }

    @Test
    void testSolution_Success() {
        Mockito.when(solutionRepository.findById(new ObjectId("66a79a4624356b00da07cfbf")))
                .thenReturn(Uni.createFrom().item(solutionEntity));

        Uni<SolutionEntity> terminalEntityUni = solutionService.findById("66a79a4624356b00da07cfbf");

        terminalEntityUni
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(solutionEntity);
    }
}
