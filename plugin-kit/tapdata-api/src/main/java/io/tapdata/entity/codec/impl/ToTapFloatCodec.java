package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapFloatValue;

/** Converts inbound values to binary32 semantics carried by a Double. */
@Implementation(value = ToTapValueCodec.class, type = TapDefaultCodecs.TAP_FLOAT_VALUE, buildNumber = 0)
public class ToTapFloatCodec implements ToTapValueCodec<TapFloatValue> {
    @Override
    public TapFloatValue toTapValue(Object value, TapType typeFromSchema) {
        if (value == null) {
            return null;
        }

        Float converted;
        boolean sourceSpecial;
        if (value instanceof Number) {
            Number number = (Number) value;
            double source = number.doubleValue();
            sourceSpecial = Double.isNaN(source) || Double.isInfinite(source);
            converted = number.floatValue();
            if (!sourceSpecial && converted.isInfinite()) {
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
            if (!sourceSpecial && converted.isInfinite()) {
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
        return new TapFloatValue((double) converted);
    }

    private boolean allowsSpecial(Float value, TapType typeFromSchema) {
        if (!value.isNaN() && !value.isInfinite()) {
            return true;
        }
        if (typeFromSchema instanceof TapFloat) {
            TapFloat type = (TapFloat) typeFromSchema;
            if (value.isNaN() && Boolean.FALSE.equals(type.getSupportsNaN())) {
                return false;
            }
            if (value.isInfinite() && Boolean.FALSE.equals(type.getSupportsInfinity())) {
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
