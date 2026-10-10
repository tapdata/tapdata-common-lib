package io.tapdata.entity.codec;

import io.tapdata.entity.schema.type.TapType;

/**
 * Converts an inbound value to the Java value represented by a schema type.
 *
 * <p>This is intentionally separate from {@link ToTapValueCodec}: hot paths
 * which already have a schema and do not need TapValue metadata can avoid
 * allocating a mutable TapValue wrapper for every record.</p>
 */
public interface ToTapRawValueCodec {
    Object toRawValue(Object value, TapType tapType);
}
