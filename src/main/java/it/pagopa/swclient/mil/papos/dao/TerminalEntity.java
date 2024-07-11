package it.pagopa.swclient.mil.papos.dao;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MongoEntity(database = "mil", collection = "terminals")
public class TerminalEntity extends PanacheMongoEntity {

    private String pspId;
    private String terminalId;
    private Boolean enabled;
    private String payeeCode;
    private String terminalUuid;
    private List<String> workstations;
}
