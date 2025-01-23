package io.tapdata.entity.mapping.type;


import io.tapdata.entity.result.TapResult;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapMoney;
import io.tapdata.entity.schema.type.TapType;

import java.math.BigDecimal;
import java.util.Map;

import static io.tapdata.entity.simplify.TapSimplify.tapMoney;

public class TapMoneyMapping extends TapMapping{
    public static final String KEY_PRECISION_DEFAULT = "defaultPrecision";
    public static final String KEY_SCALE_DEFAULT = "defaultScale";

    private Integer defaultPrecision;
    private Integer defaultScale;
    @Override
    public void from(Map<String, Object> info) {
        Object defaultPrecisionObj = getObject(info, KEY_PRECISION_DEFAULT);
        if(defaultPrecisionObj instanceof Number) {
            defaultPrecision = ((Number) defaultPrecisionObj).intValue();
        }

        Object defaultScaleObj = getObject(info, KEY_SCALE_DEFAULT);
        if(defaultScaleObj instanceof Number) {
            defaultScale = ((Number) defaultScaleObj).intValue();
        }

    }

    @Override
    public TapType toTapType(String dataType, Map<String, String> params) {
        return tapMoney().scale(defaultScale).precision(defaultPrecision);
    }

    @Override
    public TapResult<String> fromTapType(String typeExpression, TapType tapType) {
        if(tapType instanceof TapMoney){
            return TapResult.successfully(removeBracketVariables(typeExpression, 0));
        }
        return null;
    }

    @Override
    public BigDecimal matchingScore(TapField field) {
        if (field.getTapType() instanceof TapMoney) {
            return TapMapping.MAX_SCORE;
        }
        return TapMapping.MIN_SCORE;
    }
}
