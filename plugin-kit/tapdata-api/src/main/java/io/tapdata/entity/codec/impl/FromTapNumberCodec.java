package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.FromTapValueCodec;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.value.TapNumberValue;

import java.math.BigDecimal;

@Implementation(value = FromTapValueCodec.class, type = TapDefaultCodecs.TAP_NUMBER_VALUE, buildNumber = 0)
public class FromTapNumberCodec implements FromTapValueCodec<TapNumberValue> {
    @Override
    public Object fromTapValue(TapNumberValue tapValue) {
        if (tapValue == null || tapValue.getNumberValue() == null)
            return null;
        Number value = tapValue.getNumberValue();
        TapNumber type = tapValue.getTapType();
        if (type == null)
            return value;

        boolean floating = !Boolean.TRUE.equals(type.getFixed()) && type.getBit() != null
                && (type.getBit() == 32 || type.getBit() == 64);
        boolean nonFinite = (value instanceof Double || value instanceof Float)
                && !Double.isFinite(value.doubleValue());
        if (nonFinite) {
            if (floating) {
                if (type.getBit() == 32) return value.floatValue();
                return value.doubleValue();
            }
            if (type.getScale() == null && !Boolean.TRUE.equals(type.getFixed())
                    && type.getMinValue() == null && type.getMaxValue() == null)
                return value;
            throw new ArithmeticException("Non-finite number cannot be converted to a constrained decimal or integer");
        }
        if (!floating && type.getScale() == null && !Boolean.TRUE.equals(type.getFixed())
                && type.getMinValue() == null && type.getMaxValue() == null && !Boolean.TRUE.equals(type.getUnsigned()))
            return value;
        BigDecimal decimal = toDecimal(value);
        if (Boolean.TRUE.equals(type.getUnsigned()) && decimal.signum() < 0)
            throw new ArithmeticException("Negative number for unsigned target: " + value);
        if (type.getMinValue() != null && decimal.compareTo(type.getMinValue()) < 0
                || type.getMaxValue() != null && decimal.compareTo(type.getMaxValue()) > 0)
            throw new ArithmeticException("Number outside target bounds: " + value);
        if (floating) {
            if (type.getBit() == 32) {
                float result = value.floatValue();
                if (!Float.isFinite(result)) throw new ArithmeticException("Float overflow: " + value);
                return result;
            }
            double result = value.doubleValue();
            if (!Double.isFinite(result)) throw new ArithmeticException("Double overflow: " + value);
            return result;
        }
        Integer scale = type.getScale();
        if (scale == null && !Boolean.TRUE.equals(type.getFixed()))
            return value;
        // No implicit rounding: callers must supply an explicit rounding policy upstream.
        if (scale != null) decimal = decimal.setScale(scale, java.math.RoundingMode.UNNECESSARY);
        if (type.getPrecision() != null && decimal.precision() > type.getPrecision())
            throw new ArithmeticException("Number exceeds target precision: " + value);
        if (scale != null && scale == 0 && type.getMinValue() != null && type.getMaxValue() != null) {
            BigDecimal min = type.getMinValue();
            BigDecimal max = type.getMaxValue();
            if (min.compareTo(BigDecimal.valueOf(Byte.MIN_VALUE)) >= 0 && max.compareTo(BigDecimal.valueOf(Byte.MAX_VALUE)) <= 0)
                return decimal.byteValueExact();
            if (min.compareTo(BigDecimal.valueOf(Short.MIN_VALUE)) >= 0 && max.compareTo(BigDecimal.valueOf(Short.MAX_VALUE)) <= 0)
                return decimal.shortValueExact();
            if (min.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) >= 0 && max.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0)
                return decimal.intValueExact();
            if (min.compareTo(BigDecimal.valueOf(Long.MIN_VALUE)) >= 0 && max.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) <= 0)
                return decimal.longValueExact();
        }
        return decimal;
    }

    private BigDecimal toDecimal(Number value) {
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof java.math.BigInteger) return new BigDecimal((java.math.BigInteger) value);
        // String conversion avoids passing integral values through doubleValue().
        return new BigDecimal(value.toString());
    }
}
