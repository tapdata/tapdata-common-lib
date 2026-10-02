package io.tapdata.entity.schema.type;

import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.value.TapNumberValue;
import io.tapdata.entity.schema.value.TapValue;
import io.tapdata.entity.utils.InstanceFactory;

/**
 * A floating-point declaration whose concrete precision is selected by a
 * mapping rule.  It is an intermediate schema type; mappings should resolve
 * it to {@link TapFloat} or {@link TapDouble} before it is used for values.
 */
public class TapCoefficientFloat extends TapNumber {
    private static final long serialVersionUID = 1L;

    @Override
    public TapType cloneTapType() {
        return new TapCoefficientFloat()
                .bit(getBit())
                .precision(getPrecision())
                .scale(getScale())
                .unsigned(getUnsigned())
                .zerofill(getZerofill())
                .minValue(getMinValue())
                .maxValue(getMaxValue())
                .fixed(getFixed())
                .cannotWrite(getCannotWrite());
    }

    @Override
    public Class<? extends TapValue<?, ?>> tapValueClass() {
        return TapNumberValue.class;
    }

    @Override
    public ToTapValueCodec<?> toTapValueCodec() {
        return InstanceFactory.instance(ToTapValueCodec.class, TapDefaultCodecs.TAP_NUMBER_VALUE);
    }
}
