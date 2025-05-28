package io.tapdata.entity.schema.value;

import io.tapdata.entity.schema.type.TapInputStream;
import io.tapdata.entity.schema.type.TapType;

import java.io.InputStream;

public class TapInputStreamValue extends TapValue<InputStream, TapInputStream> {
    public TapInputStreamValue() {
    }

    public TapInputStreamValue(InputStream value) {
        this.value = value;
    }

    @Override
    public TapType createDefaultTapType() {
        return new TapInputStream();
    }

    @Override
    public Class<? extends TapType> tapTypeClass() {
        return TapInputStream.class;
    }
}
