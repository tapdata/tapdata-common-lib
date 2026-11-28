package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.logger.TapLogger;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapNumberValue;

import java.math.BigDecimal;

@Implementation(value = ToTapValueCodec.class, type = TapDefaultCodecs.TAP_NUMBER_VALUE, buildNumber = 0)
public class ToTapNumberCodec implements ToTapValueCodec<TapNumberValue> {
    private static final String TAG = ToTapNumberCodec.class.getSimpleName();

    @Override
    public TapNumberValue toTapValue(Object value, TapType typeFromSchema) {

        TapNumberValue numberValue = null;
        if(value instanceof Number) {
            numberValue = TapNumberValue.ofNumber((Number) value);
        } else if(value instanceof String) {
            try {
                String text = ((String) value).trim();
                if ("NaN".equals(text) || "Infinity".equals(text) || "+Infinity".equals(text) || "-Infinity".equals(text)) {
                    numberValue = new TapNumberValue(Double.parseDouble(text));
                } else {
                    numberValue = TapNumberValue.ofNumber(new BigDecimal(text));
                }
            } catch(NumberFormatException throwable) {
                TapLogger.error(TAG, "Parse string {} to number failed, {}, will mapping other codecs", value, throwable.getMessage());
            }
        } else if(value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            numberValue = new TapNumberValue(bool ? 1d : 0d);
        }
        return numberValue;
    }
}
