package io.tapdata.entity.schema.type;

import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.value.TapDoubleValue;
import io.tapdata.entity.schema.value.TapValue;
import io.tapdata.entity.utils.InstanceFactory;

import java.math.BigDecimal;

/** Binary64 floating-point schema type. */
public class TapDouble extends TapType {
    private static final int BIT = 64;
    private static final int STORAGE_BYTES = 8;

    private Integer bit = BIT;
    private Integer storageBytes = STORAGE_BYTES;
    private Integer effectivePrecision = 15;
    private Integer binaryPrecision;
    private BigDecimal minValue = BigDecimal.valueOf(-Double.MAX_VALUE);
    private BigDecimal maxValue = BigDecimal.valueOf(Double.MAX_VALUE);
    private Boolean fixed = false;
    private Boolean supportsNaN;
    private Boolean supportsInfinity;

    public TapDouble() {
        type = TYPE_NUMBER;
    }

    public Integer getBit() {
        return bit;
    }

    public void setBit(Integer bit) {
        if (bit != null && bit != BIT) {
            throw new IllegalArgumentException("TapDouble bit must be 64");
        }
        this.bit = bit;
    }

    public TapDouble bit(Integer bit) {
        setBit(bit);
        return this;
    }

    public Integer getStorageBytes() {
        return storageBytes;
    }

    public void setStorageBytes(Integer storageBytes) {
        if (storageBytes != null && storageBytes != STORAGE_BYTES) {
            throw new IllegalArgumentException("TapDouble storageBytes must be 8");
        }
        this.storageBytes = storageBytes;
    }

    public TapDouble storageBytes(Integer storageBytes) {
        setStorageBytes(storageBytes);
        return this;
    }

    public Integer getEffectivePrecision() {
        return effectivePrecision;
    }

    public void setEffectivePrecision(Integer effectivePrecision) {
        this.effectivePrecision = effectivePrecision;
    }

    public TapDouble effectivePrecision(Integer effectivePrecision) {
        setEffectivePrecision(effectivePrecision);
        return this;
    }

    public Integer getBinaryPrecision() {
        return binaryPrecision;
    }

    public void setBinaryPrecision(Integer binaryPrecision) {
        this.binaryPrecision = binaryPrecision;
    }

    public TapDouble binaryPrecision(Integer binaryPrecision) {
        setBinaryPrecision(binaryPrecision);
        return this;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public TapDouble minValue(BigDecimal minValue) {
        setMinValue(minValue);
        return this;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public TapDouble maxValue(BigDecimal maxValue) {
        setMaxValue(maxValue);
        return this;
    }

    public Boolean getFixed() {
        return fixed;
    }

    public void setFixed(Boolean fixed) {
        if (Boolean.TRUE.equals(fixed)) {
            throw new IllegalArgumentException("TapDouble cannot be fixed-point");
        }
        this.fixed = fixed;
    }

    public TapDouble fixed(Boolean fixed) {
        setFixed(fixed);
        return this;
    }

    public Boolean getSupportsNaN() {
        return supportsNaN;
    }

    public void setSupportsNaN(Boolean supportsNaN) {
        this.supportsNaN = supportsNaN;
    }

    public TapDouble supportsNaN(Boolean supportsNaN) {
        setSupportsNaN(supportsNaN);
        return this;
    }

    public Boolean getSupportsInfinity() {
        return supportsInfinity;
    }

    public void setSupportsInfinity(Boolean supportsInfinity) {
        this.supportsInfinity = supportsInfinity;
    }

    public TapDouble supportsInfinity(Boolean supportsInfinity) {
        setSupportsInfinity(supportsInfinity);
        return this;
    }

    @Override
    public void setType(byte type) {
        if (type != TYPE_NUMBER) {
            throw new IllegalArgumentException("TapDouble type must be TYPE_NUMBER");
        }
        super.setType(type);
    }

    @Override
    public TapType cloneTapType() {
        return new TapDouble()
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
        return TapDoubleValue.class;
    }

    @Override
    public ToTapValueCodec<?> toTapValueCodec() {
        return InstanceFactory.instance(ToTapValueCodec.class, TapDefaultCodecs.TAP_DOUBLE_VALUE);
    }
}
