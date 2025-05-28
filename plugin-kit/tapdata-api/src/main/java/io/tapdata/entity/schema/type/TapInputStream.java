package io.tapdata.entity.schema.type;

import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.value.TapInputStreamValue;
import io.tapdata.entity.schema.value.TapValue;
import io.tapdata.entity.utils.InstanceFactory;

import java.io.InputStream;

import static io.tapdata.entity.simplify.TapSimplify.tapInputStream;

public class TapInputStream extends TapType {
    public TapInputStream() {
        type = TYPE_INPUT_STREAM;
    }

    private InputStream inputStream;

    public TapInputStream inputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public TapType cloneTapType() {
        return tapInputStream().inputStream(inputStream);
    }

    @Override
    public Class<? extends TapValue<?, ?>> tapValueClass() {
        return TapInputStreamValue.class;
    }

    @Override
    public ToTapValueCodec<?> toTapValueCodec() {
        return InstanceFactory.instance(ToTapValueCodec.class, TapDefaultCodecs.TAP_INPUT_STREAM_VALUE);
    }
}
