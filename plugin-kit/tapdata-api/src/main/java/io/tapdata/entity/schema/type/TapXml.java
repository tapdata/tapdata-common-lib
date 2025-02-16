package io.tapdata.entity.schema.type;

import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.value.TapValue;
import io.tapdata.entity.schema.value.TapXmlValue;
import io.tapdata.entity.utils.InstanceFactory;

import static io.tapdata.entity.simplify.TapSimplify.tapXml;

public class TapXml extends TapType {
    public TapXml() {
        type = TYPE_XML;
    }

    private String xml;

    public TapXml xml(String xml) {
        this.xml = xml;
        return this;
    }

    public String getXml() {
        return xml;
    }

    @Override
    public TapType cloneTapType() {
        return tapXml().xml(xml);
    }

    @Override
    public Class<? extends TapValue<?, ?>> tapValueClass() {
        return TapXmlValue.class;
    }

    @Override
    public ToTapValueCodec<?> toTapValueCodec() {
        return InstanceFactory.instance(ToTapValueCodec.class, TapDefaultCodecs.TAP_XML_VALUE);
    }
}
