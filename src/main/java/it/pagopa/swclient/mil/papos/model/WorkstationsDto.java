package it.pagopa.swclient.mil.papos.model;

import it.pagopa.swclient.mil.papos.util.ErrorCodes;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record WorkstationsDto(
        @NotNull(message = ErrorCodes.ERROR_WORKSTATIONS_MUST_NOT_BE_NULL_MSG)
        List<String> workstations) {
}
