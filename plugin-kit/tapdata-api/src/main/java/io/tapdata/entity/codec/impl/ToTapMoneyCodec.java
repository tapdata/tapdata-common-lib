package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapMoneyValue;

@Implementation(value = ToTapValueCodec.class, type = TapDefaultCodecs.TAP_MONEY_VALUE, buildNumber = 0)
public class ToTapMoneyCodec implements ToTapValueCodec<TapMoneyValue> {
    @Override
    public TapMoneyValue toTapValue(Object value, TapType tapType) {
        return new TapMoneyValue(Double.valueOf(String.valueOf(value)));
    }
}
