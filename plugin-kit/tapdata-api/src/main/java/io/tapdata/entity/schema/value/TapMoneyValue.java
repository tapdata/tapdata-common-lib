package io.tapdata.entity.schema.value;

import io.tapdata.entity.schema.type.TapMoney;
import io.tapdata.entity.schema.type.TapType;

public class TapMoneyValue extends TapValue<Double, TapMoney>{
    public TapMoneyValue() {
    }
    public TapMoneyValue(Double value) {
        this.value = value;
    }
    @Override
    public TapType createDefaultTapType() {
        return new TapMoney();
    }

    @Override
    public Class<? extends TapType> tapTypeClass() {
        return TapMoney.class;
    }
}
