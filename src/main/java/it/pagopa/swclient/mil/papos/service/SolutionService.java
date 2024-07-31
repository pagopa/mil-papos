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
     * @param requestId
     * @return Solutions found
     */
    public Uni<List<SolutionEntity>> findSolutions(String requestId, int pageNumber, int pageSize) {
        Log.debugf("SolutionService -> findSolutions - Input requestId: %s, pageNumber: %s, size: %s", requestId, pageNumber, pageSize);
        return solutionRepository.findAll().page(pageNumber,pageSize).list();
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

        if (attributeName.equals("workstation")) {
            return solutionRepository.count("{ 'workstations': ?1 }", attributeValue);
        }
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

        if (attributeName.equals("workstation")) {
            return solutionRepository
                    .find("{ 'workstations': ?1 }", attributeValue)
                    .page(pageIndex, pageSize)
                    .list();
        }
        return solutionRepository
                .find(String.format("%s = ?1", attributeName), attributeValue)
                .page(pageIndex, pageSize)
                .list();
    }

}
