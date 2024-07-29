package it.pagopa.swclient.mil.papos.dao;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

@ApplicationScoped
public class SolutionRepository implements ReactivePanacheMongoRepositoryBase<SolutionEntity, ObjectId> {
}
