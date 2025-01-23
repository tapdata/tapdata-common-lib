package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.FromTapValueCodec;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.schema.type.TapMoney;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapMoneyValue;

import java.math.BigDecimal;

@Implementation(value = FromTapValueCodec.class, type = TapDefaultCodecs.TAP_MONEY_VALUE, buildNumber = 0)
public class FromTapMoneyCodec implements FromTapValueCodec<TapMoneyValue> {
    @Override
    public Object fromTapValue(TapMoneyValue tapValue) {
        if(tapValue == null)
            return null;
        TapType tapType = tapValue.getTapType();
        TapMoney tapMoney = null;
        if(tapType instanceof TapMoney) {
            tapMoney = (TapMoney) tapType;
        }
        if(tapMoney != null){
            return tapValue.getValue();
        }
        return tapValue.getValue();
    }
}
