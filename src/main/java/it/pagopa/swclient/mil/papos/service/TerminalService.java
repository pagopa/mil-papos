package it.pagopa.swclient.mil.papos.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.Terminal;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.dao.TerminalRepository;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.util.Utility;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TerminalService {

    private final TerminalRepository terminalRepository;

    public TerminalService(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }

    /**
     * Create a new terminal starting from a terminalDto.
     *
     * @param terminalDto       dto of terminal to be generated
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

    private TerminalEntity createTerminalEntity(TerminalDto terminalDto, String terminalUuid) {
        Log.debugf("TerminalService -> createTerminalEntity: storing terminal [%s] on DB", terminalDto);

        Terminal terminal = new Terminal();
        terminal.setPspId(terminalDto.pspId());
        terminal.setTerminalId(terminalDto.terminalId());
        terminal.setEnabled(terminalDto.enabled());
        terminal.setPayeeCode(terminalDto.payeeCode());
//        terminal.setSlave(terminalDto.slave());
//        terminal.setPagoPaConf(terminalDto.pagoPaConf());
//        terminal.setIdpay(terminalDto.idpay());
//        terminal.setWorkstations(terminalDto.workstations());

        TerminalEntity terminalEntity = new TerminalEntity();
        terminalEntity.setTerminalUuid(terminalUuid);
        terminalEntity.setTerminal(terminal);
//        terminalEntity.setTerminalHandler(terminalDto.terminalHandlerId());
//        terminalEntity.setServiceProviderId(serviceProviderId);

        return terminalEntity;
    }
}
