package io.tapdata.modules.api.net.service.node.connection.entity;

import io.tapdata.modules.api.net.data.FileMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/12/9 17:04
 */
public class NodeMessageTest {

    @Test
    void testFileMeta() {
        NodeMessage nodeMessage = new NodeMessage();
        nodeMessage.fileMeta(new FileMeta());

        Assertions.assertNotNull(nodeMessage.getFileMeta());
        nodeMessage.setFileMeta(FileMeta.builder().build());
        Assertions.assertNotNull(nodeMessage.getFileMeta());
    }
}
