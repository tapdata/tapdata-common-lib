package io.tapdata.pdk.apis.consumer;

import io.tapdata.entity.event.TapEvent;
import io.tapdata.pdk.apis.utils.StateListener;

import java.util.List;
import java.util.function.BiConsumer;

public class StreamReadConsumer extends TapStreamReadConsumer<List<TapEvent>, Object> {
    public static final int STATE_STREAM_READ_PENDING = 1;
    public static final int STATE_STREAM_READ_STARTED = 10;
    public static final int STATE_STREAM_READ_ENDED = 100;
    protected int state = STATE_STREAM_READ_PENDING;

    protected BiConsumer<List<TapEvent>, Object> consumer;
    protected StateListener<Integer> stateListener;
    protected boolean asyncMethodAndNoRetry = false;

    public static StreamReadConsumer create(BiConsumer<List<TapEvent>, Object> consumer) {
        return new StreamReadConsumer().consumer(consumer);
    }

    @Override
    public void asyncMethodAndNoRetry() {
        asyncMethodAndNoRetry = true;
    }

    @Override
    public synchronized void streamReadStarted() {
        if(state == STATE_STREAM_READ_STARTED)
            return;

        int old = state;
        state = STATE_STREAM_READ_STARTED;
        if(stateListener != null) {
            stateListener.stateChanged(old, state);
        }
    }

    @Override
    public synchronized void streamReadEnded() {
        if(state == STATE_STREAM_READ_ENDED)
            return;

        int old = state;
        state = STATE_STREAM_READ_ENDED;
        if(stateListener != null) {
            stateListener.stateChanged(old, state);
        }
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public boolean isAsyncMethodAndNoRetry() {
        return asyncMethodAndNoRetry;
    }

    @Override
    public StreamReadConsumer consumer(BiConsumer<List<TapEvent>, Object> consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public StreamReadConsumer stateListener(StateListener<Integer> stateListener) {
        this.stateListener = stateListener;
        return this;
    }

    @Override
    public void accept(List<TapEvent> e, Object offset) {
        consumer.accept(e, offset);
    }
}
