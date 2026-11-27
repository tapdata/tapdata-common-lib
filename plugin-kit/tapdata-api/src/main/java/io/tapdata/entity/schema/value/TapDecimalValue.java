package io.tapdata.entity.schema.value;

import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapType;

import java.math.BigDecimal;

/** A decimal whose digits are preserved across connector and inspection boundaries. */
public class TapDecimalValue extends AbsBasicTapValue<BigDecimal, TapNumber> {
    private static final long serialVersionUID = 1L;

    public TapDecimalValue() {
    }

    public TapDecimalValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public TapType createDefaultTapType() {
        TapNumber type = new TapNumber().fixed(true);
        if (value != null) {
            type.precision(value.precision()).scale(value.scale());
        }
        return type;
    }

    @Override
    public Class<? extends TapType> tapTypeClass() {
        return TapNumber.class;
    }
}
