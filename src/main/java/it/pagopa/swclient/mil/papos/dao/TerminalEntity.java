package it.pagopa.swclient.mil.papos.dao;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MongoEntity(database = "mil", collection = "terminalRegistry")
public class TerminalEntity extends PanacheMongoEntity {

    private String terminalUuid;

    private Terminal terminal;
}
