package io.tapdata.entity.mapping.type;


import io.tapdata.entity.result.TapResult;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.type.TapXml;

import java.math.BigDecimal;
import java.util.Map;

import static io.tapdata.entity.simplify.TapSimplify.tapXml;

public class TapXmlMapping extends TapMapping {

    @Override
    public void from(Map<String, Object> info) {

    }

    @Override
    public TapType toTapType(String dataType, Map<String, String> params) {
        return tapXml();
    }

    @Override
    public TapResult<String> fromTapType(String typeExpression, TapType tapType) {
        if (tapType instanceof TapXml) {
            return TapResult.successfully(removeBracketVariables(typeExpression, 0));
        }
        return null;
    }

    @Override
    public BigDecimal matchingScore(TapField field) {
        if (field.getTapType() instanceof TapXml) {
            return TapMapping.MAX_SCORE;
        }
        return TapMapping.MIN_SCORE;
    }
}
