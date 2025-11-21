package io.tapdata.entity.schema.value;

import io.tapdata.entity.schema.type.TapJson;
import io.tapdata.entity.schema.type.TapType;

public class TapJsonValue extends AbsBasicTapValue<String, TapJson> {
    public TapJsonValue() {
    }

    public TapJsonValue(String value) {
        this.value = value;
    }

    @Override
    public TapType createDefaultTapType() {
        return new TapJson();
    }

    @Override
    public Class<? extends TapType> tapTypeClass() {
        return TapJson.class;
    }
}
