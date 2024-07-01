package it.pagopa.swclient.mil.papos.dao;

import java.util.List;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TerminalPageResponse(List<TerminalEntity> terminals, PageMetadata page) {
}
