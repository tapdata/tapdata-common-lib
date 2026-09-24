package io.tapdata.entity.mapping.type;

import io.tapdata.entity.result.TapResult;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Resolver for source declarations such as float($precision). */
public class TapFloatingPointMapping extends TapMapping {
    private int minBinaryPrecision = 1;
    private int maxBinaryPrecision = 53;
    private int defaultBinaryPrecision = 53;
    private int singlePrecisionMax = 24;
    private int doublePrecisionMin = 25;

    @Override
    public void from(Map<String, Object> info) {
        if (info == null) {
            return;
        }
        int[] range = range(info.get("binaryPrecision"));
        if (range != null) {
            minBinaryPrecision = range[0];
            maxBinaryPrecision = range[1];
        }
        Integer defaultPrecision = FloatingPointMappingSupport.integer(info.get("defaultBinaryPrecision"));
        if (defaultPrecision != null) {
            defaultBinaryPrecision = defaultPrecision;
        }
        int[] singleRange = ruleRange(info.get("singlePrecision"));
        if (singleRange != null) {
            singlePrecisionMax = singleRange[1];
        }
        int[] doubleRange = ruleRange(info.get("doublePrecision"));
        if (doubleRange != null) {
            doublePrecisionMin = doubleRange[0];
        }
    }

    @Override
    public TapType toTapType(String dataType, Map<String, String> params) {
        int precision = defaultBinaryPrecision;
        String precisionValue = getParam(params, "precision");
        if (precisionValue == null) {
            precisionValue = getParam(params, "binaryPrecision");
        }
        if (precisionValue != null && !precisionValue.trim().isEmpty()) {
            precision = Integer.parseInt(precisionValue.trim());
        }
        if (precision < minBinaryPrecision || precision > maxBinaryPrecision) {
            throw new IllegalArgumentException("Floating point binary precision out of range: " + precision);
        }
        if (precision <= singlePrecisionMax) {
            return new TapFloat().binaryPrecision(precision);
        }
        if (precision >= doublePrecisionMin) {
            return new TapDouble().binaryPrecision(precision);
        }
        throw new IllegalArgumentException("Floating point binary precision has no matching type: " + precision);
    }

    @Override
    public TapResult<String> fromTapType(String typeExpression, TapType tapType) {
        if (!(tapType instanceof TapFloat) && !(tapType instanceof TapDouble)) {
            return null;
        }
        Integer precision = tapType instanceof TapFloat ? ((TapFloat) tapType).getBinaryPrecision() : ((TapDouble) tapType).getBinaryPrecision();
        String expression = typeExpression;
        if (precision != null) {
            expression = expression.replace("$precision", String.valueOf(precision));
        }
        expression = removeBracketVariables(expression, 0);
        return new TapResult<String>().result(TapResult.RESULT_SUCCESSFULLY).data(expression);
    }

    @Override
    public BigDecimal matchingScore(TapField field) {
        return field != null && (field.getTapType() instanceof TapFloat || field.getTapType() instanceof TapDouble)
                ? TapMapping.MAX_SCORE : TapMapping.MIN_SCORE;
    }

    private int[] ruleRange(Object rule) {
        if (!(rule instanceof Map)) {
            return null;
        }
        return range(((Map<?, ?>) rule).get("range"));
    }

    private int[] range(Object value) {
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
