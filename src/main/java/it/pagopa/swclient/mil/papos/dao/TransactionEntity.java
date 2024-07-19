package it.pagopa.swclient.mil.papos.dao;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@MongoEntity(database = "mil", collection = "transactions")
public class TransactionEntity extends PanacheMongoEntity {

    private String transactionId;
    private String pspId;
    private String terminalId;
    private String noticeNumber;
    private String payeeCode;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private String status;
    private Long amount;
}
