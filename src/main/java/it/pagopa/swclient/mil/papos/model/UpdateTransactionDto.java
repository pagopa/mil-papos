package it.pagopa.swclient.mil.papos.model;

import it.pagopa.swclient.mil.papos.util.ErrorCodes;
import it.pagopa.swclient.mil.papos.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateTransactionDto(@NotNull(message = ErrorCodes.ERROR_AMOUNT_MUST_NOT_BE_NULL_MSG)
                                   Long amount,

                                   @NotNull(message = ErrorCodes.ERROR_STATUS_MUST_NOT_BE_NULL_MSG)
                                   @Pattern(regexp = RegexPatterns.TRANSACTION_STATUS_PATTERN)
                                   String status) {
}
