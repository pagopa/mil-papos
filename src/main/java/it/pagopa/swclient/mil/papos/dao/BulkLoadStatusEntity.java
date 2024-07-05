package it.pagopa.swclient.mil.papos.dao;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MongoEntity(database = "mil", collection = "bulkLoadStatus")
public class BulkLoadStatusEntity extends PanacheMongoEntity {

    private String bulkLoadingId;
    private int totalRecords;
    private int successRecords;
    private int failedRecords;
    private List<String> errorMessages;
}
