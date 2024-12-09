package io.tapdata.wsclient.modules.imclient.impls.websocket;

import io.tapdata.entity.utils.ReflectionUtil;
import io.tapdata.modules.api.net.data.FileMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import static org.mockito.Mockito.*;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/12/9 17:07
 */
public class WebsocketPushChannelTest {

    @Test
    void testWriteFile() throws NoSuchMethodException {

        WebsocketPushChannel websocketPushChannel = mock(WebsocketPushChannel.class);
        doCallRealMethod().when(websocketPushChannel).writeFile(anyString(), any());

        byte[] data = new byte[1024 * 100 * 2 + 1024];
        FileMeta fileMeta = FileMeta.builder().code("ok").fileSize(1024L)
                .filename("a.log").transferFile(true).fileInputStream(new ByteArrayInputStream(data))
                .build();
        Assertions.assertDoesNotThrow(() -> {
            websocketPushChannel.writeFile("id", fileMeta);

            verify(websocketPushChannel, times(3)).send(any());
        });

    }

}
