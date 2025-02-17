package io.tapdata.entity.schema.type;

import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.value.TapJsonValue;
import io.tapdata.entity.schema.value.TapValue;
import io.tapdata.entity.utils.InstanceFactory;

import static io.tapdata.entity.simplify.TapSimplify.tapJson;

public class TapJson extends TapType {
    public TapJson() {
        type = TYPE_JSON;
    }

    private String json;

    public TapJson json(String json) {
        this.json = json;
        return this;
    }

    public String getJson() {
        return json;
    }

    @Override
    public TapType cloneTapType() {
        return tapJson().json(json);
    }

    @Override
    public Class<? extends TapValue<?, ?>> tapValueClass() {
        return TapJsonValue.class;
    }

    @Override
    public ToTapValueCodec<?> toTapValueCodec() {
        return InstanceFactory.instance(ToTapValueCodec.class, TapDefaultCodecs.TAP_JSON_VALUE);
    }
}
