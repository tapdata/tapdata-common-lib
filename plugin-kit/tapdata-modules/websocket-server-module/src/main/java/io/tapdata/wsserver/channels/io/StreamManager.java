package io.tapdata.wsserver.channels.io;

import io.tapdata.modules.api.net.data.Chunk;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/12/3 19:06
 */
public class StreamManager {
    private static final StreamManager instance = new StreamManager();
    public static long streamMaxLiveTime = 10 * 60 * 1000;

    private final Map<String, CachedItem> cachedOutputStreams = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private StreamManager() {
        scheduledExecutorService.scheduleAtFixedRate(this::cleanTimeoutStream, 1, 1, TimeUnit.MINUTES);
    }

    public static StreamManager getInstance() {
        return instance;
    }

    private void cleanTimeoutStream() {
        List<CachedItem> expirations = cachedOutputStreams.values().stream().filter(CachedItem::isExpire).collect(Collectors.toList());
        if (!expirations.isEmpty())
            expirations.forEach(c -> {
                cachedOutputStreams.remove(c.id);
                IOUtils.closeQuietly(c.pipedOutputStream);
            });
    }

    public InputStream createPipeInputStream(String id) throws IOException {

        PipedInputStream pipedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
        cachedOutputStreams.put(id, new CachedItem(id, pipedOutputStream, System.currentTimeMillis() + streamMaxLiveTime));

        return pipedInputStream;
    }

    public void writeData(String id, Chunk chunk) {
        CachedItem cachedItem = cachedOutputStreams.get(id);
        if (cachedItem == null) {
            return;
        }
        try {
            cachedItem.pipedOutputStream.write(chunk.getContent(), 0, chunk.getOffset());
            cachedItem.pipedOutputStream.flush();
            if (chunk.getTotalChunks()!= null && chunk.getTotalChunks().equals(chunk.getChunkNum())) {
                cachedOutputStreams.remove(id);
                cachedItem.pipedOutputStream.close();
            }
        } catch (IOException e) {
            cachedOutputStreams.remove(id);
            IOUtils.closeQuietly(cachedItem.pipedOutputStream);
        }
    }

    private static class CachedItem {
        public String id;
        public PipedOutputStream pipedOutputStream;
        public long expiration;

        public CachedItem(String id, PipedOutputStream pipedOutputStream, long expiration) {
            this.id = id;
            this.pipedOutputStream = pipedOutputStream;
            this.expiration = expiration;
        }

        public boolean isExpire() {
            return expiration - System.currentTimeMillis() < 0;
        }
    }

}
