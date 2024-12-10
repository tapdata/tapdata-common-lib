package io.tapdata.wsserver.channels.io;

import io.tapdata.modules.api.net.data.Chunk;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/12/9 17:23
 */
public class StreamManagerTest {

    @Test
    void testMainLine() {

        Assertions.assertDoesNotThrow(() -> {

            InputStream input = StreamManager.getInstance().createPipeInputStream("streamId");
            Assertions.assertNotNull(input);

            Chunk chunk = new Chunk();
            chunk.setChunkNum(1);
            chunk.setTotalChunks(1);
            chunk.setId("id");
            chunk.setContent("test".getBytes(StandardCharsets.UTF_8));
            chunk.setOffset(chunk.getContent().length);
            chunk.setOriginalType(1);
            StreamManager.getInstance().writeData("streamId", chunk);


            ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
            IOUtils.copy(input, output);
            byte[] receivedData = output.toByteArray();
            Assertions.assertNotNull(receivedData);
            Assertions.assertEquals("test", new String(receivedData));
        });
    }

    @Test
    void testClean() throws IOException, NoSuchFieldException, IllegalAccessException {

        Field field = StreamManager.class.getDeclaredField("cachedOutputStreams");
        field.setAccessible(true);
        Map cachedOutputStreams = (Map) field.get(StreamManager.getInstance());

        InputStream input = StreamManager.getInstance().createPipeInputStream("streamId");
        Assertions.assertNotNull(input);

        Assertions.assertFalse(cachedOutputStreams.isEmpty());

        Assertions.assertEquals(1, cachedOutputStreams.size());

        Assertions.assertNotNull(cachedOutputStreams.get("streamId"));
        Field expirationField = cachedOutputStreams.get("streamId").getClass().getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(cachedOutputStreams.get("streamId"), System.currentTimeMillis() - 1);

        StreamManager.getInstance().cleanTimeoutStream();
        Assertions.assertTrue(cachedOutputStreams.isEmpty());

    }

}
