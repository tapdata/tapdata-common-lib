package io.tapdata.pdk.apis.consumer;

import io.tapdata.entity.event.TapEvent;
import io.tapdata.pdk.apis.utils.StateListener;

import java.util.function.BiConsumer;

/**
 * @author <a href="2749984520@qq.com">Gavin'Xiao</a>
 * @author <a href="https://github.com/11000100111010101100111">Gavin'Xiao</a>
 * @version v1.0 2025/11/21 11:16 Create
 * @description
 */
public class StreamReadOneByOneConsumer extends TapStreamReadConsumer<TapEvent, Object> {
    public static StreamReadOneByOneConsumer create(BiConsumer<TapEvent, Object> consumer) {
        return new StreamReadOneByOneConsumer().consumer(consumer);
    }

    @Override
    public void asyncMethodAndNoRetry() {
        super.asyncMethodAndNoRetry();
    }

    @Override
    public synchronized void streamReadStarted() {
        super.streamReadStarted();
    }

    @Override
    public synchronized void streamReadEnded() {
        super.streamReadEnded();
    }

    @Override
    public int getState() {
        return super.getState();
    }

    @Override
    public boolean isAsyncMethodAndNoRetry() {
        return super.isAsyncMethodAndNoRetry();
    }

    @Override
    public StreamReadOneByOneConsumer consumer(BiConsumer<TapEvent, Object> consumer) {
        return (StreamReadOneByOneConsumer) super.consumer(consumer);
    }

    @Override
    public StreamReadOneByOneConsumer stateListener(StateListener<Integer> stateListener) {
        this.stateListener = stateListener;
        return this;
    }
}
