package io.tapdata.entity.schema.value;

import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.type.TapXml;

public class TapXmlValue extends AbsBasicTapValue<String, TapXml> {
    public TapXmlValue() {
    }

    public TapXmlValue(String value) {
        this.value = value;
    }

    @Override
    public TapType createDefaultTapType() {
        return new TapXml();
    }

    @Override
    public Class<? extends TapType> tapTypeClass() {
        return TapXml.class;
    }
}
