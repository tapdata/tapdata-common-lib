package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.FromTapValueCodec;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.schema.value.TapDoubleValue;

/** Converts a platform double value to a Java Double at egress. */
@Implementation(value = FromTapValueCodec.class, type = TapDefaultCodecs.TAP_DOUBLE_VALUE, buildNumber = 0)
public class FromTapDoubleCodec implements FromTapValueCodec<TapDoubleValue> {
    @Override
    public Object fromTapValue(TapDoubleValue tapValue) {
        return tapValue == null ? null : tapValue.getValue();
    }
}
