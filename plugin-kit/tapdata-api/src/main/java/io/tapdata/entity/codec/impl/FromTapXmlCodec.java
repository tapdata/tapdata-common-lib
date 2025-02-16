package io.tapdata.entity.codec.impl;

import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.codec.FromTapValueCodec;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.schema.value.TapXmlValue;

@Implementation(value = FromTapValueCodec.class, type = TapDefaultCodecs.TAP_XML_VALUE, buildNumber = 0)
public class FromTapXmlCodec implements FromTapValueCodec<TapXmlValue> {
    @Override
    public Object fromTapValue(TapXmlValue tapValue) {
        if (tapValue == null)
            return null;
        return tapValue.getValue();
    }
}
