package io.tapdata.entity.schema.type;

import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.value.TapFloatValue;
import io.tapdata.entity.schema.value.TapValue;
import io.tapdata.entity.utils.InstanceFactory;

import java.math.BigDecimal;

/**
 * Binary32 floating-point schema type.
 *
 * <p>The value is exposed as a {@link Double} in the common data model, but
 * is quantized to binary32 at ingress by {@code ToTapFloatCodec}.</p>
 */
public class TapFloat extends TapType {
    private static final int BIT = 32;
    private static final int STORAGE_BYTES = 4;

    private Integer bit = BIT;
    private Integer storageBytes = STORAGE_BYTES;
    private Integer effectivePrecision = 7;
    private Integer binaryPrecision;
    private BigDecimal minValue = BigDecimal.valueOf(-Float.MAX_VALUE);
    private BigDecimal maxValue = BigDecimal.valueOf(Float.MAX_VALUE);
    private Boolean fixed = false;
    private Boolean supportsNaN;
    private Boolean supportsInfinity;

    public TapFloat() {
        type = TYPE_NUMBER;
    }

    public Integer getBit() {
        return bit;
    }

    public void setBit(Integer bit) {
        if (bit != null && bit != BIT) {
            throw new IllegalArgumentException("TapFloat bit must be 32");
        }
        this.bit = bit;
    }

    public TapFloat bit(Integer bit) {
        setBit(bit);
        return this;
    }

    public Integer getStorageBytes() {
        return storageBytes;
    }

    public void setStorageBytes(Integer storageBytes) {
        if (storageBytes != null && storageBytes != STORAGE_BYTES) {
            throw new IllegalArgumentException("TapFloat storageBytes must be 4");
        }
        this.storageBytes = storageBytes;
    }

    public TapFloat storageBytes(Integer storageBytes) {
        setStorageBytes(storageBytes);
        return this;
    }

    public Integer getEffectivePrecision() {
        return effectivePrecision;
    }

    public void setEffectivePrecision(Integer effectivePrecision) {
        this.effectivePrecision = effectivePrecision;
    }

    public TapFloat effectivePrecision(Integer effectivePrecision) {
        setEffectivePrecision(effectivePrecision);
        return this;
    }

    public Integer getBinaryPrecision() {
        return binaryPrecision;
    }

    public void setBinaryPrecision(Integer binaryPrecision) {
        this.binaryPrecision = binaryPrecision;
    }

    public TapFloat binaryPrecision(Integer binaryPrecision) {
        setBinaryPrecision(binaryPrecision);
        return this;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public TapFloat minValue(BigDecimal minValue) {
        setMinValue(minValue);
        return this;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public TapFloat maxValue(BigDecimal maxValue) {
        setMaxValue(maxValue);
        return this;
    }

    public Boolean getFixed() {
        return fixed;
    }

    public void setFixed(Boolean fixed) {
        if (Boolean.TRUE.equals(fixed)) {
            throw new IllegalArgumentException("TapFloat cannot be fixed-point");
        }
        this.fixed = fixed;
    }

    public TapFloat fixed(Boolean fixed) {
        setFixed(fixed);
        return this;
    }

    public Boolean getSupportsNaN() {
        return supportsNaN;
    }

    public void setSupportsNaN(Boolean supportsNaN) {
        this.supportsNaN = supportsNaN;
    }

    public TapFloat supportsNaN(Boolean supportsNaN) {
        setSupportsNaN(supportsNaN);
        return this;
    }

    public Boolean getSupportsInfinity() {
        return supportsInfinity;
    }

    public void setSupportsInfinity(Boolean supportsInfinity) {
        this.supportsInfinity = supportsInfinity;
    }

    public TapFloat supportsInfinity(Boolean supportsInfinity) {
        setSupportsInfinity(supportsInfinity);
        return this;
    }

    @Override
    public void setType(byte type) {
        if (type != TYPE_NUMBER) {
            throw new IllegalArgumentException("TapFloat type must be TYPE_NUMBER");
        }
        super.setType(type);
    }

    @Override
    public TapType cloneTapType() {
        return new TapFloat()
                .bit(bit)
                .storageBytes(storageBytes)
                .effectivePrecision(effectivePrecision)
                .binaryPrecision(binaryPrecision)
                .minValue(minValue)
                .maxValue(maxValue)
                .fixed(fixed)
                .supportsNaN(supportsNaN)
                .supportsInfinity(supportsInfinity)
                .cannotWrite(cannotWrite);
    }

    @Override
    public Class<? extends TapValue<?, ?>> tapValueClass() {
        return TapFloatValue.class;
    }

    @Override
    public ToTapValueCodec<?> toTapValueCodec() {
        return InstanceFactory.instance(ToTapValueCodec.class, TapDefaultCodecs.TAP_FLOAT_VALUE);
    }
}
