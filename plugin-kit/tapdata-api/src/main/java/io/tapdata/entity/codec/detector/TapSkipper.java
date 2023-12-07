package io.tapdata.entity.codec.detector;

import io.tapdata.entity.schema.TapField;

public interface TapSkipper extends TapDetector {
    default boolean skip(TapField field){
        return true;
    }
}
