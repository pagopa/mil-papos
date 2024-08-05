package it.pagopa.swclient.mil.papos.util;

import it.pagopa.swclient.mil.papos.dao.BulkLoadStatusEntity;
import it.pagopa.swclient.mil.papos.dao.SolutionEntity;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.model.*;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public final class TestData {

    public static TerminalDto getCorrectTerminalDto() {
        return new TerminalDto("66a79a4624356b00da07cfbf", "34523860", true, List.of("cassa-1-ufficio-3", "cassa-2-ufficio-3"));
    }

    public static TerminalEntity getCorrectTerminalEntity() {
        TerminalEntity terminalEntity = new TerminalEntity();
        terminalEntity.setSolutionId("66a79a4624356b00da07cfbf");
        terminalEntity.setTerminalUuid("74a7c24f-5c64-41c2-aeac-d1fae93bff49");
        terminalEntity.setTerminalId("34523860");
        terminalEntity.setEnabled(true);

        return terminalEntity;
    }
    

    public static BulkLoadStatusEntity getCorrectBulkLoadStatusEntity() {
        BulkLoadStatusEntity bulkLoadStatusEntity = new BulkLoadStatusEntity();
        bulkLoadStatusEntity.setBulkLoadingId("74a7c24f-5c64-41c2-aeac-d1fae93bff49");
        bulkLoadStatusEntity.setPspId("AGID_01");
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
        return new TransactionDto("22dd2aad-6dcd-41c9-91c4-2552f11d098f", "485564829563528563",
                "06534340721");
    }

    public static UpdateTransactionDto getCorrectUpdateTransactionDto() {
        return new UpdateTransactionDto(40L, "CLOSED_OK");
    }

    public static TransactionEntity getCorrectTransactionEntity() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setPayeeCode("06534340721");
        transactionEntity.setNoticeNumber("123456789123456789");

        return transactionEntity;
    }

    public static SolutionEntity getCorrectSolutionEntity() {
        SolutionEntity solutionEntity = new SolutionEntity();
        solutionEntity.id = new ObjectId("66a79a4624356b00da07cfbf");
        solutionEntity.setPspId("TMIL0101");
        solutionEntity.setLocationCode("12704343560");

        return solutionEntity;
    }

    public static SolutionDto getCorrectSolutionDto() {
        return new SolutionDto("TMIL0101", "12704343560");
    }

    public static List<TerminalEntity> mockedList() {
        TerminalEntity te1 = new TerminalEntity();
        te1.setTerminalUuid("c7a1b24b0583477292ebdbaa");
        te1.setSolutionId("66a79a4624356b00da07cfbf");
        TerminalEntity te2 = new TerminalEntity();
        te2.setTerminalUuid("uuid2");

        return List.of(te1, te2);
    }

    public static List<SolutionEntity> mockedListSolution() {
        SolutionEntity se1 = new SolutionEntity();
        se1.id = new ObjectId("66a79a4624356b00da07cfbf");
        se1.setPspId("TMIL0101");
        se1.setLocationCode("06534340721");
        SolutionEntity se2 = new SolutionEntity();
        se2.id = new ObjectId("66a79a4624346b20da01cfbf");
        se2.setPspId("TMIL0101");
        se2.setLocationCode("06534340721");

        return List.of(se1, se2);
    }

    public static List<TransactionEntity> mockedListTransaction() {
        TransactionEntity te1 = new TransactionEntity();
        te1.setTerminalUuid("c7a1b24b0583477292ebdbaa");
        TransactionEntity te2 = new TransactionEntity();
        te2.setTerminalUuid("c7a1b24b0583477292ebdbaa");

        return List.of(te1, te2);
    }

    public static List<TerminalDto> mockedListTerminalDto() {
        TerminalDto td1 = new TerminalDto("66a79a4624356b00da07cfbf", "34523860", true, null);
        TerminalDto td2 = new TerminalDto("66a79a4624356b00da07cfbf", "34523861", false, null);

        return List.of(td1, td2);
    }

}
