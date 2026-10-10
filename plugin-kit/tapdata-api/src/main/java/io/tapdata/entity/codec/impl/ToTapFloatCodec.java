package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapRawValueCodec;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapFloatValue;

/** Converts inbound values to binary32 semantics carried by a Double. */
@Implementation(value = ToTapValueCodec.class, type = TapDefaultCodecs.TAP_FLOAT_VALUE, buildNumber = 0)
public class ToTapFloatCodec implements ToTapValueCodec<TapFloatValue>, ToTapRawValueCodec {
    @Override
    public TapFloatValue toTapValue(Object value, TapType typeFromSchema) {
        Double converted = convert(value, typeFromSchema);
        return converted == null ? null : new TapFloatValue(converted);
    }

    @Override
    public Double toRawValue(Object value, TapType typeFromSchema) {
        return convert(value, typeFromSchema);
    }

    private Double convert(Object value, TapType typeFromSchema) {
        if (value == null) {
            return null;
        }

        double converted;
        boolean sourceSpecial;
        if (value instanceof Number) {
            Number number = (Number) value;
            double source = number.doubleValue();
            sourceSpecial = (number instanceof Float || number instanceof Double)
                    && (Double.isNaN(source) || Double.isInfinite(source));
            converted = number.floatValue();
            if (!sourceSpecial && Double.isInfinite(converted)) {
                return null;
            }
        } else if (value instanceof CharSequence) {
            String text = value.toString().trim();
            try {
                converted = Float.parseFloat(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
            sourceSpecial = isSpecialLiteral(text);
            if (!sourceSpecial && Double.isInfinite(converted)) {
                return null;
            }
        } else if (value instanceof Boolean) {
            converted = (Boolean) value ? 1.0f : 0.0f;
            sourceSpecial = false;
        } else {
            return null;
        }

        if (!allowsSpecial(converted, typeFromSchema)) {
            return null;
        }
        return converted;
    }

    private boolean allowsSpecial(double value, TapType typeFromSchema) {
        if (!Double.isNaN(value) && !Double.isInfinite(value)) {
            return true;
        }
        if (typeFromSchema instanceof TapFloat) {
            TapFloat type = (TapFloat) typeFromSchema;
            if (Double.isNaN(value) && Boolean.FALSE.equals(type.getSupportsNaN())) {
                return false;
            }
            if (Double.isInfinite(value) && Boolean.FALSE.equals(type.getSupportsInfinity())) {
                return false;
            }
        }
        return true;
    }

    private boolean isSpecialLiteral(String text) {
        String normalized = text.toLowerCase();
        return "nan".equals(normalized) || "infinity".equals(normalized) || "+infinity".equals(normalized)
                || "-infinity".equals(normalized);
    }
}
