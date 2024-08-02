package it.pagopa.swclient.mil.papos.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.SolutionEntity;
import it.pagopa.swclient.mil.papos.dao.SolutionRepository;
import it.pagopa.swclient.mil.papos.model.SolutionDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class SolutionService {

    private final SolutionRepository solutionRepository;

    public SolutionService(SolutionRepository solutionRepository) {
        this.solutionRepository = solutionRepository;
    }

    /**
     * Create a new solution starting from a solutionDto.
     *
     * @param solutionDto dto of solution to be generated
     * @return solution created
     */
    public Uni<SolutionEntity> createSolution(SolutionDto solutionDto) {
        Log.debugf("SolutionService -> createSolution - Input parameters: %s", solutionDto);

        SolutionEntity entity = new SolutionEntity();
        entity.setLocationCode(solutionDto.locationCode());
        entity.setPspId(solutionDto.pspId());

        return solutionRepository.persist(entity)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(solutionSaved -> solutionSaved);
    }

    /**
     * Find all the solutions.
     *
     * @param pageNumber   0-based page index
     * @param pageSize    page size
     * @return Solutions found
     */
    public Uni<List<SolutionEntity>> findSolutions(int pageNumber, int pageSize) {
        Log.debugf("SolutionService -> findSolutions - Input pageNumber: %s, size: %s", pageNumber, pageSize);

        return solutionRepository.findAll()
                .page(pageNumber, pageSize)
                .list();
    }

    /**
     * Find first solution equals to solutionId given in input.
     *
     * @param solutionId id of Solution
     * @return Solution found
     */
    public Uni<SolutionEntity> findById(String solutionId) {
        Log.debugf("SolutionService -> findById - Input parameters: %s", solutionId);

        return solutionRepository.findById(new ObjectId(solutionId));
    }

    /**
     * Delete solution starting from a solutionEntity.
     *
     * @param solution solution to be deleted
     * @return void
     */
    public Uni<Void> deleteSolution(SolutionEntity solution) {
        Log.debugf("SolutionService -> deleteSolution - Input parameters: %s", solution);

        return solutionRepository.delete(solution)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(solutionDeleted -> solutionDeleted);
    }

    /**
     * Returns a number corresponding to the total number of solutions found.
     *
     * @return a number
     */
    public Uni<Long> getSolutionsCount() {
        Log.debugf("SolutionService -> getSolutionsCount");

        return solutionRepository.count();
    }

    /**
     * Returns a number corresponding to the total number of solutions found.
     *
     * @param attributeName  name of the attribute
     * @param attributeValue value of the attribute
     * @return a number
     */
    public Uni<Long> getSolutionCountByAttribute(String attributeName, String attributeValue) {
        Log.debugf("SolutionService -> getSolutionCountByAttribute - Input parameters: %s, %s", attributeName, attributeValue);

        return solutionRepository.count(attributeName, attributeValue);
    }

    /**
     * Returns a list of solutions paginated. The query filters on attributeName.
     *
     * @param attributeName  string representing the name of attribute to be filtered
     * @param attributeValue value of attribute
     * @param pageIndex      0-based page index
     * @param pageSize       page size
     * @return a list of solutions
     */
    public Uni<List<SolutionEntity>> getSolutionsListPagedByAttribute(String attributeName, String attributeValue, int pageIndex, int pageSize) {
        Log.debugf("SolutionService -> getSolutionListPagedByAttribute - Input parameters: %s, %s, %s, %s", attributeName, attributeValue, pageIndex, pageSize);

        return solutionRepository
                .find(String.format("%s = ?1", attributeName), attributeValue)
                .page(pageIndex, pageSize)
                .list();
    }

    /**
     * Returns a list of solutions. The query filters on locationCode.
     *
     * @param locationCode of the solution to be filtered
     * @return a list of solutions
     */
    public Uni<List<SolutionEntity>> getSolutionsListByLocationCode(String locationCode) {
        Log.debugf("SolutionService -> getSolutionsListByLocationCode - Input parameters: %s", locationCode);

        return solutionRepository
                .find("locationCode = ?1", locationCode)
                .list();
    }

    /**
     * Find all solution equals to attributeValue given in input.
     *
     * @param attributeName  string representing the name of attribute to be filtered
     * @param attributeValue value of attribute
     * @return list of Solution found
     */
    public Uni<List<SolutionEntity>> findAllByLocationOrPsp(String attributeName, String attributeValue) {
        Log.debugf("SolutionService -> findAllByLocationOrPsp - Input parameters: [%s, %s]", attributeName, attributeValue);

        return solutionRepository.list(String.format("%s = ?1", attributeName), attributeValue);
    }

    /**
     * Find all solution equals to pspId and solutionId given in input.
     *
     * @param pspId       ID of the POS service provider
     * @param solutionIds id of the solution
     * @return list of Solution found
     */
    public Uni<List<SolutionEntity>> findAllByPspAndSolutionId(String pspId, List<String> solutionIds) {
        Log.debugf("SolutionService -> findAllByPspAndSolutionId - Input parameters: [%s, %s]", pspId, solutionIds);
        List<ObjectId> solutionObjectIds = solutionIds.stream()
                .map(ObjectId::new)
                .toList();

        return solutionRepository.list("pspId = ?1 and _id in ?2", pspId, solutionObjectIds);
    }
}
