package io.tapdata.pdk.apis.consumer;

import io.tapdata.entity.event.TapEvent;
import io.tapdata.entity.event.dml.TapInsertRecordEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.function.BiConsumer;

class StreamReadConsumerTest {

    StreamReadConsumer consumer;
    BiConsumer<List<TapEvent>, Object> biConsumer;

    @BeforeEach
    void init(){
        consumer = new StreamReadConsumer();
        biConsumer = new BiConsumer<List<TapEvent>, Object>() {
            @Override
            public void accept(List<TapEvent> tapEvents, Object o) {
                //do nothing
            }
        };
        ReflectionTestUtils.setField(consumer, "consumer", biConsumer);
    }

    @Nested
    class acceptOneByOneTest {
        @Test
        void test() {
            Assertions.assertDoesNotThrow(() ->  consumer.accept(new TapInsertRecordEvent(), new Object()));
        }
    }
}