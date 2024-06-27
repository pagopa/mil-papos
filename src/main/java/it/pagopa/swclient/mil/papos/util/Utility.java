package it.pagopa.swclient.mil.papos.util;

import java.util.UUID;

public class Utility {
    private Utility() {
    }

    public static String generateTerminalUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
