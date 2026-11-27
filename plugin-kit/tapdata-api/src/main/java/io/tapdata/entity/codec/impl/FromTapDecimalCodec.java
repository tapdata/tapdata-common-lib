package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.FromTapValueCodec;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.schema.value.TapDecimalValue;

/** Unwrap without rounding; target codecs may override this for their field types. */
@Implementation(value = FromTapValueCodec.class, type = TapDefaultCodecs.TAP_DECIMAL_VALUE, buildNumber = 0)
public class FromTapDecimalCodec implements FromTapValueCodec<TapDecimalValue> {
    @Override
    public Object fromTapValue(TapDecimalValue tapValue) {
        return tapValue == null ? null : tapValue.getValue();
    }
}
