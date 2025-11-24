package io.tapdata.pdk.apis.consumer;

import io.tapdata.entity.event.TapEvent;

import java.util.List;
import java.util.function.BiConsumer;

public class StreamReadConsumer extends TapStreamReadConsumer<List<TapEvent>, Object> {

    public static StreamReadConsumer create(BiConsumer<List<TapEvent>, Object> consumer) {
        return (StreamReadConsumer) new StreamReadConsumer().consumer(consumer);
    }
}
