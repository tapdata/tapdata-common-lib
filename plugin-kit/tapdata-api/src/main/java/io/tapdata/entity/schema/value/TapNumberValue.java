package io.tapdata.entity.schema.value;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapType;

import java.math.BigDecimal;


/**
 * Double-compatible numeric wrapper. Exact-number consumers must use getNumberValue().
 * V2 object serialization preserves the numeric representation; Java serialization
 * requires matching library versions.
 */
public class TapNumberValue extends AbsBasicTapValue<Double, TapNumber> {
    private static final long serialVersionUID = 1L;
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
            this.value = null;
            this.numberValue = value;
        }
    }

    /** Legacy API: callers handling exact numbers must use getNumberValue(). */
    @Override
    public Double getValue() {
        if (numberValue != null) {
            throw new IllegalStateException("Use getNumberValue() for " + numberValue.getClass().getName());
        }
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

    // TapValue is not Serializable, so its fields must be written explicitly.
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
        out.writeObject(value);
        out.writeObject(getTapType());
        out.writeObject(getOriginValue());
        out.writeObject(getOriginType());
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        value = (Double) in.readObject();
        setTapType((TapNumber) in.readObject());
        setOriginValue(in.readObject());
        setOriginType((String) in.readObject());
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
