package io.tapdata.pdk.apis.consumer;

import io.tapdata.entity.event.TapEvent;
import io.tapdata.pdk.apis.utils.StateListener;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

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
    public static StreamReadOneByOneConsumer create(BiConsumer<TapEvent, Object> consumer, Supplier<Integer> batchSizeGetter) {
        return new StreamReadOneByOneConsumer(batchSizeGetter).consumer(consumer);
    }
    BiConsumer<List<TapEvent>, Object> batchConsumer;
    Supplier<Integer> batchSizeGetter;

    public StreamReadOneByOneConsumer(Supplier<Integer> batchSizeGetter) {
        this.batchSizeGetter = batchSizeGetter;
    }

    public StreamReadOneByOneConsumer() {
        this.batchSizeGetter = () -> 1;
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

    public StreamReadOneByOneConsumer batchConsumer(BiConsumer<List<TapEvent>, Object> consumer) {
        this.batchConsumer = consumer;
        return this;
    }

    @Override
    public StreamReadOneByOneConsumer stateListener(StateListener<Integer> stateListener) {
        this.stateListener = stateListener;
        return this;
    }

    public void accept(List<TapEvent> events, Object offset) {
        batchConsumer.accept(events, offset);
    }

    public int getBatchSize() {
        return batchSizeGetter.get();
    }
}
