package io.tapdata.entity.schema.value;

import io.tapdata.entity.error.CoreException;
import io.tapdata.entity.error.TapAPIErrorCodes;
import io.tapdata.entity.serializer.JavaCustomSerializer;
import io.tapdata.entity.utils.io.DataInputStreamEx;
import io.tapdata.entity.utils.io.DataOutputStreamEx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.UnaryOperator;

public class DateTime implements Serializable, JavaCustomSerializer, Comparable<DateTime> {
    public static final int ORIGIN_TYPE_NONE = 1;
    public static final String DATETIME_TYPE = "datetime";
    public static final String DATE_TYPE = "date";
    public static final String TIME_TYPE = "time";
    public static final String YEAR_TYPE = "year";
    private static final int ORIGIN_TYPE_ZONED_DATE_TIME = 10;
    private static final int ORIGIN_TYPE_INSTANT = 20;
    private static final int ORIGIN_TYPE_DATE = 30;
    private static final int ORIGIN_TYPE_SQL_DATE = 40;
    private static final int ORIGIN_TYPE_TIME = 50;
    private static final int ORIGIN_TYPE_TIMESTAMP = 60;
    private static final int ORIGIN_TYPE_BIG_DECIMAL_FRACTION = 70;
    private static final int ORIGIN_TYPE_LONG_FRACTION = 80;
    private static final int ORIGIN_TYPE_LONG = 90;
    private static final int ORIGIN_TYPE_LOCAL_DATETIME = 100;
    private int originType;
    public int getOriginType() {
        return originType;
    }
    private int fraction = 3;
    public int getFraction() {
        return fraction;
    }
    /**
     * 秒数
     */
    private Long seconds;
    /**
     * 纳秒
     * <p>
     * 毫秒， 微秒， 纳秒， 1000
     */
    //nano int
    private Integer nano;
    /**
     * 时区 GMT+8
     */
    private TimeZone timeZone;
    private String illegalDate;
    public String getIllegalDate() {
        return illegalDate;
    }
    private byte[] originBytes;
    public byte[] getOriginBytes() {
        return originBytes;
    }
    public void setOriginBytes(byte[] originBytes) {
        this.originBytes = originBytes;
    }
    public boolean isContainsIllegal() {
        return containsIllegal;
    }
    private boolean containsIllegal = false;

    public DateTime() {
        originType = ORIGIN_TYPE_NONE;
        updateFromDate(new Date());
    }

    public DateTime(String illegalDate, String dateType) {
        illegalDate = autofillWithZero(illegalDate, dateType);
        this.illegalDate = illegalDate;
        this.containsIllegal = true;
    }
    public DateTime(ZonedDateTime zonedDateTime) {
        this(zonedDateTime.toInstant());
        timeZone = TimeZone.getTimeZone(zonedDateTime.getZone());
        originType = ORIGIN_TYPE_ZONED_DATE_TIME;
    }

    public DateTime(Instant instant) {
        if (instant == null)
            throw new IllegalArgumentException("DateTime constructor instant is null");
        seconds = instant.getEpochSecond();
        nano = instant.getNano();
        originType = ORIGIN_TYPE_INSTANT;
    }

    public DateTime(Date date) {
        if (date == null)
            throw new IllegalArgumentException("DateTime constructor date is null");
        long time = date.getTime();
        seconds = Math.floorDiv(time, 1000L);
        nano = (int)(Math.floorMod(time, 1000L) * 1000000);
        if (nano < 0) {
            seconds -= 1;
            nano += 1000000000;
        }
        originType = ORIGIN_TYPE_DATE;
    }

    public DateTime(java.sql.Date date) {
        if (date == null)
            throw new IllegalArgumentException("DateTime constructor date is null");
        long time = date.getTime();
        seconds = Math.floorDiv(time, 1000L);
        nano = (int)(Math.floorMod(time, 1000L) * 1000000);
        if (nano < 0) {
            seconds -= 1;
            nano += 1000000000;
        }
        originType = ORIGIN_TYPE_SQL_DATE;
    }

    public DateTime(java.sql.Time time) {
        if (time == null)
            throw new IllegalArgumentException("DateTime constructor time is null");
        long sqlTime = time.getTime();
        seconds = Math.floorDiv(sqlTime, 1000L);
        nano = (int)(Math.floorMod(sqlTime, 1000L) * 1000000);
        if (nano < 0) {
            seconds -= 1;
            nano += 1000000000;
        }
        originType = ORIGIN_TYPE_TIME;
    }

    public DateTime(Timestamp timestamp) {
        if (timestamp == null)
            throw new IllegalArgumentException("DateTime constructor timestamp is null");
        long time = timestamp.getTime();
        seconds = Math.floorDiv(time, 1000L);
        nano = timestamp.getNanos();
        if (nano < 0) {
            seconds -= 1;
            nano += 1000000000;
        }
        originType = ORIGIN_TYPE_TIMESTAMP;
    }

    public DateTime(BigDecimal time) {
        this(time, 9);
    }
    public DateTime(BigDecimal time, int fraction) {
        if (time == null)
            throw new IllegalArgumentException("DateTime constructor time is null");
        if (fraction > 9 || fraction < 0) {
            throw new IllegalArgumentException("Fraction must be 0~9");
        }

        seconds = time.divide(BigDecimal.valueOf(((Double) Math.pow(10, fraction)).longValue()), RoundingMode.HALF_UP).longValue();
        nano = time.divideAndRemainder(BigDecimal.valueOf(((Double) Math.pow(10, fraction)).longValue()))[1].multiply(BigDecimal.valueOf(((Double) Math.pow(10, 9 - fraction)).longValue())).intValue();
        if (nano < 0) {
            seconds -= 1;
            nano += 1000000000;
        }
        originType = ORIGIN_TYPE_BIG_DECIMAL_FRACTION;
        this.fraction = fraction;
    }

    public DateTime(Long time, int fraction) {
        if (time == null)
            throw new IllegalArgumentException("DateTime constructor time is null");
        if (fraction > 9 || fraction < 0) {
            throw new IllegalArgumentException("Fraction must be 0~9");
        }
        seconds = time / ((Double) Math.pow(10, fraction)).longValue();
        nano = (int) ((time % ((Double) Math.pow(10, fraction)).longValue()) * ((Double) Math.pow(10, 9 - fraction)).longValue());
        if (nano < 0) {
            seconds -= 1;
            nano += 1000000000;
        }
        originType = ORIGIN_TYPE_LONG_FRACTION;
        this.fraction = fraction;
//        switch (fraction) {
//            case 0:
//                seconds = time;
//                nano = 0;
//                break;
//            case 3:
//                seconds = time / 1000;
//                nano = (int) ((time % 1000) * 1000000);
//                break;
//            case 6:
//                seconds = time / 1000000;
//                nano = (int) ((time % 1000000) * 1000);
//                break;
//            case 9:
//                seconds = time / 1000000000;
//                nano = (int) (time % 1000000000);
//                break;
//            default:
//                throw new IllegalArgumentException("Fraction must be 0, 3, 6, 9");
//        }
    }

    /**
     * milliseconds
     *
     * @param time
     */
    public DateTime(Long time) {
        if (time == null)
            throw new IllegalArgumentException("DateTime constructor time is null");

        seconds = Math.floorDiv(time, 1000L);
        nano = (int)(Math.floorMod(time, 1000L) * 1000000);
        if (nano < 0) {
            seconds -= 1;
            nano += 1000000000;
        }
        originType = ORIGIN_TYPE_LONG;
    }

    public DateTime(LocalDateTime localDateTime) {
        if (localDateTime == null)
            throw new IllegalArgumentException("DateTime constructor localDateTime is null");
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        seconds = instant.getEpochSecond();
        nano = instant.getNano();
        originType = ORIGIN_TYPE_LOCAL_DATETIME;
    }

    public Object toOriginObject(int originType) {
        switch (originType) {
            case ORIGIN_TYPE_DATE:
                return toDate();
            case ORIGIN_TYPE_INSTANT:
                return toInstant();
            case ORIGIN_TYPE_LOCAL_DATETIME:
                return toZonedDateTime().toLocalDateTime();
            case ORIGIN_TYPE_SQL_DATE:
                return toSqlDate();
            case ORIGIN_TYPE_TIME:
                return toTime();
            case ORIGIN_TYPE_TIMESTAMP:
                return toTimestamp();
            case ORIGIN_TYPE_ZONED_DATE_TIME:
                return toZonedDateTime();
            case ORIGIN_TYPE_LONG:
            case ORIGIN_TYPE_LONG_FRACTION:
                return toLong();
            default:
                throw new CoreException(TapAPIErrorCodes.ERROR_ILLEGAL_DATETIME_ORIGIN_TYPE, "Illegal originType {} for DateTime", originType);
        }
    }

    public static DateTime withDateStr(String dateStr) {
        if (dateStr == null)
            throw new IllegalArgumentException("DateTime constructor dateStr is null");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return new DateTime(sdf.parse(dateStr).getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("DateTime constructor illegal dateStr: " + dateStr);
        }
    }

	public static DateTime withTimeStr(String timeStr) {
        if (timeStr == null)
            throw new IllegalArgumentException("DateTime constructor timeStr is null");

        DateTime dateTime = new DateTime();
        String[] scaleArr = timeStr.split("\\.");
        if (scaleArr.length > 1) {
            switch (scaleArr[1].length()) {
                case 1:
                case 2:
                case 3:
                    dateTime.nano = Integer.parseInt(scaleArr[1]) * 1000 * 1000;
                    break;
                case 4:
                case 5:
                case 6:
                    dateTime.nano = Integer.parseInt(scaleArr[1]) * 1000;
                    break;
                case 7:
                case 8:
                case 9:
                    dateTime.nano = Integer.parseInt(scaleArr[1]);
                    break;
                default:
                    throw new IllegalArgumentException("DateTime constructor illegal timeStr with nano: " + timeStr);
            }
        } else {
            dateTime.nano = 0;
        }
        boolean negative = false;
        if (scaleArr[0].startsWith("-")) {
            negative = true;
            scaleArr = scaleArr[0].substring(1).split(":");
        } else {
            scaleArr = scaleArr[0].split(":");
        }
        switch (scaleArr.length) {
            case 1:
                dateTime.seconds = Long.parseLong(scaleArr[0]);
                break;
            case 2:
                dateTime.seconds = Long.parseLong(scaleArr[0]) * 60 + Long.parseLong(scaleArr[1]);
                break;
            case 3:
                dateTime.seconds = Long.parseLong(scaleArr[0]) * 60 * 60 + Long.parseLong(scaleArr[1]) * 60 + Long.parseLong(scaleArr[2]);
                break;
            default:
                throw new IllegalArgumentException("DateTime constructor illegal timeStr: " + timeStr);
        }
        if (negative) {
            dateTime.seconds *= -1;
            dateTime.nano *= -1;
        }
        return dateTime;
    }

    public static DateTime fromObject(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("DateTime value is null");
        }
        if (value instanceof DateTime) {
            return copyOf((DateTime) value);
        }
        if (value instanceof Timestamp) {
            return new DateTime((Timestamp) value);
        }
        if (value instanceof java.sql.Date) {
            return new DateTime((java.sql.Date) value);
        }
        if (value instanceof Time) {
            return new DateTime((Time) value);
        }
        if (value instanceof Date) {
            return new DateTime((Date) value);
        }
        if (value instanceof ZonedDateTime) {
            return new DateTime((ZonedDateTime) value);
        }
        if (value instanceof LocalDateTime) {
            return new DateTime((LocalDateTime) value);
        }
        if (value instanceof Instant) {
            return new DateTime((Instant) value);
        }
        throw new IllegalArgumentException("Unsupported date value type: " + value.getClass().getName());
    }

    public static boolean isBefore(Object a, Object b) {
        return compare(a, b) < 0;
    }

    public static boolean isAfter(Object a, Object b) {
        return compare(a, b) > 0;
    }

    public static boolean isEqual(Object a, Object b) {
        return compare(a, b) == 0;
    }

    public static boolean isSameDay(Object a, Object b, ZoneId zoneId) {
        ZoneId actualZoneId = resolveZoneId(zoneId);
        LocalDate left = requiredDateTime(a).toInstant().atZone(actualZoneId).toLocalDate();
        LocalDate right = requiredDateTime(b).toInstant().atZone(actualZoneId).toLocalDate();
        return left.equals(right);
    }

    public static boolean isSameMonth(Object a, Object b, ZoneId zoneId) {
        ZoneId actualZoneId = resolveZoneId(zoneId);
        ZonedDateTime left = requiredDateTime(a).toInstant().atZone(actualZoneId);
        ZonedDateTime right = requiredDateTime(b).toInstant().atZone(actualZoneId);
        return left.getYear() == right.getYear() && left.getMonthValue() == right.getMonthValue();
    }

    public static int compare(Object a, Object b) {
        return requiredDateTime(a).compareTo(requiredDateTime(b));
    }

    public static DateTime addYears(Object value, int years) {
        return addWithZonedDateTime(value, dateTime -> dateTime.plusYears(years));
    }

    public static DateTime addMonths(Object value, int months) {
        return addWithZonedDateTime(value, dateTime -> dateTime.plusMonths(months));
    }

    public static DateTime addDays(Object value, int days) {
        return addWithZonedDateTime(value, dateTime -> dateTime.plusDays(days));
    }

    public static DateTime addHours(Object value, int hours) {
        return addWithZonedDateTime(value, dateTime -> dateTime.plusHours(hours));
    }

    public static DateTime addMinutes(Object value, int minutes) {
        return addWithZonedDateTime(value, dateTime -> dateTime.plusMinutes(minutes));
    }

    public static DateTime addSeconds(Object value, int seconds) {
        return addWithZonedDateTime(value, dateTime -> dateTime.plusSeconds(seconds));
    }

    public static long diffMillis(Object a, Object b) {
        return Duration.between(requiredDateTime(b).toInstant(), requiredDateTime(a).toInstant()).toMillis();
    }

    public static long diffSeconds(Object a, Object b) {
        return Duration.between(requiredDateTime(b).toInstant(), requiredDateTime(a).toInstant()).getSeconds();
    }

    public static long diffMinutes(Object a, Object b) {
        return Duration.between(requiredDateTime(b).toInstant(), requiredDateTime(a).toInstant()).toMinutes();
    }

    public static long diffHours(Object a, Object b) {
        return Duration.between(requiredDateTime(b).toInstant(), requiredDateTime(a).toInstant()).toHours();
    }

    public static long diffDays(Object a, Object b) {
        return Duration.between(requiredDateTime(b).toInstant(), requiredDateTime(a).toInstant()).toDays();
    }

    private static DateTime addWithZonedDateTime(Object value, UnaryOperator<ZonedDateTime> updater) {
        DateTime result = requiredDateTime(value);
        result.updateFromZonedDateTime(updater.apply(result.toZonedDateTime()));
        return result;
    }

    private static DateTime requiredDateTime(Object value) {
        DateTime dateTime = fromObject(value);
        if (dateTime.containsIllegal || dateTime.seconds == null || dateTime.nano == null) {
            throw new IllegalArgumentException("Illegal DateTime value for operation");
        }
        return dateTime;
    }

    private static ZoneId resolveZoneId(ZoneId zoneId) {
        return Optional.ofNullable(zoneId).orElseGet(ZoneId::systemDefault);
    }

    private static DateTime copyOf(DateTime source) {
        DateTime copy = new DateTime();
        copy.originType = source.originType;
        copy.fraction = source.fraction;
        copy.seconds = source.seconds;
        copy.nano = source.nano;
        copy.timeZone = source.timeZone == null ? null : (TimeZone) source.timeZone.clone();
        copy.illegalDate = source.illegalDate;
        copy.originBytes = source.originBytes == null ? null : source.originBytes.clone();
        copy.containsIllegal = source.containsIllegal;
        return copy;
    }

    public String toTimeStr() {
        DecimalFormat decimalFormat = new DecimalFormat("00");
        long realSecond;
        boolean negative = false;
        if (seconds < 0 || nano < 0) {
            realSecond = seconds * (-1);
            negative = true;
        } else {
            realSecond = seconds;
        }
        int hour = (int) (realSecond / (60 * 60));
        int minute = (int) (realSecond % (60 * 60) / 60);
        int second = (int) (realSecond % 60);
        String timeStr = decimalFormat.format(hour) + ":" + decimalFormat.format(minute) + ":" + decimalFormat.format(second);
        if (nano != 0) {
            timeStr += ("" + (double) Math.abs(nano) / 1000000000L).substring(1);
        }
        return (negative ? "-" : "") + timeStr;
    }

    public Instant toInstant() {
        return Instant.ofEpochSecond(seconds, nano);
    }

    public ZonedDateTime toZonedDateTime() {
        TimeZone theTimeZone = timeZone;
        if(theTimeZone == null)
            theTimeZone = TimeZone.getDefault();
        return ZonedDateTime.ofInstant(toInstant(), theTimeZone.toZoneId());
    }

    public Date toDate() {
        return Date.from(toInstant());
    }

    public java.sql.Date toSqlDate() {
        return java.sql.Date.valueOf(toLocalDateTime().toLocalDate());
    }

    public Long toLong() {
        if (fraction > 9 || fraction < 0) {
            throw new IllegalArgumentException("Fraction must be 0~9");
        }
        long time;
        time = seconds * ((Double) Math.pow(10, fraction)).longValue();
        time = time + nano / ((Double) Math.pow(10, 9 - fraction)).longValue();
        return time;
    }

    public BigDecimal toNanoSeconds() {
        BigDecimal nanoSeconds;
        if (seconds != null) {
            nanoSeconds = BigDecimal.valueOf(seconds).multiply(BigDecimal.valueOf(1000_000_000));
            if (nano != null) {
                nanoSeconds = nanoSeconds.add(BigDecimal.valueOf(nano));
            }
        } else {
            return null;
        }
        return nanoSeconds;
    }

    public java.sql.Time toTime() {
        long milliseconds;
        if (seconds != null) {
            milliseconds = seconds * 1000;
            if (nano != null) {
                milliseconds = milliseconds + (nano / 1000 / 1000);
            }
        } else {
            return null;
        }
        return new Time(milliseconds);
    }

    public Timestamp toTimestamp() {
        if (seconds != null) {
            Timestamp timestamp = new Timestamp(seconds * 1000);
            if (nano != null) {
                timestamp.setNanos(nano);
            }
            return timestamp;
        }
        return null;
    }

    public String toFormatString(String format) {
        return new SimpleDateFormat(format).format(new Date(toTimestamp().getTime() + (timeZone == null ? 0 : timeZone.getRawOffset())));
    }

    public String toFormatStringV2(String format) {
        return DateTimeFormatter.ofPattern(format).format(toLocalDateTime());
    }

    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.ofEpochSecond(seconds, nano, zoneOffset());
    }

    public ZoneOffset zoneOffset() {
        ZoneOffset zoneOffset = ZoneOffset.UTC;
        if (null != timeZone) {
            zoneOffset = ZoneOffset.ofTotalSeconds(timeZone.getOffset(seconds) / 1000);
        }
        return zoneOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateTime that = (DateTime) o;
        return Objects.equals(seconds, that.seconds) &&
                Objects.equals(timeZone, that.timeZone) &&
                Objects.equals(nano, that.nano);
    }

    @Override
    public String toString() {
        return "DateTime nano " + nano + " seconds " + seconds + " timeZone " + timeZone;
    }

    public Long getSeconds() {
        return seconds;
    }

    public void setSeconds(Long seconds) {
        this.seconds = seconds;
    }

    public Integer getNano() {
        return nano;
    }

    public void setNano(Integer nano) {
        this.nano = nano;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public void from(InputStream inputStream) throws IOException {
        DataInputStreamEx dataInputStream = dataInputStream(inputStream);
        originType = dataInputStream.original().readInt();
        fraction = dataInputStream.original().readInt();
        seconds = dataInputStream.readLong();
        nano = dataInputStream.readInt();
        containsIllegal = dataInputStream.readBoolean();
        illegalDate = dataInputStream.readUTF();
        int hasValue = dataInputStream.original().read();
        if(hasValue == DataOutputStreamEx.HASVALUE) {
            String zoneId = dataInputStream.original().readUTF();
            timeZone = TimeZone.getTimeZone(zoneId);
        }
    }

    @Override
    public void to(OutputStream outputStream) throws IOException {
        DataOutputStreamEx dataOutputStreamEx = dataOutputStream(outputStream);
        dataOutputStreamEx.original().writeInt(originType);
        dataOutputStreamEx.original().writeInt(fraction);
        dataOutputStreamEx.writeLong(seconds);
        dataOutputStreamEx.writeInt(nano);
        dataOutputStreamEx.writeBoolean(containsIllegal);
        dataOutputStreamEx.writeUTF(illegalDate);
        if(timeZone != null) {
            dataOutputStreamEx.original().write(DataOutputStreamEx.HASVALUE);
            dataOutputStreamEx.original().writeUTF(timeZone.getID());
        } else
            dataOutputStreamEx.original().write(DataOutputStreamEx.NOVALUE);

    }

    @Override
    public int compareTo(DateTime o) {
        int compareTo = 0;
        if(seconds != null && o.seconds != null) {
            compareTo = seconds.compareTo(o.seconds);
        }
        if(compareTo == 0 && nano != null && o.nano != null) {
            compareTo = nano.compareTo(o.nano);
        }
        return compareTo;
    }

    public long toEpochMilli(){
        if (seconds < 0 && nano > 0) {
            long millis = Math.multiplyExact(seconds+1, 1000);
            long adjustment = nano / 1000_000 - 1000;
            return Math.addExact(millis, adjustment);
        } else {
            long millis = Math.multiplyExact(seconds, 1000);
            return Math.addExact(millis, nano / 1000_000);
        }
    }
    protected String autofillWithZero(String str, String dateType){
        if(str == null) return null;
        StringBuilder stringBuilder = new StringBuilder();
        String[] split = str.split("-");
        switch (dateType){
            case DATETIME_TYPE:
                stringBuilder.append(fill(split, 6));
                break;
            case DATE_TYPE:
                stringBuilder.append(fill(split, 2));
                break;
            case TIME_TYPE:
                if (split.length < 2) return null;
                for (int i=0;i< split.length;i++){
                    autofill(split[0],2, stringBuilder);
                    if (i < split.length - 1){
                        stringBuilder.append(":");
                    }
                }
                break;
            case YEAR_TYPE:
                autofill(str,4, stringBuilder);
                break;
        }
        return stringBuilder.toString();
    }
    private String fill(String[] split, int dateType){
        if (split.length < dateType) return null;
        StringBuilder stringBuilder = new StringBuilder();
        autofill(split[0],4,stringBuilder);
        stringBuilder.append("-");
        autofill(split[1],2,stringBuilder);
        stringBuilder.append("-");
        autofill(split[2],2,stringBuilder);
        if (dateType > 2){
            stringBuilder.append(" ");
            autofill(split[3],2,stringBuilder);
            stringBuilder.append(":");
            autofill(split[4],2,stringBuilder);
            stringBuilder.append(":");
            autofill(split[5],2,stringBuilder);
        }
        return stringBuilder.toString();
    }

    private StringBuilder autofill(String str, int i, StringBuilder stringBuilder){
        if (str == null) return stringBuilder;
        int length = str.length();
        if (length == i){
            stringBuilder.append(str);
        }else {
            stringBuilder.append(String.format("%0"+i+"d",Integer.parseInt(str)));
        }
        return stringBuilder;
    }

    // ==================== Delegate methods for java.util.Date ====================

    /**
     * @see Date#after(Date)
     */
    public boolean after(DateTime when) {
        return this.compareTo(when) > 0;
    }

    /**
     * @see Date#before(Date)
     */
    public boolean before(DateTime when) {
        return this.compareTo(when) < 0;
    }

    /**
     * @see Date#compareTo(Date)
     */
    public int compareTo(Date anotherDate) {
        return toDate().compareTo(anotherDate);
    }

    /**
     * @see Date#getDate()
     */
    public int getDate() {
        return toZonedDateTime().getDayOfMonth();
    }

    /**
     * @see Date#getDay()
     */
    public int getDay() {
        return toZonedDateTime().getDayOfWeek().getValue() % 7;
    }

    /**
     * JS Date.getFullYear() - returns the 4-digit year
     */
    public int getFullYear() {
        return toZonedDateTime().getYear();
    }

    /**
     * @see Date#getHours()
     */
    public int getHours() {
        return toZonedDateTime().getHour();
    }

    /**
     * JS Date.getMilliseconds() - returns milliseconds (0-999)
     */
    public int getMilliseconds() {
        return nano != null ? nano / 1000000 : 0;
    }

    /**
     * @see Date#getMinutes()
     */
    public int getMinutes() {
        return toZonedDateTime().getMinute();
    }

    /**
     * @see Date#getMonth()
     */
    public int getMonth() {
        return toZonedDateTime().getMonthValue() - 1;
    }

    /**
     * JS Date.getSeconds() / java.util.Date.getSeconds() - returns second of minute (0-59)
     */
    public int getDateSeconds() {
        return toZonedDateTime().getSecond();
    }

    /**
     * @see Date#getTime()
     */
    public long getTime() {
        return toDate().getTime();
    }

    /**
     * @see Date#getTimezoneOffset()
     * Returns offset in minutes (positive = behind UTC, negative = ahead of UTC, matching JS convention)
     */
    public int getTimezoneOffset() {
        return -toZonedDateTime().getOffset().getTotalSeconds() / 60;
    }

    /**
     * JS Date.getUTCDate() - returns day of month (1-31) in UTC
     */
    public int getUTCDate() {
        return toInstant().atZone(ZoneOffset.UTC).getDayOfMonth();
    }

    /**
     * JS Date.getUTCDay() - returns day of week (0=Sunday, 6=Saturday) in UTC
     */
    public int getUTCDay() {
        return toInstant().atZone(ZoneOffset.UTC).getDayOfWeek().getValue() % 7;
    }

    /**
     * JS Date.getUTCFullYear() - returns 4-digit year in UTC
     */
    public int getUTCFullYear() {
        return toInstant().atZone(ZoneOffset.UTC).getYear();
    }

    /**
     * JS Date.getUTCHours() - returns hours (0-23) in UTC
     */
    public int getUTCHours() {
        return toInstant().atZone(ZoneOffset.UTC).getHour();
    }

    /**
     * JS Date.getUTCMilliseconds() - returns milliseconds (0-999) in UTC
     */
    public int getUTCMilliseconds() {
        return getMilliseconds();
    }

    /**
     * JS Date.getUTCMinutes() - returns minutes (0-59) in UTC
     */
    public int getUTCMinutes() {
        return toInstant().atZone(ZoneOffset.UTC).getMinute();
    }

    /**
     * JS Date.getUTCMonth() - returns month (0-11) in UTC
     */
    public int getUTCMonth() {
        return toInstant().atZone(ZoneOffset.UTC).getMonthValue() - 1;
    }

    /**
     * JS Date.getUTCSeconds() - returns seconds (0-59) in UTC
     */
    public int getUTCSeconds() {
        return toInstant().atZone(ZoneOffset.UTC).getSecond();
    }

    /**
     * @see Date#getYear()
     */
    public int getYear() {
        return toZonedDateTime().getYear() - 1900;
    }

    /**
     * @see Date#setDate(int)
     */
    public void setDate(int date) {
        ZonedDateTime zdt = toZonedDateTime().withDayOfMonth(date);
        updateFromZonedDateTime(zdt);
    }

    /**
     * JS Date.setSeconds() / java.util.Date.setSeconds() - sets second of minute (0-59)
     */
    public void setDateSeconds(int seconds) {
        ZonedDateTime zdt = toZonedDateTime().withSecond(seconds);
        updateFromZonedDateTime(zdt);
    }

    /**
     * JS Date.setFullYear() - sets the full year (4 digits)
     */
    public void setFullYear(int year) {
        ZonedDateTime zdt = toZonedDateTime();
        zdt = zdt.withYear(year);
        updateFromZonedDateTime(zdt);
    }

    /**
     * @see Date#setHours(int)
     */
    public void setHours(int hours) {
        ZonedDateTime zdt = toZonedDateTime().withHour(hours);
        updateFromZonedDateTime(zdt);
    }

    /**
     * JS Date.setMilliseconds() - sets the milliseconds (0-999)
     */
    public void setMilliseconds(int ms) {
        int subMilliNano = (nano != null) ? (nano % 1000000) : 0;
        this.nano = ms * 1000000 + subMilliNano;
    }

    /**
     * @see Date#setMinutes(int)
     */
    public void setMinutes(int minutes) {
        ZonedDateTime zdt = toZonedDateTime().withMinute(minutes);
        updateFromZonedDateTime(zdt);
    }

    /**
     * @see Date#setMonth(int)
     */
    public void setMonth(int month) {
        ZonedDateTime zdt = toZonedDateTime().withMonth(month + 1);
        updateFromZonedDateTime(zdt);
    }

    /**
     * @see Date#setTime(long)
     */
    public void setTime(long time) {
        this.seconds = Math.floorDiv(time, 1000L);
        this.nano = (int)(Math.floorMod(time, 1000L) * 1000000);
        if (this.nano < 0) {
            this.seconds -= 1;
            this.nano += 1000000000;
        }
    }

    /**
     * JS Date.setUTCDate() - sets day of month (1-31) in UTC
     */
    public void setUTCDate(int dayOfMonth) {
        ZonedDateTime zdt = toInstant().atZone(ZoneOffset.UTC);
        zdt = zdt.withDayOfMonth(dayOfMonth);
        updateFromInstant(zdt.toInstant());
    }

    /**
     * JS Date.setUTCFullYear() - sets the full year in UTC
     */
    public void setUTCFullYear(int year) {
        ZonedDateTime zdt = toInstant().atZone(ZoneOffset.UTC);
        zdt = zdt.withYear(year);
        updateFromInstant(zdt.toInstant());
    }

    /**
     * JS Date.setUTCHours() - sets the hours (0-23) in UTC
     */
    public void setUTCHours(int hours) {
        ZonedDateTime zdt = toInstant().atZone(ZoneOffset.UTC);
        zdt = zdt.withHour(hours);
        updateFromInstant(zdt.toInstant());
    }

    /**
     * JS Date.setUTCMilliseconds() - sets milliseconds (0-999) in UTC
     */
    public void setUTCMilliseconds(int ms) {
        setMilliseconds(ms);
    }

    /**
     * JS Date.setUTCMinutes() - sets minutes (0-59) in UTC
     */
    public void setUTCMinutes(int minutes) {
        ZonedDateTime zdt = toInstant().atZone(ZoneOffset.UTC);
        zdt = zdt.withMinute(minutes);
        updateFromInstant(zdt.toInstant());
    }

    /**
     * JS Date.setUTCMonth() - sets month (0-11) in UTC
     */
    public void setUTCMonth(int month) {
        ZonedDateTime zdt = toInstant().atZone(ZoneOffset.UTC);
        zdt = zdt.withMonth(month + 1);
        updateFromInstant(zdt.toInstant());
    }

    /**
     * JS Date.setUTCSeconds() - sets seconds (0-59) in UTC
     */
    public void setUTCSeconds(int seconds) {
        ZonedDateTime zdt = toInstant().atZone(ZoneOffset.UTC);
        zdt = zdt.withSecond(seconds);
        updateFromInstant(zdt.toInstant());
    }

    /**
     * @see Date#setYear(int)
     */
    public void setYear(int year) {
        ZonedDateTime zdt = toZonedDateTime().withYear(year + 1900);
        updateFromZonedDateTime(zdt);
    }

    /**
     * @see Date#toGMTString()
     */
    public String toGMTString() {
        return toDate().toGMTString();
    }

    /**
     * @see Date#toLocaleString()
     */
    public String toLocaleString() {
        return toDate().toLocaleString();
    }

    /**
     * JS Date.toDateString() - returns date portion as human-readable string
     * Format: "ddd MMM dd yyyy", e.g. "Wed May 08 2024"
     */
    public String toDateString() {
        ZonedDateTime zdt = toZonedDateTime();
        return DateTimeFormatter.ofPattern("EEE MMM dd yyyy", java.util.Locale.US).format(zdt);
    }

    /**
     * JS Date.toISOString() - returns ISO 8601 format string in UTC
     */
    public String toISOString() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .format(toInstant().atZone(ZoneOffset.UTC));
    }

    /**
     * JS Date.toJSON() - returns same as toISOString(), for JSON.stringify()
     */
    public String toJSON() {
        return toISOString();
    }

    /**
     * JS Date.toLocaleDateString() - returns locale-sensitive date string
     */
    public String toLocaleDateString() {
        return java.text.DateFormat.getDateInstance().format(toDate());
    }

    /**
     * JS Date.toLocaleTimeString() - returns locale-sensitive time string
     */
    public String toLocaleTimeString() {
        return java.text.DateFormat.getTimeInstance().format(toDate());
    }

    /**
     * JS Date.toTimeString() - returns time portion as human-readable string
     */
    public String toTimeString() {
        String full = toDate().toString();
        // java.util.Date.toString() format: "dow mon dd hh:mm:ss zzz yyyy"
        // time part starts at index 11
        int timeStart = full.indexOf(':') - 2;
        return timeStart >= 0 ? full.substring(timeStart) : full;
    }

    /**
     * JS Date.toUTCString() - returns UTC date string
     */
    public String toUTCString() {
        return toDate().toGMTString();
    }

    /**
     * JS Date.valueOf() - returns primitive value (milliseconds since epoch)
     */
    public long valueOf() {
        return getTime();
    }

    private void updateFromDate(Date d) {
        long time = d.getTime();
        int savedNano = this.nano != null ? (this.nano % 1000000) : 0;
        this.seconds = Math.floorDiv(time, 1000L);
        this.nano = (int)(Math.floorMod(time, 1000L) * 1000000) + savedNano;
        if (this.nano < 0) {
            this.seconds -= 1;
            this.nano += 1000000000;
        }
    }

    private void updateFromZonedDateTime(ZonedDateTime zdt) {
        Instant instant = zdt.toInstant();
        int savedSubMilliNano = this.nano != null ? (this.nano % 1000000) : 0;
        this.seconds = instant.getEpochSecond();
        this.nano = instant.getNano();
        // preserve sub-millisecond precision if original had it
        if (savedSubMilliNano > 0 && (this.nano % 1000000) == 0) {
            this.nano += savedSubMilliNano;
        }
    }

    private void updateFromInstant(Instant instant) {
        int savedSubMilliNano = this.nano != null ? (this.nano % 1000000) : 0;
        this.seconds = instant.getEpochSecond();
        this.nano = instant.getNano();
        if (savedSubMilliNano > 0 && (this.nano % 1000000) == 0) {
            this.nano += savedSubMilliNano;
        }
    }

    public void plus(long amountToAdd, TemporalUnit unit) {
        if (containsIllegal) {
            return;
        }
        if (seconds != null) {
            Instant instant = Instant.ofEpochSecond(seconds, nano);
            Instant plus = instant.plus(amountToAdd, unit);
            seconds = plus.getEpochSecond();
            nano = plus.getNano();
        }
    }

    public void minus(long amountToAdd, TemporalUnit unit) {
        if (containsIllegal) {
            return;
        }
        if (seconds != null) {
            Instant instant = Instant.ofEpochSecond(seconds, nano);
            Instant minus = instant.minus(amountToAdd, unit);
            seconds = minus.getEpochSecond();
            nano = minus.getNano();
        }
    }
}
