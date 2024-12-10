package io.tapdata.modules.api.net.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.tapdata.modules.api.net.data.BinaryCodec.ENCODE_JAVA_CUSTOM_SERIALIZER;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/12/9 16:01
 */
public class ChunkTest {

    @Test
    void testSerializableChunk() {
        Chunk chunk = new Chunk();
        chunk.setId("chunkId");
        chunk.setOriginalType(1);
        chunk.setContent("test".getBytes(StandardCharsets.UTF_8));
        chunk.setOffset(0);
        chunk.setTotalChunks(1);
        chunk.setChunkNum(1);

        chunk.persistent();
        byte[] chunkData = chunk.getData();

        Assertions.assertNotNull(chunkData);

        Chunk chunk1 = new Chunk(chunkData, ENCODE_JAVA_CUSTOM_SERIALIZER);

        chunk1.resurrect();

        Assertions.assertEquals(chunk.getId(), chunk1.getId());
        Assertions.assertEquals(chunk.getOriginalType(), chunk1.getOriginalType());
        Assertions.assertEquals(new String(chunk.getContent(), StandardCharsets.UTF_8), new String(chunk1.getContent(), StandardCharsets.UTF_8));
        Assertions.assertEquals(chunk.getOffset(), chunk1.getOffset());
        Assertions.assertEquals(chunk.getTotalChunks(), chunk1.getTotalChunks());
        Assertions.assertEquals(chunk.getChunkNum(), chunk1.getChunkNum());

    }

}
