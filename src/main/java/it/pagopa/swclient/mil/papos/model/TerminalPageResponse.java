package it.pagopa.swclient.mil.papos.model;

import java.util.List;
import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;

@RegisterForReflection
public record TerminalPageResponse(List<TerminalEntity> terminals, PageMetadata page) {
}
