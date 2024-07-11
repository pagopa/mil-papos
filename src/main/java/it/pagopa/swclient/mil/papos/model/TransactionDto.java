package it.pagopa.swclient.mil.papos.model;

import it.pagopa.swclient.mil.papos.util.ErrorCodes;
import it.pagopa.swclient.mil.papos.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TransactionDto(@NotNull(message = ErrorCodes.ERROR_PSPID_MUST_NOT_BE_NULL_MSG)
                             @Pattern(regexp = RegexPatterns.ASCII_PRINTABLE_1_TO_64_PATTERN)
                             String pspId,

                             @NotNull(message = ErrorCodes.ERROR_TERMINALID_MUST_NOT_BE_NULL_MSG)
                             @Pattern(regexp = RegexPatterns.FOUR_TO_TWELVE_DIGITS_PATTERN)
                             String terminalId,

                             @NotNull(message = ErrorCodes.ERROR_PAYEECODE_MUST_NOT_BE_NULL_MSG)
                             @Pattern(regexp = RegexPatterns.EXACT_EIGHTEEN_NUM_PATTERN)
                             String noticeNumber,

                             @NotNull(message = ErrorCodes.ERROR_PAYEECODE_MUST_NOT_BE_NULL_MSG)
                             @Pattern(regexp = RegexPatterns.EXACT_ELEVEN_NUM_PATTERN)
                             String payeeCode) {
}
