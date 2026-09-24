package io.tapdata.entity.mapping.type;

import io.tapdata.entity.result.TapResult;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapType;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves a floating-point type from the existing effective precision
 * parameter.  The {@code coefficient} object in a specification is mapping
 * metadata, not a TapNumber property.
 */
public class TapCoefficientFloatMapping extends TapMapping {
    public static final String KEY_COEFFICIENT = "coefficient";
    private static final String TAP_FLOAT = "TapFloat";
    private static final String TAP_DOUBLE = "TapDouble";

    private Map<String, int[]> coefficientRanges = new LinkedHashMap<>();
    private Map<String, Object> info;

    @Override
    public void from(Map<String, Object> info) {
        this.info = info;
        coefficientRanges = parseCoefficientRanges(info == null ? null : info.get(KEY_COEFFICIENT));
        if (coefficientRanges.isEmpty()) {
            throw new IllegalArgumentException("TapCoefficientFloat requires coefficient ranges");
        }
    }

    @Override
    public TapType toTapType(String dataType, Map<String, String> params) {
        Integer precision = resolvePrecision(dataType, params);
        String targetType = resolveTargetType(precision);
        if (TAP_FLOAT.equals(targetType)) {
            TapFloat type = new TapFloat();
            FloatingPointMappingSupport.apply(info, dataType, params, type);
            return type;
        }
        if (TAP_DOUBLE.equals(targetType)) {
            TapDouble type = new TapDouble();
            FloatingPointMappingSupport.apply(info, dataType, params, type);
            return type;
        }
        throw new IllegalArgumentException("Unsupported TapCoefficientFloat target type: " + targetType);
    }

    @Override
    public TapResult<String> fromTapType(String typeExpression, TapType tapType) {
        if (!(tapType instanceof TapFloat) && !(tapType instanceof TapDouble)) {
            return null;
        }
        Integer precision = precisionForOutput(tapType);
        String expression = typeExpression;
        if (precision != null) {
            expression = expression.replace("$precision", String.valueOf(precision));
        }
        expression = removeBracketVariables(expression, 0);
        return new TapResult<String>().result(TapResult.RESULT_SUCCESSFULLY).data(expression);
    }

    @Override
    public BigDecimal matchingScore(TapField field) {
        if (field == null || field.getTapType() == null) {
            return TapMapping.MIN_SCORE;
        }
        return field.getTapType() instanceof TapFloat || field.getTapType() instanceof TapDouble
                ? TapMapping.MAX_SCORE : TapMapping.MIN_SCORE;
    }

    private Integer resolvePrecision(String dataType, Map<String, String> params) {
        TapNumberMapping numberMapping = new TapNumberMapping();
        numberMapping.from(info);
        TapNumber number = (TapNumber) numberMapping.toTapType(dataType, params);
        if (number.getPrecision() == null) {
            throw new IllegalArgumentException("TapCoefficientFloat precision is missing");
        }
        return number.getPrecision();
    }

    private String resolveTargetType(Integer precision) {
        if (precision == null) {
            throw new IllegalArgumentException("TapCoefficientFloat precision is missing");
        }
        for (Map.Entry<String, int[]> entry : coefficientRanges.entrySet()) {
            int[] range = entry.getValue();
            if (precision >= range[0] && precision <= range[1]) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("TapCoefficientFloat precision out of range: " + precision);
    }

    private Integer precisionForOutput(TapType tapType) {
        if (tapType instanceof TapFloat) {
            Integer binaryPrecision = ((TapFloat) tapType).getBinaryPrecision();
            return binaryPrecision == null ? maxRange(TAP_FLOAT) : binaryPrecision;
        }
        Integer binaryPrecision = ((TapDouble) tapType).getBinaryPrecision();
        return binaryPrecision == null ? maxRange(TAP_DOUBLE) : binaryPrecision;
    }

    private Integer maxRange(String targetType) {
        int[] range = coefficientRanges.get(targetType);
        return range == null ? null : range[1];
    }

    private Map<String, int[]> parseCoefficientRanges(Object value) {
        Map<String, int[]> ranges = new LinkedHashMap<>();
        if (!(value instanceof Map)) {
            return ranges;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException("TapCoefficientFloat target type must be a string");
            }
            String targetType = (String) entry.getKey();
            if (!TAP_FLOAT.equals(targetType) && !TAP_DOUBLE.equals(targetType)) {
                throw new IllegalArgumentException("Unsupported TapCoefficientFloat target type: " + targetType);
            }
            int[] range = parseRange(entry.getValue());
            if (range == null || range[0] > range[1]) {
                throw new IllegalArgumentException("Invalid TapCoefficientFloat range for " + targetType);
            }
            for (Map.Entry<String, int[]> existing : ranges.entrySet()) {
                if (range[0] <= existing.getValue()[1] && existing.getValue()[0] <= range[1]) {
                    throw new IllegalArgumentException("Overlapping TapCoefficientFloat ranges for "
                            + existing.getKey() + " and " + targetType);
                }
            }
            ranges.put(targetType, range);
        }
        return ranges;
    }

    private int[] parseRange(Object value) {
        if (!(value instanceof List)) {
            return null;
        }
        List<?> values = (List<?>) value;
        if (values.size() != 2 || !(values.get(0) instanceof Number) || !(values.get(1) instanceof Number)) {
            return null;
        }
        return new int[]{((Number) values.get(0)).intValue(), ((Number) values.get(1)).intValue()};
    }
}
