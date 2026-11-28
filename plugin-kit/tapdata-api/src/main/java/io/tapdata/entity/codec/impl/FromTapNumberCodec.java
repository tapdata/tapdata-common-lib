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
        // Keep historical target conversion for existing Double payloads. Compatible rounding and
        // target validation apply only to the new exact-number API.
        if (value instanceof Double) return fromLegacyDouble(tapValue);
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
        if (floating) {
            checkBounds(decimal, type, value);
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
        // Match historical integer truncation; decimal scale reductions use HALF_UP.
        // Validate the converted value: rounding can carry into an overflowing digit.
        if (scale != null) decimal = decimal.setScale(scale, scale == 0
                ? java.math.RoundingMode.DOWN : java.math.RoundingMode.HALF_UP);
        checkBounds(decimal, type, value);
        if (scale == null && !Boolean.TRUE.equals(type.getFixed()))
            return value;
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

    private void checkBounds(BigDecimal decimal, TapNumber type, Number value) {
        if (Boolean.TRUE.equals(type.getUnsigned()) && decimal.signum() < 0)
            throw new ArithmeticException("Negative number for unsigned target: " + value);
        if (type.getMinValue() != null && decimal.compareTo(type.getMinValue()) < 0
                || type.getMaxValue() != null && decimal.compareTo(type.getMaxValue()) > 0)
            throw new ArithmeticException("Number outside target bounds: " + value);
    }

    private Object fromLegacyDouble(TapNumberValue tapValue) {
        if(tapValue == null)
            return null;
        io.tapdata.entity.schema.type.TapType tapType = tapValue.getTapType();
        TapNumber tapNumber = null;
        if(tapType instanceof TapNumber) {
            tapNumber = (TapNumber) tapType;
        }
        if(tapNumber != null) {
            Integer scale = tapNumber.getScale();
            if(scale == null || scale == 0) {
                if (tapNumber.getMaxValue() == null || tapNumber.getMinValue() == null)
                    return tapValue.getValue();
                if(tapNumber.getMaxValue().compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) <= 0 && tapNumber.getMinValue().compareTo(BigDecimal.valueOf(Long.MIN_VALUE)) >= 0) {
                    long maxValue = tapNumber.getMaxValue().longValue();
                    long minValue = tapNumber.getMinValue().longValue();
                    if(Byte.MAX_VALUE >= maxValue && Byte.MIN_VALUE <= minValue) {
                        return tapValue.getValue().byteValue();
                    } else if(Short.MAX_VALUE >= maxValue && Short.MIN_VALUE <= minValue) {
                        return tapValue.getValue().shortValue();
                    } else if(Integer.MAX_VALUE >= maxValue && Integer.MIN_VALUE <= minValue) {
                        return tapValue.getValue().intValue();
                    } else {
                        return tapValue.getValue().longValue();
                    }
                } else {
                    return BigDecimal.valueOf(tapValue.getValue());
                }
            } else {
                if((tapNumber.getBit() != null && tapNumber.getBit() == 32) && (tapNumber.getFixed() == null || !tapNumber.getFixed())) {
                    BigDecimal minValue = tapNumber.getMinValue();
                    BigDecimal maxValue = tapNumber.getMaxValue();
                    if(minValue != null && maxValue != null &&
                            minValue.compareTo(BigDecimal.valueOf(-Float.MAX_VALUE)) >= 0 &&
                            BigDecimal.valueOf(Float.MAX_VALUE).compareTo(maxValue) >= 0) {
                        Double d = tapValue.getValue();
                        if(d != null)
                            return d.floatValue();
                    }

                }
                return tapValue.getValue();
            }
        }

        return tapValue.getValue();
    }


    private BigDecimal toDecimal(Number value) {
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof java.math.BigInteger) return new BigDecimal((java.math.BigInteger) value);
        // Preserve integral precision; Float uses its shortest decimal representation,
        // rather than expanding its binary approximation via doubleValue().
        return new BigDecimal(value.toString());
    }
}
