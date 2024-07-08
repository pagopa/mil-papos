package it.pagopa.swclient.mil.papos.util;

import it.pagopa.swclient.mil.papos.dao.BulkLoadStatusEntity;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.model.WorkstationsDto;

import java.util.ArrayList;
import java.util.List;

public final class TerminalTestData {

    public static TerminalDto getCorrectTerminalDto() {
        return new TerminalDto("AGID_01", "34523860", true,
                "RSSMRA85T10A562S", List.of("cassa-1-ufficio-3", "cassa-2-ufficio-3"));
    }

    public static TerminalEntity getCorrectTerminalEntity() {
        TerminalEntity terminalEntity = new TerminalEntity();
        terminalEntity.setTerminalUuid("c7a1b24b0583477292ebdbaa");
        terminalEntity.setTerminalId("34523860");
        terminalEntity.setEnabled(true);
        terminalEntity.setPayeeCode("RSSMRA85T10A562S");

        return terminalEntity;
    }

    public static BulkLoadStatusEntity getCorrectBulkLoadStatusEntity() {
        BulkLoadStatusEntity bulkLoadStatusEntity = new BulkLoadStatusEntity();
        bulkLoadStatusEntity.setBulkLoadingId("c7a1b24b0583477292ebdbaa");
        bulkLoadStatusEntity.setSuccessRecords(5);
        bulkLoadStatusEntity.setFailedRecords(0);
        bulkLoadStatusEntity.setTotalRecords(5);
        bulkLoadStatusEntity.setErrorMessages(new ArrayList<>());

        return bulkLoadStatusEntity;
    }

    public static WorkstationsDto getCorrectWorkstationDto() {
        return new WorkstationsDto(List.of("cassa-1-ufficio-3", "cassa-2-ufficio-3"));
    }

}
