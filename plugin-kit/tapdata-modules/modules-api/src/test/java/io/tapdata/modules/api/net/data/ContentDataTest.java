package io.tapdata.modules.api.net.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/12/9 16:45
 */
public class ContentDataTest {

    // generator JUnit test case
    private ContentData contentData;

    @BeforeEach
    void setUp() {
        contentData = new ContentData((byte)61) {
        };
    }

    @Test
    void testContentData() {

        FileMeta fileMeta = FileMeta.builder().fileSize(1024L).transferFile(true).filename("a.log").code("ok")
                .fileInputStream(new ByteArrayInputStream(new byte[0]))
                .build();
        contentData.fileMeta(fileMeta);

        Assertions.assertNotNull(contentData.getFileMeta());
        contentData.setFileTransfer(fileMeta);
        Assertions.assertNotNull(contentData.getFileMeta());

        contentData.persistent();
        byte[] data = contentData.getData();

        ContentData contentData1 = new ContentData((byte) 61) {
        };
        contentData1.setData(data);
        contentData1.setContentEncode(BinaryCodec.ENCODE_JAVA_CUSTOM_SERIALIZER);
        contentData1.resurrect();
        Assertions.assertNotNull(contentData1.getFileMeta());
        Assertions.assertEquals(contentData.getFileMeta().getFileSize(), contentData1.getFileMeta().getFileSize());
        Assertions.assertEquals(contentData.getFileMeta().getCode(), contentData1.getFileMeta().getCode());
        Assertions.assertEquals(contentData.getFileMeta().getFilename(), contentData1.getFileMeta().getFilename());
        Assertions.assertEquals(contentData.getFileMeta().isTransferFile(), contentData1.getFileMeta().isTransferFile());

        contentData.setFileTransfer(null);
        contentData.persistent();
        data = contentData.getData();
        contentData1 = new ContentData((byte) 61) {
        };
        contentData1.setData(data);
        contentData1.setContentEncode(BinaryCodec.ENCODE_JAVA_CUSTOM_SERIALIZER);
        contentData1.resurrect();
        Assertions.assertNull(contentData1.getFileMeta());
    }

    @Test
    void testFileMeta() {
        Assertions.assertDoesNotThrow(() -> {
            FileMeta fileMeta = new FileMeta();
            fileMeta.setFilename("a.log");
            fileMeta.setFileSize(1L);
            fileMeta.setFileInputStream(new ByteArrayInputStream(new byte[0]));
            fileMeta.setCode("ok");
            fileMeta.setTransferFile(true);

            Assertions.assertNotNull(fileMeta.getFileInputStream());
        });

    }

}
