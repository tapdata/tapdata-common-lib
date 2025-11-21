package io.tapdata.entity.schema.value;
import io.tapdata.entity.schema.type.TapBinary;
import io.tapdata.entity.schema.type.TapType;

public class TapBinaryValue extends AbsBasicTapValue<ByteData, TapBinary> {
    public TapBinaryValue() {}
    public TapBinaryValue(byte[] value) {
        this.value = new ByteData(value);
    }
    public TapBinaryValue(ByteData byteData) {
        this.value = byteData;
    }

    @Override
    public TapType createDefaultTapType() {
        return new TapBinary().bytes(Long.MAX_VALUE);
    }

    @Override
    public Class<? extends TapType> tapTypeClass() {
        return TapBinary.class;
    }
}
