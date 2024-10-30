package io.tapdata.entity.codec.impl.utils;

import io.tapdata.entity.schema.value.DateTime;
import io.tapdata.entity.schema.value.TapDateTimeValue;
import io.tapdata.entity.simplify.pretty.ClassHandlersV2;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

public class AnyTimeToDateTime {
    private static final ClassHandlersV2 classHandlers = new ClassHandlersV2();

    static {
        classHandlers.register(Date.class, DateTime::new);
        classHandlers.register(java.sql.Date.class, DateTime::new);
        classHandlers.register(java.sql.Time.class, DateTime::new);
        classHandlers.register(LocalDateTime.class, DateTime::new);
        classHandlers.register(Instant.class, DateTime::new);
        classHandlers.register(Long.class, DateTime::new);
        classHandlers.register(long.class, DateTime::new);
        classHandlers.register(Timestamp.class, DateTime::new);
        classHandlers.register(ZonedDateTime.class, DateTime::new);
    }

    private static Object specialTypeHandler(Object obj) {
        if(obj instanceof Number)
            obj = ((Number) obj).longValue();
        else if(obj instanceof TapDateTimeValue)
            return ((TapDateTimeValue) obj).getOriginValue();
        return obj;
    }

    public static DateTime toDateTime(Object obj) {
        if(obj == null)
            return null;
        obj = specialTypeHandler(obj);
        if(obj instanceof DateTime)
            return (DateTime) obj;
        return (DateTime) classHandlers.handle(obj);
    }

    public static DateTime toDateTime(Object obj, Integer fraction) {
        if(fraction != null && obj instanceof Long) {
            return new DateTime((Long) obj, fraction);
        } else {
            return toDateTime(obj);
        }
    }

    public static DateTime withTimeStr(String timeStr) {
        return DateTime.withTimeStr(timeStr);
    }

    public static DateTime withDateStr(String dateStr) {
        return DateTime.withDateStr(dateStr);
    }

    public static long convertTimestamp(long timestamp, TimeZone fromTimeZone, TimeZone toTimeZone) {
        LocalDateTime dt = LocalDateTime.now();
        ZonedDateTime fromZonedDateTime = dt.atZone(fromTimeZone.toZoneId());
        ZonedDateTime toZonedDateTime = dt.atZone(toTimeZone.toZoneId());
        long diff = Duration.between(toZonedDateTime, fromZonedDateTime).toMillis();

        return timestamp + diff;
    }
}
