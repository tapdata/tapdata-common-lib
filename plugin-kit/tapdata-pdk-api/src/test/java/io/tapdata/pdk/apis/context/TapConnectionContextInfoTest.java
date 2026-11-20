package io.tapdata.pdk.apis.context;

import io.tapdata.entity.utils.DataMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TapConnectionContextInfoTest {
    @Test
    void eachContextHasItsOwnInfoIndependentOfConfiguration() {
        DataMap config = new DataMap();
        TapConnectionContext test = new TapConnectionContext(null, config, null, null);
        TapConnectorContext task = new TapConnectorContext(null, config, null, null);
        String id = "0123456789abcdef01234567";

        task.getInfo().put(TapConnectionContext.INFO_CONNECTION_ID, id);
        assertEquals(id, task.getInfo().getString("connectionId"));
        assertTrue(test.getInfo().isEmpty());
        assertTrue(config.isEmpty());
    }
}
