package io.tapdata.entity.schema.value;

import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapType;

/** Binary32 value carried as a widened {@link Double}. */
public class TapFloatValue extends AbsBasicTapValue<Double, TapFloat> {
    public TapFloatValue() {
    }

    public TapFloatValue(Double value) {
        setValue(value);
    }

    @Override
    public TapFloatValue value(Double value) {
        setValue(value);
        return this;
    }

    @Override
    public void setValue(Double value) {
        this.value = value == null ? null : (double) value.floatValue();
    }

    @Override
    public TapType createDefaultTapType() {
        return new TapFloat();
    }

    @Override
    public Class<? extends TapType> tapTypeClass() {
        return TapFloat.class;
    }
}
