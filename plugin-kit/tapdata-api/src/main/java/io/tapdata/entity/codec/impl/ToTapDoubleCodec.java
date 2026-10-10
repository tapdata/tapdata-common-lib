package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapRawValueCodec;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapDoubleValue;

/** Converts inbound values to binary64 semantics carried by a Double. */
@Implementation(value = ToTapValueCodec.class, type = TapDefaultCodecs.TAP_DOUBLE_VALUE, buildNumber = 0)
public class ToTapDoubleCodec implements ToTapValueCodec<TapDoubleValue>, ToTapRawValueCodec {
    @Override
    public TapDoubleValue toTapValue(Object value, TapType typeFromSchema) {
        Double converted = convert(value, typeFromSchema);
        return converted == null ? null : new TapDoubleValue(converted);
    }

    @Override
    public Double toRawValue(Object value, TapType typeFromSchema) {
        return convert(value, typeFromSchema);
    }

    private Double convert(Object value, TapType typeFromSchema) {
        if (value == null) {
            return null;
        }

        Double converted;
        boolean sourceSpecial;
        if (value instanceof Number) {
            Number number = (Number) value;
            double source = number.doubleValue();
            sourceSpecial = (number instanceof Float || number instanceof Double)
                    && (Double.isNaN(source) || Double.isInfinite(source));
            converted = source;
            if (!sourceSpecial && converted.isInfinite()) {
                return null;
            }
        } else if (value instanceof CharSequence) {
            String text = value.toString().trim();
            try {
                converted = Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
            sourceSpecial = isSpecialLiteral(text);
            if (!sourceSpecial && converted.isInfinite()) {
                return null;
            }
        } else if (value instanceof Boolean) {
            converted = (Boolean) value ? 1.0d : 0.0d;
            sourceSpecial = false;
        } else {
            return null;
        }

        if ((converted.isNaN() && typeFromSchema instanceof TapDouble && Boolean.FALSE.equals(((TapDouble) typeFromSchema).getSupportsNaN()))
                || (converted.isInfinite() && typeFromSchema instanceof TapDouble && Boolean.FALSE.equals(((TapDouble) typeFromSchema).getSupportsInfinity()))) {
            return null;
        }
        return converted;
    }

    private boolean isSpecialLiteral(String text) {
        String normalized = text.toLowerCase();
        return "nan".equals(normalized) || "infinity".equals(normalized) || "+infinity".equals(normalized)
                || "-infinity".equals(normalized);
    }
}
