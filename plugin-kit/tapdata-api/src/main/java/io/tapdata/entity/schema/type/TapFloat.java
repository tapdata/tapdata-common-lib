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
public class TapFloat extends TapNumber {
    private static final long serialVersionUID = 1L;

    private static final int BIT = 32;
    private static final int STORAGE_BYTES = 4;

    private Integer storageBytes = STORAGE_BYTES;
    private Integer effectivePrecision = 7;
    private Integer binaryPrecision;
    private Boolean supportsNaN;
    private Boolean supportsInfinity;

    public TapFloat() {
        super();
        bit(BIT);
        precision(effectivePrecision);
        fixed(false);
        minValue(BigDecimal.valueOf(-Float.MAX_VALUE));
        maxValue(BigDecimal.valueOf(Float.MAX_VALUE));
    }

    @Override
    public Integer getBit() {
        return super.getBit();
    }

    @Override
    public void setBit(Integer bit) {
        if (bit != null && bit != BIT) {
            throw new IllegalArgumentException("TapFloat bit must be 32");
        }
        super.setBit(bit);
    }

    @Override
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
        Integer previous = this.effectivePrecision;
        this.effectivePrecision = effectivePrecision;
        if (getPrecision() == null || getPrecision().equals(previous)) {
            super.setPrecision(effectivePrecision);
        }
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

    @Override
    public TapFloat precision(Integer precision) {
        super.precision(precision);
        return this;
    }

    @Override
    public TapFloat scale(Integer scale) {
        super.scale(scale);
        return this;
    }

    @Override
    public TapFloat unsigned(Boolean unsigned) {
        super.unsigned(unsigned);
        return this;
    }

    @Override
    public TapFloat zerofill(Boolean zerofill) {
        super.zerofill(zerofill);
        return this;
    }

    @Override
    public TapFloat minValue(BigDecimal minValue) {
        super.minValue(minValue);
        return this;
    }

    @Override
    public TapFloat maxValue(BigDecimal maxValue) {
        super.maxValue(maxValue);
        return this;
    }

    @Override
    public void setFixed(Boolean fixed) {
        if (Boolean.TRUE.equals(fixed)) {
            throw new IllegalArgumentException("TapFloat cannot be fixed-point");
        }
        super.setFixed(fixed);
    }

    @Override
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
    public TapFloat cannotWrite(Boolean cannotWrite) {
        super.cannotWrite(cannotWrite);
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
        return TapFloatValue.class;
    }

    @Override
    public ToTapValueCodec<?> toTapValueCodec() {
        return InstanceFactory.instance(ToTapValueCodec.class, TapDefaultCodecs.TAP_FLOAT_VALUE);
    }
}
