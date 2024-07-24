package it.pagopa.swclient.mil.papos.util;

import it.pagopa.swclient.mil.papos.dao.BulkLoadStatusEntity;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.model.TransactionDto;
import it.pagopa.swclient.mil.papos.model.UpdateTransactionDto;
import it.pagopa.swclient.mil.papos.model.WorkstationsDto;

import java.util.ArrayList;
import java.util.List;

public final class TestData {

    public static TerminalDto getCorrectTerminalDto() {
        return new TerminalDto("AGID_01", "34523860", true,
                "06534340721", List.of("cassa-1-ufficio-3", "cassa-2-ufficio-3"));
    }

    public static TerminalEntity getCorrectTerminalEntity() {
        TerminalEntity terminalEntity = new TerminalEntity();
        terminalEntity.setTerminalUuid("74a7c24f-5c64-41c2-aeac-d1fae93bff49");
        terminalEntity.setTerminalId("34523860");
        terminalEntity.setEnabled(true);
        terminalEntity.setPayeeCode("06534340721");

        return terminalEntity;
    }

    public static BulkLoadStatusEntity getCorrectBulkLoadStatusEntity() {
        BulkLoadStatusEntity bulkLoadStatusEntity = new BulkLoadStatusEntity();
        bulkLoadStatusEntity.setBulkLoadingId("74a7c24f-5c64-41c2-aeac-d1fae93bff49");
        bulkLoadStatusEntity.setSuccessRecords(5);
        bulkLoadStatusEntity.setFailedRecords(0);
        bulkLoadStatusEntity.setTotalRecords(5);
        bulkLoadStatusEntity.setErrorMessages(new ArrayList<>());

        return bulkLoadStatusEntity;
    }

    public static WorkstationsDto getCorrectWorkstationDto() {
        return new WorkstationsDto(List.of("cassa-1-ufficio-3", "cassa-2-ufficio-3"));
    }

    public static TransactionDto getCorrectTransactionDto() {
        return new TransactionDto("AGID_01", "34523860", "123456789123456789",
                "06534340721");
    }

    public static UpdateTransactionDto getCorrectUpdateTransactionDto() {
        return new UpdateTransactionDto(40L, "CLOSED_OK");
    }

    public static TransactionEntity getCorrectTransactionEntity() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setTransactionId("74a7c24f-5c64-41c2-aeac-d1fae93bff49");
        transactionEntity.setTerminalId("34523860");
        transactionEntity.setPspId("AGID_01");
        transactionEntity.setPayeeCode("06534340721");
        transactionEntity.setNoticeNumber("123456789123456789");

        return transactionEntity;
    }

    public static List<TerminalEntity> mockedList() {
        TerminalEntity te1 = new TerminalEntity();
        te1.setTerminalUuid("uuid1");
        TerminalEntity te2 = new TerminalEntity();
        te2.setTerminalUuid("uuid2");

        return List.of(te1, te2);
    }

    public static List<TerminalDto> mockedListTerminalDto() {
        TerminalDto td1 = new TerminalDto("AGID_01", "34523860", true, "123456789123456789", null);
        TerminalDto td2 = new TerminalDto("AGID_02", "34523861", false, "123456789123456782", null);

        return List.of(td1, td2);
    }

}
