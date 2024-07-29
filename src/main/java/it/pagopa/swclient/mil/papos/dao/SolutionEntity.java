package it.pagopa.swclient.mil.papos.dao;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MongoEntity(database = "mil", collection = "solutions")
public class SolutionEntity extends PanacheMongoEntity {

    private String pspId;
    private String locationCode;
}
