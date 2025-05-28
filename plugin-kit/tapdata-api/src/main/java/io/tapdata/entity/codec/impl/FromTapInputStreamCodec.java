package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.FromTapValueCodec;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.schema.value.TapInputStreamValue;

@Implementation(value = FromTapValueCodec.class, type = TapDefaultCodecs.TAP_INPUT_STREAM_VALUE, buildNumber = 0)
public class FromTapInputStreamCodec implements FromTapValueCodec<TapInputStreamValue> {
    @Override
    public Object fromTapValue(TapInputStreamValue tapValue) {
        if (tapValue == null)
            return null;
        return tapValue.getValue();
    }
}
