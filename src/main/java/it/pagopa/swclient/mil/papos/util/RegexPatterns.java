package it.pagopa.swclient.mil.papos.util;

public class RegexPatterns {
    public static final String GENERIC_ID_PATTERN = "^[\\x{0001}-\\x{D7FF}\\x{E000}-\\x{FFFD}\\x{10000}-\\x{10FFFF}]{1,35}$";
    public static final String MAX_EIGHT_NUM_PATTERN = "^\\d{8}$";
    public static final String ALPHANUMERIC_UPPERCASE_LIMITED_PATTERN = "^[A-Z0-9]{1,16}$";
    public static final String REQUEST_ID_PATTERN = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";

    private RegexPatterns() {
    }
}

