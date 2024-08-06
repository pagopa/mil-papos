package it.pagopa.swclient.mil.papos.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.*;
import it.pagopa.swclient.mil.papos.model.BulkLoadStatus;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.model.WorkstationsDto;
import it.pagopa.swclient.mil.papos.util.Utility;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TerminalService {

    private final TerminalRepository terminalRepository;

    private final BulkLoadStatusRepository bulkLoadStatusRepository;

    public TerminalService(TerminalRepository terminalRepository, BulkLoadStatusRepository bulkLoadStatusRepository) {
        this.terminalRepository = terminalRepository;
        this.bulkLoadStatusRepository = bulkLoadStatusRepository;
    }

    /**
     * Create a new terminal starting from a terminalDto.
     *
     * @param terminalDto dto of terminal to be generated
     * @return terminal created
     */
    public Uni<TerminalEntity> createTerminal(TerminalDto terminalDto) {
        Log.debugf("TerminalService -> createTerminal - Input parameters: %s", terminalDto);

        String terminalUuid = Utility.generateRandomUuid();
        TerminalEntity entity = createTerminalEntity(terminalDto, terminalUuid);

        return terminalRepository.persist(entity)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(terminalSaved -> terminalSaved);
    }

    /**
     * Allows the bulk loading of a set of terminals starting from a fileContent.
     *
     * @param terminalRequests file json containing a set of terminals
     * @return list of terminal created
     */
    public Uni<BulkLoadStatusEntity> processBulkLoad(List<TerminalDto> terminalRequests, int totalRecords, String pspId) {
        Log.debugf("TerminalService -> processBulkLoad - Input parameters: file content length: %d bytes", terminalRequests.size());

        String bulkLoadingId = Utility.generateRandomUuid();
        BulkLoadStatus bulkLoadStatus = new BulkLoadStatus(bulkLoadingId, totalRecords);
        bulkLoadStatus.setFailedRecords(totalRecords - terminalRequests.size());
        bulkLoadStatus.setPspId(pspId);
        List<Uni<Void>> terminalCreationUnis = new ArrayList<>();

        for (TerminalDto terminal : terminalRequests) {
            Uni<Void> terminalCreationUni = createTerminal(terminal)
                    .onFailure()
                    .transform(failure -> {
                        bulkLoadStatus.recordFailure(failure.getMessage());

                        return failure;
                    })
                    .onItem()
                    .transform(success -> {
                        bulkLoadStatus.recordSuccess();

                        return null;
                    });

            terminalCreationUnis.add(terminalCreationUni);
        }

        Uni<Void> allTerminalCreations = Uni.combine().all().unis(terminalCreationUnis).discardItems();

        return allTerminalCreations
                .onItem()
                .transformToUni(ignored -> bulkLoadStatusRepository.persist(createBulkLoadStatusEntity(bulkLoadStatus)))
                .onFailure()
                .transform(error -> {
                    Log.error("TerminalService -> processBulkLoad: error persisting bulkLoadStatus", error);

                    return error;
                })
                .onItem()
                .transform(terminalSaved -> terminalSaved);

    }

    /**
     * Find first bulkLoad status equals to terminalUuid given in input.
     *
     * @param bulkLoadingId id of bulkLoadStatus
     * @return bulkLoadStatus found
     */
    public Uni<BulkLoadStatusEntity> findBulkLoadStatus(String bulkLoadingId) {
        Log.debugf("TerminalService -> findBulkLoadStatus - Input parameters: %s", bulkLoadingId);

        return bulkLoadStatusRepository
                .find("bulkLoadingId = ?1", bulkLoadingId)
                .firstResult();
    }

    /**
     * Returns a number corresponding to the total number of terminal found.
     *
     * @param workstation name of workstation
     * @return a number
     */
    public Uni<Long> getTerminalCountByWorkstation(String workstation, List<String> solutionIds) {
        Log.debugf("TerminalService -> getTerminalCountByWorkstation - Input parameter: %s", workstation);

        return terminalRepository.count("workstations = ?1 and solutionId in ?2", workstation, solutionIds);
    }

    /**
     * Returns a list of terminals paginated. The query filters on workstation.
     *
     * @param workstation name of workstation
     * @param pageIndex   0-based page index
     * @param pageSize    page size
     * @return a list of terminals
     */
    public Uni<List<TerminalEntity>> getTerminalListPagedByWorkstation(String workstation, int pageIndex, int pageSize, List<String> solutionIds) {
        Log.debugf("TerminalService -> getTerminalListPagedByWorkstation - Input parameters: %s, %s, %s, %s", workstation, pageIndex, pageSize, solutionIds);

        return terminalRepository
                .find("workstations = ?1 and solutionId in ?2", workstation, solutionIds)
                .page(pageIndex, pageSize)
                .list();
    }

    /**
     * Find first terminal equals to terminalUuid given in input.
     *
     * @param terminalUuid uuid of terminal
     * @return terminal found
     */
    public Uni<TerminalEntity> findTerminal(String terminalUuid) {
        Log.debugf("TerminalService -> findTerminal - Input parameters: %s", terminalUuid);

        return terminalRepository
                .find("terminalUuid = ?1", terminalUuid)
                .firstResult();
    }

    /**
     * Update terminal adding workstations from a workstationDto.
     *
     * @param workstations dto of workstations to be added
     * @param oldTerminal  old terminal to be modified
     * @return terminal updated
     */
    public Uni<TerminalEntity> updateWorkstations(WorkstationsDto workstations, TerminalEntity oldTerminal) {
        Log.debugf("TerminalService -> updateWorkstations - Input parameters: %s, %s", workstations, oldTerminal);

        oldTerminal.setWorkstations(workstations.workstations());

        return terminalRepository.update(oldTerminal)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(terminalUpdated -> terminalUpdated);
    }

    /**
     * Update terminal starting from a terminalDto.
     *
     * @param terminalDto  dto of modified terminal
     * @param terminalUuid terminalUuid of old terminal to be modified
     * @return terminal updated
     */
    public Uni<TerminalEntity> updateTerminal(String terminalUuid, TerminalDto terminalDto, TerminalEntity oldTerminal) {
        Log.debugf("TerminalService -> updateTerminal - Input parameters: %s, %s, %s", terminalUuid, terminalDto, oldTerminal);

        TerminalEntity entity = createTerminalEntity(terminalDto, terminalUuid);
        entity.setTerminalUuid(oldTerminal.getTerminalUuid());
        entity.setWorkstations(oldTerminal.getWorkstations());

        return terminalRepository.update(entity)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(terminalUpdated -> terminalUpdated);
    }

    /**
     * Delete terminal starting from a terminalEntity.
     *
     * @param terminal terminal to be deleted
     * @return void
     */
    public Uni<Void> deleteTerminal(TerminalEntity terminal) {
        Log.debugf("TerminalService -> deleteTerminal - Input parameters: %s", terminal);

        return terminalRepository.delete(terminal)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(terminalDeleted -> terminalDeleted);
    }

    /**
     * Returns a number corresponding to the total number of terminal found.
     *
     * @param solutionIds list of Solution
     * @return a number
     */
    public Uni<Long> countBySolutionIds(List<String> solutionIds) {
        Log.debugf("TerminalService -> countBySolutionIds - Input parameter: %s", solutionIds);

        return terminalRepository.count("solutionId in (?1)", solutionIds);
    }

    /**
     * Returns all terminal corresponding to the list of solution ids.
     *
     * @param solutionIds list of Solution
     * @return a list of terminals
     */
    public Uni<List<TerminalEntity>> findAllBySolutionIds(List<String> solutionIds) {
        Log.debugf("TerminalService -> findAllBySolutionIds - Input parameter: %s", solutionIds);

        return terminalRepository.find("solutionId in ?1", solutionIds).list();
    }

    /**
     * Returns all terminals by solutionIds and terminalId given in input.
     *
     * @param solutionIds list of solutions
     * @param terminalId ID of the terminal for the PSP
     * @return list of terminals found
     */
    public Uni<List<TerminalEntity>> findAllBySolutionIdAndTerminalId(List<String> solutionIds, String terminalId) {
        Log.debugf("SolutionService -> findAllBySolutionIdAndTerminalId - Input parameters: %s, %s", solutionIds, terminalId);

        return terminalRepository.find("solutionId in ?1 and terminalId = ?2", solutionIds, terminalId).list();
    }

    /**
     * Find all terminal equals to solutionIds given in input.
     *
     * @param solutionIds list of Solution
     * @return Solutions found
     */
    public Uni<List<TerminalEntity>> findBySolutionIds(List<String> solutionIds, int pageIndex, int pageSize) {
        Log.debugf("TerminalService -> findBySolutionIds - Input parameter: %s, %s, %s", solutionIds, pageIndex, pageSize);

        return terminalRepository.find("solutionId in ?1", solutionIds)
                .page(pageIndex, pageSize)
                .list();
    }

    private TerminalEntity createTerminalEntity(TerminalDto terminalDto, String terminalUuid) {
        Log.debugf("TerminalService -> createTerminalEntity: storing terminal [%s] on DB", terminalDto);

        TerminalEntity terminalEntity = new TerminalEntity();
        terminalEntity.setTerminalUuid(terminalUuid);
        terminalEntity.setTerminalId(terminalDto.terminalId());
        terminalEntity.setEnabled(terminalDto.enabled());
        terminalEntity.setSolutionId(terminalDto.solutionId());
        terminalEntity.setWorkstations(terminalDto.workstations());

        return terminalEntity;
    }

    private BulkLoadStatusEntity createBulkLoadStatusEntity(BulkLoadStatus bulkLoadStatus) {
        Log.debugf("TerminalService -> createBulkLoadStatusEntity: storing bulkLoadStatus [%s] on DB", bulkLoadStatus);

        BulkLoadStatusEntity bulkLoadStatusEntity = new BulkLoadStatusEntity();
        bulkLoadStatusEntity.setBulkLoadingId(bulkLoadStatus.getBulkLoadingId());
        bulkLoadStatusEntity.setTotalRecords(bulkLoadStatus.getTotalRecords());
        bulkLoadStatusEntity.setSuccessRecords(bulkLoadStatus.getSuccessRecords());
        bulkLoadStatusEntity.setFailedRecords(bulkLoadStatus.getFailedRecords());
        bulkLoadStatusEntity.setErrorMessages(bulkLoadStatus.getErrorMessages());
        bulkLoadStatusEntity.setPspId(bulkLoadStatus.getPspId());

        return bulkLoadStatusEntity;
    }
}
