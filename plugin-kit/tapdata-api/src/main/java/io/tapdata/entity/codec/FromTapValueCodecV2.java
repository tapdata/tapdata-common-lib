package io.tapdata.entity.codec;

import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.value.TapValue;

/**
 * Runtime codec which can convert a TapValue using the target field metadata.
 * The target field may be null when the caller has no target schema.
 */
public interface FromTapValueCodecV2<T extends TapValue<?, ?>> {
    Object fromTapValue(T tapValue, TapField targetField);
}
