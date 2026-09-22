package io.tapdata.entity.schema.type;

import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.value.TapDoubleValue;
import io.tapdata.entity.schema.value.TapValue;
import io.tapdata.entity.utils.InstanceFactory;

import java.math.BigDecimal;

/** Binary64 floating-point schema type. */
public class TapDouble extends TapNumber {
    private static final long serialVersionUID = 1L;

    private static final int BIT = 64;
    private static final int STORAGE_BYTES = 8;

    private Integer storageBytes = STORAGE_BYTES;
    private Integer effectivePrecision = 15;
    private Integer binaryPrecision;
    private Boolean supportsNaN;
    private Boolean supportsInfinity;

    public TapDouble() {
        super();
        bit(BIT);
        precision(effectivePrecision);
        fixed(false);
        minValue(BigDecimal.valueOf(-Double.MAX_VALUE));
        maxValue(BigDecimal.valueOf(Double.MAX_VALUE));
    }

    @Override
    public Integer getBit() {
        return super.getBit();
    }

    @Override
    public void setBit(Integer bit) {
        if (bit != null && bit != BIT) {
            throw new IllegalArgumentException("TapDouble bit must be 64");
        }
        super.setBit(bit);
    }

    @Override
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
        Integer previous = this.effectivePrecision;
        this.effectivePrecision = effectivePrecision;
        if (getPrecision() == null || getPrecision().equals(previous)) {
            super.setPrecision(effectivePrecision);
        }
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

    @Override
    public TapDouble precision(Integer precision) {
        super.precision(precision);
        return this;
    }

    @Override
    public TapDouble scale(Integer scale) {
        super.scale(scale);
        return this;
    }

    @Override
    public TapDouble unsigned(Boolean unsigned) {
        super.unsigned(unsigned);
        return this;
    }

    @Override
    public TapDouble zerofill(Boolean zerofill) {
        super.zerofill(zerofill);
        return this;
    }

    @Override
    public TapDouble minValue(BigDecimal minValue) {
        super.minValue(minValue);
        return this;
    }

    @Override
    public TapDouble maxValue(BigDecimal maxValue) {
        super.maxValue(maxValue);
        return this;
    }

    @Override
    public void setFixed(Boolean fixed) {
        if (Boolean.TRUE.equals(fixed)) {
            throw new IllegalArgumentException("TapDouble cannot be fixed-point");
        }
        super.setFixed(fixed);
    }

    @Override
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
    public TapDouble cannotWrite(Boolean cannotWrite) {
        super.cannotWrite(cannotWrite);
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
                .bit(getBit())
                .precision(getPrecision())
                .scale(getScale())
                .unsigned(getUnsigned())
                .zerofill(getZerofill())
                .minValue(getMinValue())
                .maxValue(getMaxValue())
                .fixed(getFixed())
                .cannotWrite(getCannotWrite())
                .storageBytes(storageBytes)
                .effectivePrecision(effectivePrecision)
                .binaryPrecision(binaryPrecision)
                .supportsNaN(supportsNaN)
                .supportsInfinity(supportsInfinity);
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
