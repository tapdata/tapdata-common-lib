package io.tapdata.pdk.apis.entity;

import io.tapdata.entity.schema.value.DateTime;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static io.tapdata.entity.simplify.TapSimplify.entry;
import static io.tapdata.entity.simplify.TapSimplify.map;

public class QueryOperator implements Serializable {
    public static final int GT = 1;
    public static final int GTE = 2;
    public static final int LT = 3;
    public static final int LTE = 4;

    private String key;
    private Object value;
    private int operator;

    private Object originalValue;

    private Boolean fastQuery = false;

    private Long number;

    private TapTimeForm form;

    private TapTimeUnit unit;


    public QueryOperator() {

    }

    public QueryOperator(String key, Object value, int operator) {
        this.key = key;
        this.value = value;
        this.operator = operator;
    }

    public static QueryOperator gt(String key, Object value) {
        return new QueryOperator(key, value, GT);
    }

    public static QueryOperator gte(String key, Object value) {
        return new QueryOperator(key, value, GTE);
    }

    public static QueryOperator lt(String key, Object value) {
        return new QueryOperator(key, value, LT);
    }

    public static QueryOperator lte(String key, Object value) {
        return new QueryOperator(key, value, LTE);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public String toString() {
        return toString("");
    }

    public Object getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(Object originalValue) {
        this.originalValue = originalValue;
    }

    public boolean isFastQuery() {
        return fastQuery;
    }

    public void setFastQuery(boolean fastQuery) {
        this.fastQuery = fastQuery;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public TapTimeForm getForm() {
        return form;
    }

    public void setForm(TapTimeForm form) {
        this.form = form;
    }

    public TapTimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TapTimeUnit unit) {
        this.unit = unit;
    }

    public String toString(String quote) {
        String operatorStr;
        switch (operator) {
            case GT:
                operatorStr = ">";
                break;
            case GTE:
                operatorStr = ">=";
                break;
            case LT:
                operatorStr = "<";
                break;
            case LTE:
                operatorStr = "<=";
                break;
            default:
                operatorStr = "";
                break;
        }
        StringBuilder sb = new StringBuilder(quote + key + quote + operatorStr);
        if (value instanceof DateTime) {
            sb.append(toTimestampString((DateTime) value));
        } else if(value instanceof Number) {
            sb.append(value);
        } else {
            sb.append("'").append(value).append("'");
        }
        return sb.toString();
    }

    public static String toTimestampString(DateTime dateTime) {
        StringBuilder sb = new StringBuilder("'" + formatTapDateTime(dateTime, "yyyy-MM-dd HH:mm:ss"));
        if (dateTime.getNano() > 0) {
            DecimalFormat decimalFormat = new DecimalFormat("000000000");
            sb.append(".").append(decimalFormat.format(dateTime.getNano()).replaceAll("(0)+$", ""));
        }
        sb.append('\'');
        return sb.toString();
    }

    public static String formatTapDateTime(DateTime dateTime, String pattern) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
            final ZoneId zoneId = dateTime.getTimeZone() != null ? dateTime.getTimeZone().toZoneId() : ZoneId.of("GMT");
            LocalDateTime localDateTime = LocalDateTime.ofInstant(dateTime.toInstant(), zoneId);
            return dateTimeFormatter.format(localDateTime);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        QueryOperator queryOperator = new QueryOperator("a", map(entry("aa", "aa")), LT);
        System.out.println(queryOperator);
    }
}
