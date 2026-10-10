package io.tapdata.entity.codec.filter;

/**
 * Controls how schema-known TapFloat/TapDouble fields are represented while
 * transforming a record.
 */
public enum FloatingPointTransformMode {
    /** Preserve the existing TapValue conversion contract. */
    COMPATIBILITY,
    /** Emit the schema's raw Java value and avoid a per-record TapValue wrapper. */
    RAW
}
