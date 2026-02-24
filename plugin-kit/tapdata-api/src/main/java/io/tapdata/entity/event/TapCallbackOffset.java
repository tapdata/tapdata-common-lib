package io.tapdata.entity.event;

import java.util.HashMap;

/**
 * TapCallbackOffset - 封装 flush offset callback 需要的数据
 * 用于在数据刷新成功后，将 offset 信息传递给目标节点
 */
public class TapCallbackOffset extends HashMap<String, Object> {
    
    private static final long serialVersionUID = 1L;
    
    // 从 TapRecordEvent 中提取的 offset 信息
    public static final String KEY_BATCH_OFFSET = "batchOffset";
    public static final String KEY_STREAM_OFFSET = "streamOffset";
    public static final String KEY_TABLE_ID = "tableId";
    public static final String KEY_SYNC_STAGE = "syncStage";
    public static final String KEY_SOURCE_TIME = "sourceTime";
    public static final String KEY_EVENT_TIME = "eventTime";
    public static final String KEY_NODE_IDS = "nodeIds";
    
    public TapCallbackOffset() {
        super();
    }
    
    /**
     * 设置批次偏移量（用于全量同步）
     */
    public TapCallbackOffset batchOffset(Object batchOffset) {
        if (batchOffset != null) {
            put(KEY_BATCH_OFFSET, batchOffset);
        }
        return this;
    }
    
    /**
     * 设置流偏移量（用于增量同步）
     */
    public TapCallbackOffset streamOffset(Object streamOffset) {
        if (streamOffset != null) {
            put(KEY_STREAM_OFFSET, streamOffset);
        }
        return this;
    }
    
    /**
     * 设置表ID
     */
    public TapCallbackOffset tableId(String tableId) {
        if (tableId != null) {
            put(KEY_TABLE_ID, tableId);
        }
        return this;
    }
    
    /**
     * 设置同步阶段（INITIAL_SYNC 或 CDC）
     */
    public TapCallbackOffset syncStage(String syncStage) {
        if (syncStage != null) {
            put(KEY_SYNC_STAGE, syncStage);
        }
        return this;
    }
    
    /**
     * 设置源时间
     */
    public TapCallbackOffset sourceTime(Long sourceTime) {
        if (sourceTime != null) {
            put(KEY_SOURCE_TIME, sourceTime);
        }
        return this;
    }
    
    /**
     * 设置事件时间
     */
    public TapCallbackOffset eventTime(Long eventTime) {
        if (eventTime != null) {
            put(KEY_EVENT_TIME, eventTime);
        }
        return this;
    }
    
    /**
     * 设置节点ID列表
     */
    public TapCallbackOffset nodeIds(Object nodeIds) {
        if (nodeIds != null) {
            put(KEY_NODE_IDS, nodeIds);
        }
        return this;
    }
    /**
     * 检查是否有有效的 offset 数据
     */
    public boolean hasValidOffset() {
        return get(KEY_BATCH_OFFSET) != null || get(KEY_STREAM_OFFSET) != null;
    }
    
    @Override
    public String toString() {
        return "TapCallbackOffset{" +
                "batchOffset=" + get(KEY_BATCH_OFFSET) +
                ", streamOffset=" + get(KEY_STREAM_OFFSET) +
                ", tableId='" + get(KEY_TABLE_ID) + '\'' +
                ", syncStage='" + get(KEY_SYNC_STAGE) + '\'' +
                ", sourceTime=" + get(KEY_SOURCE_TIME) +
                ", eventTime=" + get(KEY_EVENT_TIME) +
                ", nodeIds=" + get(KEY_NODE_IDS) +
                '}';
    }
}
