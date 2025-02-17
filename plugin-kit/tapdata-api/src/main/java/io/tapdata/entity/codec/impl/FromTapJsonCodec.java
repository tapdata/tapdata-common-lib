package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.FromTapValueCodec;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.schema.value.TapJsonValue;

@Implementation(value = FromTapValueCodec.class, type = TapDefaultCodecs.TAP_JSON_VALUE, buildNumber = 0)
public class FromTapJsonCodec implements FromTapValueCodec<TapJsonValue> {
    @Override
    public Object fromTapValue(TapJsonValue tapValue) {
        if (tapValue == null)
            return null;
        return tapValue.getValue();
    }
}
