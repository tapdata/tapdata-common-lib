package io.tapdata.wsserver.channels.websocket.event;

import io.tapdata.modules.api.net.data.Chunk;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/12/3 18:28
 */
public class ChunkDataReceivedEvent extends NettyEvent<ChunkDataReceivedEvent> {
    private Chunk chunk;
    public ChunkDataReceivedEvent chunk(Chunk chunk) {
        this.chunk = chunk;
        return this;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }
}
