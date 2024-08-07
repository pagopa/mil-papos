package it.pagopa.swclient.mil.papos.util;

public class RegexPatterns {
    public static final String ASCII_PRINTABLE_1_TO_64_PATTERN = "^[ -~]{1,64}$";
    public static final String FOUR_TO_TWELVE_DIGITS_PATTERN = "^\\d{4,12}$";
    public static final String REQUEST_ID_PATTERN = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";
    public static final String EXACT_EIGHTEEN_NUM_PATTERN = "^\\d{18}$";
    public static final String EXACT_ELEVEN_NUM_PATTERN = "^\\d{11}$";
    public static final String SORT_STRATEGY_PATTERN = "asc|desc";
    public static final String TRANSACTION_STATUS_PATTERN = "CLOSED_OK|ERROR_ON_PAYMENT|ABORT|CREATED";
    public static final String MONGO_OBJECT_ID_PATTERN = "^[a-fA-F0-9]{24}$";

    private RegexPatterns() {
    }
}

