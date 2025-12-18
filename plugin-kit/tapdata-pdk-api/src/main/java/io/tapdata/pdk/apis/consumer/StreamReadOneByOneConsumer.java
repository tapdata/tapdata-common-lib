package io.tapdata.pdk.apis.consumer;

import io.tapdata.entity.event.TapEvent;
import io.tapdata.pdk.apis.utils.StateListener;

import java.util.List;
import java.util.Optional;
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
    public static StreamReadOneByOneConsumer create(BiConsumer<TapEvent, Object> consumer, Supplier<Integer> batchSizeGetter, Supplier<Long> batchSizeTimeoutMSGetter) {
        return new StreamReadOneByOneConsumer(batchSizeGetter, batchSizeTimeoutMSGetter).consumer(consumer);
    }
    BiConsumer<List<TapEvent>, Object> batchConsumer;
    Supplier<Integer> batchSizeGetter;
    Supplier<Long> batchSizeTimeoutMSGetter;

    public StreamReadOneByOneConsumer(Supplier<Integer> batchSizeGetter, Supplier<Long> batchSizeTimeoutMSGetter) {
        this.batchSizeGetter = batchSizeGetter;
        this.batchSizeTimeoutMSGetter = batchSizeTimeoutMSGetter;
    }

    public StreamReadOneByOneConsumer() {
        this.batchSizeGetter = () -> 1;
        this.batchSizeTimeoutMSGetter = () -> 1000L;
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

    public long getBatchSizeTimeoutMS() {
        return Optional.ofNullable(batchSizeTimeoutMSGetter).map(Supplier::get).orElse(1000L);
    }
}
