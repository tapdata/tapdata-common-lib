package io.tapdata.pdk.apis.consumer;

import io.tapdata.entity.event.TapEvent;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * @description
 *
 * @author <a href="2749984520@qq.com">Gavin'Xiao</a>
 * @author <a href="https://github.com/11000100111010101100111">Gavin'Xiao</a>
 * @version v1.0 2025/11/21 11:16 Create
 */
public class StreamReadOneByOneConsumer extends TapStreamReadConsumer<TapEvent, Object> {
    public static StreamReadOneByOneConsumer create(BiConsumer<TapEvent, Object> consumer) {
        return (StreamReadOneByOneConsumer) new StreamReadOneByOneConsumer().consumer(consumer);
    }
}
