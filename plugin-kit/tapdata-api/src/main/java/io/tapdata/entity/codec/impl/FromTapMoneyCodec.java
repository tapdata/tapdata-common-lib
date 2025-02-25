package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.FromTapValueCodec;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.schema.value.TapMoneyValue;

@Implementation(value = FromTapValueCodec.class, type = TapDefaultCodecs.TAP_MONEY_VALUE, buildNumber = 0)
public class FromTapMoneyCodec implements FromTapValueCodec<TapMoneyValue> {
    @Override
    public Object fromTapValue(TapMoneyValue tapValue) {
        if (tapValue == null)
            return null;
        return tapValue.getValue();
    }
}
