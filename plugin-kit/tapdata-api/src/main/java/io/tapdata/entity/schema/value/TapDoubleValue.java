package io.tapdata.entity.schema.value;

import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapType;

/** Binary64 value carried as a {@link Double}. */
public class TapDoubleValue extends AbsBasicTapValue<Double, TapDouble> {
    public TapDoubleValue() {
    }

    public TapDoubleValue(Double value) {
        this.value = value;
    }

    @Override
    public TapType createDefaultTapType() {
        return new TapDouble();
    }

    @Override
    public Class<? extends TapType> tapTypeClass() {
        return TapDouble.class;
    }
}
