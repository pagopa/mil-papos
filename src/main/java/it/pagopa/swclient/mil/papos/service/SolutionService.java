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
                .transform(terminalSaved -> terminalSaved);
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
}
