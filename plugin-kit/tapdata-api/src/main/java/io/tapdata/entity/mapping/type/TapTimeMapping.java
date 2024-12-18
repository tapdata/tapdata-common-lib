package io.tapdata.entity.mapping.type;

import io.tapdata.entity.logger.TapLogger;
import io.tapdata.entity.result.ResultItem;
import io.tapdata.entity.result.TapResult;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapTime;
import io.tapdata.entity.schema.type.TapType;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * "time": {"range": ["-838:59:59","838:59:59"], "to": "typeInterval:typeNumber"},
 */
public class TapTimeMapping extends TapDateBase {
    private static final String TAG = TapTimeMapping.class.getSimpleName();

    @Override
    protected String pattern() {
        return "HH:mm:ss";
    }

    @Override
    public TapType toTapType(String dataType, Map<String, String> params) {
        String fractionStr = getParam(params, KEY_FRACTION);
        Integer fraction = null;
        if (fractionStr != null) {
            fractionStr = fractionStr.trim();
            try {
                fraction = Integer.parseInt(fractionStr);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        if(fraction == null)
            fraction = defaultFraction;
        if(fraction == null)
            fraction = maxFraction;
        return new TapTime().bytes(bytes).min(min).max(max).withTimeZone(withTimeZone).fraction(fraction);
    }

    @Override
    protected Instant parse(String timeString, String thePattern) {
        String patternLowerCase = thePattern.toLowerCase();
        if(patternLowerCase.startsWith("hh") && !patternLowerCase.endsWith(":ss")) {
            thePattern = "yyyy-MM-dd " + thePattern;
            timeString = "1970-01-01 " + timeString;

            try {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(thePattern);
                LocalDateTime localDateTime = LocalDateTime.parse(timeString, dateTimeFormatter);
                return localDateTime.atZone(ZoneId.of("GMT-0")).toInstant();
            } catch (Throwable e) {
//                e.printStackTrace();
                TapLogger.debug(TAG, "Parse time {} pattern {}, failed, {}", timeString, thePattern, e.getMessage());
            }
        } else {
            try {
                return Instant.ofEpochMilli(Time.valueOf(timeString).getTime());
            } catch(Throwable throwable) {
//                throwable.printStackTrace();
                TapLogger.debug(TAG, "parse time {} pattern {}, failed {}", timeString, thePattern, throwable.getMessage());
            }
        }
        return null;
    }

    @Override
    public TapResult<String> fromTapType(String typeExpression, TapType tapType) {
        String theFinalExpression = null;
        TapResult<String> tapResult = new TapResult<>();
        if (tapType instanceof TapTime) {
            TapTime tapTime = (TapTime) tapType;
            theFinalExpression = typeExpression;

            Integer fraction = tapTime.getFraction();
            if (fraction != null) {
                theFinalExpression = clearBrackets(theFinalExpression, "$" + KEY_FRACTION, false);

                if(this.maxFraction != null && this.minFraction != null) {
                    if(minFraction > fraction) {
                        tapResult.addItem(new ResultItem("TapDateTimeMapping MIN_FRACTION", TapResult.RESULT_SUCCESSFULLY_WITH_WARN, "Fraction " + fraction + " from source exceeded the minimum of target fraction " + this.minFraction + ", expression " + typeExpression));
                        fraction = minFraction;
                    } else if(maxFraction < fraction) {
                        tapResult.addItem(new ResultItem("TapDateTimeMapping MAX_FRACTION", TapResult.RESULT_SUCCESSFULLY_WITH_WARN, "Precision " + fraction + " from source exceeded the maximum of target fraction " + this.maxFraction + ", expression " + typeExpression));
                        fraction = maxFraction;
                    }
                }
                theFinalExpression = theFinalExpression.replace("$" + KEY_FRACTION, String.valueOf(fraction));
            }

            theFinalExpression = removeBracketVariables(theFinalExpression, 0);
            if(tapResult.getResultItems() != null && !tapResult.getResultItems().isEmpty())
                tapResult.result(TapResult.RESULT_SUCCESSFULLY_WITH_WARN);
            else
                tapResult.result(TapResult.RESULT_SUCCESSFULLY);
            return tapResult.data(theFinalExpression);
        }
        return null;
    }
    final BigDecimal rangeValue = BigDecimal.valueOf(10).pow(19);
    final BigDecimal timeZoneValue = BigDecimal.valueOf(10).pow(17);
    @Override
    public BigDecimal matchingScore(TapField field) {
        if (field.getTapType() instanceof TapTime) {
            TapTime tapTime = (TapTime) field.getTapType();

            //field is primary key, but this type is not able to be primary type.
            if(field.getPrimaryKey() != null && field.getPrimaryKey() && pkEnablement != null && !pkEnablement) {
                return TapMapping.MIN_SCORE;
            }

            BigDecimal score = BigDecimal.ZERO;

            Boolean withTimeZone = tapTime.getWithTimeZone();

            Instant max = tapTime.getMax();
            Instant min = tapTime.getMin();

            if((withTimeZone != null && withTimeZone && this.withTimeZone != null && this.withTimeZone) ||
                    ((withTimeZone == null || !withTimeZone) && (this.withTimeZone == null || !this.withTimeZone))) {
                score = score.add(timeZoneValue);
            } else {
                score = score.subtract(timeZoneValue);
            }

            if(min != null && max != null && this.min != null && this.max != null)
                score = score.add(calculateScoreForValue(min, max, this.min, this.max, rangeValue));

            return score;
        }

        return TapMapping.MIN_SCORE;
    }
}
