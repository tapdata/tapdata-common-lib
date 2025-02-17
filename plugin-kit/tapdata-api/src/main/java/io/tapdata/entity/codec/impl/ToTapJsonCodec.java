package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapJsonValue;

@Implementation(value = ToTapValueCodec.class, type = TapDefaultCodecs.TAP_JSON_VALUE, buildNumber = 0)
public class ToTapJsonCodec implements ToTapValueCodec<TapJsonValue> {
    @Override
    public TapJsonValue toTapValue(Object value, TapType tapType) {
        return new TapJsonValue(String.valueOf(value));
    }
}
