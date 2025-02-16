package io.tapdata.entity.schema.type;

import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.value.TapMoneyValue;
import io.tapdata.entity.schema.value.TapValue;
import io.tapdata.entity.utils.InstanceFactory;

import static io.tapdata.entity.simplify.TapSimplify.tapMoney;

public class TapMoney extends TapType {
    public TapMoney() {
        type = TYPE_MONEY;
    }

    private Integer precision;
    public TapMoney precision(Integer precision) {
        this.precision = precision;
        return this;
    }
    private Integer scale;
    public TapMoney scale(Integer scale) {
        this.scale = scale;
        return this;
    }

    public Integer getPrecision() {
        return precision;
    }
    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Integer getScale() {
        return scale;
    }
    public void setScale(Integer scale) {
        this.scale = scale;
    }
    @Override
    public TapType cloneTapType() {
        return tapMoney().precision(precision).scale(scale);
    }

    @Override
    public Class<? extends TapValue<?, ?>> tapValueClass() {
        return TapMoneyValue.class;
    }

    @Override
    public ToTapValueCodec<?> toTapValueCodec() {
        return InstanceFactory.instance(ToTapValueCodec.class, TapDefaultCodecs.TAP_MONEY_VALUE);
    }
}
