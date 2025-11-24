package io.tapdata.pdk.apis.consumer;

import io.tapdata.pdk.apis.utils.StateListener;

import java.util.function.BiConsumer;

/**
 * @author <a href="2749984520@qq.com">Gavin'Xiao</a>
 * @author <a href="https://github.com/11000100111010101100111">Gavin'Xiao</a>
 * @version v1.0 2025/11/21 10:06 Create
 * @description
 */
public class TapStreamReadConsumer<Event, Offset> implements BiConsumer<Event, Offset> {
    public static final int STATE_STREAM_READ_PENDING = 1;
    public static final int STATE_STREAM_READ_STARTED = 10;
    public static final int STATE_STREAM_READ_ENDED = 100;
    protected int state = STATE_STREAM_READ_PENDING;

    protected BiConsumer<Event, Offset> consumer;
    protected StateListener<Integer> stateListener;
    protected boolean asyncMethodAndNoRetry = false;

    public void asyncMethodAndNoRetry() {
        asyncMethodAndNoRetry = true;
    }

    public synchronized void streamReadStarted() {
        if(state == STATE_STREAM_READ_STARTED)
            return;

        int old = state;
        state = STATE_STREAM_READ_STARTED;
        if(stateListener != null) {
            stateListener.stateChanged(old, state);
        }
    }

    public synchronized void streamReadEnded() {
        if(state == STATE_STREAM_READ_ENDED)
            return;

        int old = state;
        state = STATE_STREAM_READ_ENDED;
        if(stateListener != null) {
            stateListener.stateChanged(old, state);
        }
    }

    public int getState() {
        return state;
    }

    public boolean isAsyncMethodAndNoRetry() {
        return asyncMethodAndNoRetry;
    }

    public static <Event, Offset> TapStreamReadConsumer<Event, Offset> instance(BiConsumer<Event, Offset> consumer) {
        return new TapStreamReadConsumer<Event, Offset>().consumer(consumer);
    }

    public TapStreamReadConsumer<Event, Offset> consumer(BiConsumer<Event, Offset> consumer) {
        this.consumer = consumer;
        return this;
    }

    public TapStreamReadConsumer<Event, Offset> stateListener(StateListener<Integer> stateListener) {
        this.stateListener = stateListener;
        return this;
    }

    @Override
    public void accept(Event e, Offset offset) {
        consumer.accept(e, offset);
    }
}
