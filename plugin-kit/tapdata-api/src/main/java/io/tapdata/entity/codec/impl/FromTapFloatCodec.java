package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.FromTapValueCodec;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.schema.value.TapFloatValue;

/** Converts a platform float value to a Java Float at egress. */
@Implementation(value = FromTapValueCodec.class, type = TapDefaultCodecs.TAP_FLOAT_VALUE, buildNumber = 0)
public class FromTapFloatCodec implements FromTapValueCodec<TapFloatValue> {
    @Override
    public Object fromTapValue(TapFloatValue tapValue) {
        return tapValue == null || tapValue.getValue() == null ? null : tapValue.getValue().floatValue();
    }
}
