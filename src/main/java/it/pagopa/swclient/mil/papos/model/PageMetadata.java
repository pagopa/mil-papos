package it.pagopa.swclient.mil.papos.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PageMetadata(int size, long totalElements, int totalPages) {
}
