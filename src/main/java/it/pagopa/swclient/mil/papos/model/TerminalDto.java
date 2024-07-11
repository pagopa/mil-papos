package it.pagopa.swclient.mil.papos.model;

import it.pagopa.swclient.mil.papos.util.ErrorCodes;
import it.pagopa.swclient.mil.papos.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record TerminalDto(@NotNull(message = ErrorCodes.ERROR_PSPID_MUST_NOT_BE_NULL_MSG)
                          @Pattern(regexp = RegexPatterns.ASCII_PRINTABLE_1_TO_64_PATTERN)
                          String pspId,

                          @NotNull(message = ErrorCodes.ERROR_TERMINALID_MUST_NOT_BE_NULL_MSG)
                          @Pattern(regexp = RegexPatterns.FOUR_TO_TWELVE_DIGITS_PATTERN)
                          String terminalId,

                          @NotNull(message = ErrorCodes.ERROR_ENABLED_MUST_NOT_BE_NULL_MSG)
                          Boolean enabled,

                          @NotNull(message = ErrorCodes.ERROR_PAYEECODE_MUST_NOT_BE_NULL_MSG)
                          @Pattern(regexp = RegexPatterns.EXACT_ELEVEN_NUM_PATTERN)
                          String payeeCode,

                          List<String> workstations) {
}
