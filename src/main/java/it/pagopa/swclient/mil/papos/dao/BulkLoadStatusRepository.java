package it.pagopa.swclient.mil.papos.dao;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BulkLoadStatusRepository implements ReactivePanacheMongoRepositoryBase<BulkLoadStatusEntity, String> {
}

