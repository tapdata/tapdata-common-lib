package io.tapdata.entity.codec.filter;

import com.google.common.collect.Lists;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.entity.simplify.TapSimplify;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class TapTableTest {

    @Test
    void testMultiThread() {
        TapTable tapTable = new TapTable("test_table");
        TapField tapField = new TapField("field_pk", "int").primaryKeyPos(1).isPrimaryKey(true);
        tapTable.add(tapField);
        for (int i = 0; i < 100; i++) {
            tapTable.add(new TapField("field" + i, "varchar(20)"));
        }
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch countDownLatch = new CountDownLatch(100);
        AtomicReference<Exception> exception = new AtomicReference<>();
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        tapTable.setLogicPrimaries(Lists.newArrayList("field_pk", "field" + (int) (100 * Math.random())));
                        if (Math.random() > 0.5) {
                            tapTable.add(new TapField("field_ex" + j, "varchar(20)"));
                        }
                        TapSimplify.sleep((long) (10 * Math.random()));
                        System.out.println(tapTable.primaryKeys());
                    }
                } catch (Exception e) {
                    exception.set(e);
                }
                countDownLatch.countDown();
            });
        }
        while (countDownLatch.getCount() > 0) {
            if (null != exception.get()) {
                throw new RuntimeException(exception.get());
            }
            TapSimplify.sleep(1000);
        }
    }
}
