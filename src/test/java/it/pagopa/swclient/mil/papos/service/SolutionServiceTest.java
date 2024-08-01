package it.pagopa.swclient.mil.papos.service;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.swclient.mil.papos.dao.SolutionEntity;
import it.pagopa.swclient.mil.papos.dao.SolutionRepository;
import it.pagopa.swclient.mil.papos.model.SolutionDto;
import it.pagopa.swclient.mil.papos.util.TestData;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static it.pagopa.swclient.mil.papos.util.TestData.mockedSolutionEntityList;

import java.util.List;

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

        Uni<SolutionEntity> solutionEntityUni = solutionService.findById("66a79a4624356b00da07cfbf");

        solutionEntityUni
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(solutionEntity);
    }


    @Test
    void testGetSolutionsCount_Success() {
        Long countNumber = 10L;
        Mockito.when(solutionRepository.count())
                .thenReturn(Uni.createFrom().item(countNumber));

        Uni<Long> result = solutionService.getSolutionsCount();

        result.subscribe()
                .with(count -> Assertions.assertEquals(countNumber, count));
    }

    @Test
    void testFindSolutions_Success() {

        ReactivePanacheQuery<SolutionEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.page(anyInt(), anyInt())).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(mockedSolutionEntityList()));

        Mockito.when(solutionRepository.findAll())
                .thenReturn(query);

        Uni<List<SolutionEntity>> solutionsEntityUni = solutionService.findSolutions("requestid", 1, 10);

        solutionsEntityUni
                .subscribe()
                .with(list -> Assertions.assertEquals(mockedSolutionEntityList(), list));

    }

    @Test
    void testGetSolutionByAttributeCount_Success() {
        Mockito.when(solutionRepository.count("pspId", "pspId"))
                .thenReturn(Uni.createFrom().item(10L));

        var solutionCount = solutionService.getSolutionCountByAttribute("pspId", "pspId");

        solutionCount
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertItem(10L);
    }

    @Test
    void testGetSolutionsList_Success() {

        ReactivePanacheQuery<SolutionEntity> query = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(query.page(anyInt(), anyInt())).thenReturn(query);
        Mockito.when(query.list()).thenReturn(Uni.createFrom().item(mockedSolutionEntityList()));
        Mockito.when(solutionRepository.find(String.format("%s = ?1", "pspId"), "pspId")).thenReturn(query);

        Uni<List<SolutionEntity>> result = solutionService.getSolutionsListPagedByAttribute("pspId", "pspId", 0, 10);

        result.subscribe()
                .with(list -> Assertions.assertEquals(mockedSolutionEntityList(), list));
    }


    @Test
    void testDeleteSolution_Success() {
        Mockito.when(solutionRepository.delete(any(SolutionEntity.class)))
                .thenReturn(Uni.createFrom().voidItem());

        Uni<Void> result = solutionService.deleteSolution(solutionEntity);

        result.subscribe()
                .with(Assertions::assertNull);
    }

    @Test
    void testDeleteSolution_Failure() {
        Mockito.when(solutionRepository.delete(any(SolutionEntity.class)))
                .thenReturn(Uni.createFrom().failure(new WebApplicationException()));

        Uni<Void> result = solutionService.deleteSolution(solutionEntity);

        result.subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(WebApplicationException.class);
    }

}
