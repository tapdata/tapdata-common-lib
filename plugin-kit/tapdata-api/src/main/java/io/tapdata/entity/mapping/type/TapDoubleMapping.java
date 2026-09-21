package io.tapdata.entity.mapping.type;

import io.tapdata.entity.result.TapResult;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapType;

import java.math.BigDecimal;
import java.util.Map;

/** Mapping for an actual binary64 source type. */
public class TapDoubleMapping extends TapMapping {
    private Map<String, Object> info;

    @Override
    public void from(Map<String, Object> info) {
        this.info = info;
    }

    @Override
    public TapType toTapType(String dataType, Map<String, String> params) {
        TapDouble type = new TapDouble();
        if (info != null) {
            FloatingPointMappingSupport.apply(info, type);
        }
        return type;
    }

    @Override
    public TapResult<String> fromTapType(String typeExpression, TapType tapType) {
        if (!(tapType instanceof TapDouble) && !(tapType instanceof TapFloat)) {
            return null;
        }
        return new TapResult<String>().result(TapResult.RESULT_SUCCESSFULLY).data(typeExpression);
    }

    @Override
    public BigDecimal matchingScore(TapField field) {
        if (field == null || field.getTapType() == null) {
            return TapMapping.MIN_SCORE;
        }
        if (field.getTapType() instanceof TapDouble) {
            return TapMapping.MAX_SCORE;
        }
        return field.getTapType() instanceof TapFloat ? TapMapping.MAX_SCORE.subtract(BigDecimal.ONE) : TapMapping.MIN_SCORE;
    }
}
