package io.tapdata.modules.api.net.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/12/9 17:02
 */
public class IncomingDataTest {

    @Test
    void testId() {
        IncomingData incomingData = new IncomingData();
        incomingData.id("id");
        Assertions.assertEquals("id", incomingData.getId());
    }
}
