package it.pagopa.swclient.mil.papos.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;

import java.util.List;

@RegisterForReflection
public record TransactionPageResponse(List<TransactionEntity> transactions, PageMetadata page) {
}
