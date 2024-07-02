package it.pagopa.swclient.mil.papos.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.Terminal;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.dao.TerminalRepository;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.model.WorkstationsDto;
import it.pagopa.swclient.mil.papos.util.Utility;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class TerminalService {

    private final TerminalRepository terminalRepository;

    public TerminalService(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }

    /**
     * Create a new terminal starting from a terminalDto.
     *
     * @param terminalDto dto of terminal to be generated
     * @return terminal created
     */
    public Uni<TerminalEntity> createTerminal(TerminalDto terminalDto) {

        Log.debugf("TerminalService -> createTerminal - Input parameters: %s", terminalDto);

        String terminalUuid = Utility.generateTerminalUuid();
        TerminalEntity entity = createTerminalEntity(terminalDto, terminalUuid);

        return terminalRepository.persist(entity)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(terminalSaved -> terminalSaved);
    }

    /**
     * Returns a number corresponding to the total number of terminal found.
     *
     * @param attributeName  name of the attribute
     * @param attributeValue value of the attribute
     * @return a number
     */
    public Uni<Long> getTerminalCountByAttribute(String attributeName, String attributeValue) {
        return terminalRepository.count(attributeName, attributeValue);
    }

    /**
     * Returns a list of terminals paginated. The query filters on attributeName.
     *
     * @param attributeName  string representing the name of attribute to be filtered
     * @param attributeValue value of attribute
     * @param pageIndex      0-based page index
     * @param pageSize       page size
     * @return a list of terminals
     */
    public Uni<List<TerminalEntity>> getTerminalListPagedByAttribute(String attributeName, String attributeValue, int pageIndex, int pageSize) {
        return terminalRepository
                .find(attributeName, attributeValue)
                .page(pageIndex, pageSize)
                .list();
    }

    /**
     * Find first terminal equals to terminalUuid given in input.
     *
     * @param terminalUuid uuid of terminal
     * @return terminal founded
     */
    public Uni<TerminalEntity> findTerminal(String terminalUuid) {

        return terminalRepository
                .find("terminalUuid = ?1", terminalUuid)
                .firstResult();
    }

    /**
     * Update terminal adding workstations from a workstationDto.
     *
     * @param workstations dto of workstations to be added
     * @param oldTerminal old terminal to be modified
     * @return terminal updated
     */
    public Uni<TerminalEntity> updateWorkstations(WorkstationsDto workstations, TerminalEntity oldTerminal) {

        Terminal terminal = oldTerminal.getTerminal();
        List<String> existingWorkstations = terminal.getWorkstations() != null ? terminal.getWorkstations() : new ArrayList<>();

        Set<String> updatedWorkstationsSet = new HashSet<>(existingWorkstations);
        updatedWorkstationsSet.addAll(workstations.workstations());
        List<String> updatedWorkstations = new ArrayList<>(updatedWorkstationsSet);

        terminal.setWorkstations(updatedWorkstations);

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

        TerminalEntity entity = createTerminalEntity(terminalDto, terminalUuid);
        entity.id = oldTerminal.id;

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

        return terminalRepository.delete(terminal)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(terminalDeleted -> terminalDeleted);
    }

    private TerminalEntity createTerminalEntity(TerminalDto terminalDto, String terminalUuid) {
        Log.debugf("TerminalService -> createTerminalEntity: storing terminal [%s] on DB", terminalDto);

        Terminal terminal = new Terminal();
        terminal.setPspId(terminalDto.pspId());
        terminal.setTerminalId(terminalDto.terminalId());
        terminal.setEnabled(terminalDto.enabled());
        terminal.setPayeeCode(terminalDto.payeeCode());

        TerminalEntity terminalEntity = new TerminalEntity();
        terminalEntity.setTerminalUuid(terminalUuid);
        terminalEntity.setTerminal(terminal);

        return terminalEntity;
    }
}
