package io.tapdata.entity.schema.value;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapType;

import java.math.BigDecimal;


/**
 * Double-compatible numeric wrapper. Exact-number consumers must use getNumberValue().
 * Mutable and not thread safe. Use a separate instance per record.
 * V2 serialization preserves payload and metadata; native Java serialization retains
 * the historical limitation that non-Serializable TapValue fields are omitted.
 */
public class TapNumberValue extends AbsBasicTapValue<Double, TapNumber> {
    // Preserve the UID of the original class, before explicit serialization support.
    private static final long serialVersionUID = 7726157022888528681L;
    // Exactly one slot is active; all writes must go through the setters.
    // The inherited Double slot remains the sole storage for legacy values.
    private Number numberValue;

    public TapNumberValue() {}
    public TapNumberValue(Double value) {
        this.value = value;
    }

    /** Creates a value without converting its numeric representation to Double. */
    public static TapNumberValue ofNumber(Number value) {
        TapNumberValue result = new TapNumberValue();
        result.setNumberValue(value);
        return result;
    }

    public Number getNumberValue() {
        return numberValue != null ? numberValue : value;
    }

    public void setNumberValue(Number value) {
        if (value == null || value instanceof Double) {
            setValue((Double) value);
        } else {
            // Reject unsupported mutable/custom Number implementations at the API boundary,
            // rather than failing later during event serialization.
            if (!(value instanceof BigDecimal || value instanceof java.math.BigInteger
                    || value instanceof Long || value instanceof Integer || value instanceof Short
                    || value instanceof Byte || value instanceof Float)) {
                throw new IllegalArgumentException("Unsupported numeric representation: " + value.getClass().getName());
            }
            this.value = null;
            this.numberValue = value;
        }
    }

    /**
     * Legacy Double view. May round or overflow for exact payloads, as the old codec did.
     * New consumers must use getNumberValue() to preserve the original representation.
     */
    @Override
    public Double getValue() {
        if (numberValue != null) return numberValue.doubleValue();
        return value;
    }

    @Override
    public void setValue(Double value) {
        this.numberValue = null;
        this.value = value;
    }

    @Override
    public TapValue<Double, TapNumber> value(Double value) {
        setValue(value);
        return this;
    }

    @Override
    public String toString() {
        return "TapNumberValue{numberValue=" + getNumberValue() + ", tapType=" + getTapType() + '}';
    }

    @Override
    public TapType createDefaultTapType() {
        if (numberValue != null) {
            return new TapNumber();
        }
        return new TapNumber().maxValue(BigDecimal.valueOf(Double.MAX_VALUE)).minValue(BigDecimal.valueOf(-Double.MAX_VALUE));
    }

    @Override
    public Class<? extends TapType> tapTypeClass() {
        return TapNumber.class;
    }
}
