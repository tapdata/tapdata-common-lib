package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapInputStreamValue;

import java.io.InputStream;

@Implementation(value = ToTapValueCodec.class, type = TapDefaultCodecs.TAP_INPUT_STREAM_VALUE, buildNumber = 0)
public class ToTapInputStreamCodec implements ToTapValueCodec<TapInputStreamValue> {
    @Override
    public TapInputStreamValue toTapValue(Object value, TapType tapType) {
        return new TapInputStreamValue((InputStream) (value));
    }
}
