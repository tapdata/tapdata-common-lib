package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapXmlValue;

@Implementation(value = ToTapValueCodec.class, type = TapDefaultCodecs.TAP_XML_VALUE, buildNumber = 0)
public class ToTapXmlCodec implements ToTapValueCodec<TapXmlValue> {
    @Override
    public TapXmlValue toTapValue(Object value, TapType tapType) {
        return new TapXmlValue(String.valueOf(value));
    }
}
