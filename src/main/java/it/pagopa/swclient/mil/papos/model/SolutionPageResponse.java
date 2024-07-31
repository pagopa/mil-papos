package it.pagopa.swclient.mil.papos.model;


import java.util.List;
import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.swclient.mil.papos.dao.SolutionEntity;

@RegisterForReflection
public record SolutionPageResponse(List<SolutionEntity> terminals, PageMetadata page) {
}