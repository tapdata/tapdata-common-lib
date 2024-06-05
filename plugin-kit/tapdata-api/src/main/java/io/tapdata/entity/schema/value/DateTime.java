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
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

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
    public boolean isContainsIllegal() {
        return containsIllegal;
    }
    private boolean containsIllegal = false;

    public DateTime() {
        originType = ORIGIN_TYPE_NONE;
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
        seconds = time / 1000;
        nano = (int) ((time % 1000) * 1000000);
        originType = ORIGIN_TYPE_DATE;
    }

    public DateTime(java.sql.Date date) {
        if (date == null)
            throw new IllegalArgumentException("DateTime constructor date is null");
        long time = date.getTime();
        seconds = time / 1000;
        nano = (int) ((time % 1000) * 1000000);
        originType = ORIGIN_TYPE_SQL_DATE;
    }

    public DateTime(java.sql.Time time) {
        if (time == null)
            throw new IllegalArgumentException("DateTime constructor time is null");
        long sqlTime = time.getTime();
        seconds = sqlTime / 1000;
        nano = (int) ((sqlTime % 1000) * 1000000);
        originType = ORIGIN_TYPE_TIME;
    }

    public DateTime(Timestamp timestamp) {
        if (timestamp == null)
            throw new IllegalArgumentException("DateTime constructor timestamp is null");
        long time = timestamp.getTime();
        seconds = time / 1000;
        nano = timestamp.getNanos();
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

        seconds = time / 1000;
        nano = (int) ((time % 1000) * 1000000);
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
        long milliseconds;
        if (seconds != null) {
            milliseconds = seconds * 1000;
            if (nano != null) {
                milliseconds = milliseconds + (nano / 1000 / 1000);
            }
        } else {
            return null;
        }
        return new Date(milliseconds);
    }

    public java.sql.Date toSqlDate() {
        long milliseconds;
        if (seconds != null) {
            milliseconds = seconds * 1000;
            if (nano != null) {
                milliseconds = milliseconds + (nano / 1000 / 1000);
            }
        } else {
            return null;
        }
        return new java.sql.Date(milliseconds);
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
}
