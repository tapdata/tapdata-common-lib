package io.tapdata.pdk.apis.consumer;

import io.tapdata.entity.event.TapEvent;
import io.tapdata.pdk.apis.utils.StateListener;

import java.util.List;
import java.util.function.BiConsumer;

public class StreamReadConsumer extends TapStreamReadConsumer<List<TapEvent>, Object> {

    public static StreamReadConsumer create(BiConsumer<List<TapEvent>, Object> consumer) {
        return new StreamReadConsumer().consumer(consumer);
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
    public StreamReadConsumer consumer(BiConsumer<List<TapEvent>, Object> consumer) {
        return (StreamReadConsumer) super.consumer(consumer);
    }

    @Override
    public StreamReadConsumer stateListener(StateListener<Integer> stateListener) {
        this.stateListener = stateListener;
        return this;
    }
}
