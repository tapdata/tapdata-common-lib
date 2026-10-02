package io.tapdata.entity.mapping.type;

import io.tapdata.entity.result.TapResult;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapType;

import java.math.BigDecimal;
import java.util.Map;

/** Mapping for an actual binary32 source type. */
public class TapFloatMapping extends TapMapping {
    private Map<String, Object> info;

    @Override
    public void from(Map<String, Object> info) {
        this.info = info;
    }

    @Override
    public TapType toTapType(String dataType, Map<String, String> params) {
        TapFloat type = new TapFloat();
        if (info != null) {
            FloatingPointMappingSupport.apply(info, dataType, params, type);
        }
        return type;
    }

    @Override
    public TapResult<String> fromTapType(String typeExpression, TapType tapType) {
        if (!(tapType instanceof TapFloat)) {
            return null;
        }
        return new TapResult<String>().result(TapResult.RESULT_SUCCESSFULLY).data(typeExpression);
    }

    @Override
    public BigDecimal matchingScore(TapField field) {
        return field != null && field.getTapType() instanceof TapFloat ? TapMapping.MAX_SCORE : TapMapping.MIN_SCORE;
    }
}
