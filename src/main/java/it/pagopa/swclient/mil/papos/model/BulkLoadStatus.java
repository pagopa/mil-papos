package it.pagopa.swclient.mil.papos.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class BulkLoadStatus {
    private String bulkLoadingId;
    private int totalRecords;
    private int successRecords;
    private int failedRecords;
    private List<String> errorMessages;

    public BulkLoadStatus(String bulkLoadingId, int totalRecords) {
        this.bulkLoadingId = bulkLoadingId;
        this.totalRecords = totalRecords;
        this.successRecords = 0;
        this.failedRecords = 0;
        this.errorMessages = new ArrayList<>();
    }

    public void recordSuccess() {
        this.successRecords++;
    }

    public void recordFailure(String errorMessage) {
        this.failedRecords++;
        this.errorMessages.add(errorMessage);
    }
}
