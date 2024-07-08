package it.pagopa.swclient.mil.papos.util;

import java.util.UUID;

public class Utility {
    private Utility() {
    }

    public static String generateRandomUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
