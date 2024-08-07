package it.pagopa.swclient.mil.papos.util;

import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

public class Utility {
    private Utility() {
    }

    public static String generateRandomUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static Date convertStringToDate(String date, boolean startOfDay) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalTime localTime = startOfDay ? LocalTime.of(0, 0, 0) : LocalTime.of(23, 59, 59);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, localTime, ZoneOffset.UTC);

        return Date.from(zonedDateTime.toInstant());
    }

    public static ObjectId roundCeilObjectIdhex(Date date) {
        ObjectId objectId = new ObjectId(date);
        objectId = new ObjectId(objectId.toHexString().substring(0, 8) + "0000000000000000");

        return objectId;
    }
}
