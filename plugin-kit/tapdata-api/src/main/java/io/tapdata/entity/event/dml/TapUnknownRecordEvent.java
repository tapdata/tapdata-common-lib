package io.tapdata.entity.event.dml;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 未知事件
 *
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2024/9/4 11:13 Create
 */
public class TapUnknownRecordEvent extends TapRecordEvent {
    public static final int TYPE = 333;

    private Serializable data;

    public TapUnknownRecordEvent() {
        super(TYPE);
    }

    public Serializable getData() {
        return data;
    }

    public void setData(Serializable data) {
        this.data = data;
    }

    @Override
    public Map<String, Object> getFilter(Collection<String> primaryKeys) {
        return Collections.emptyMap();
    }

    // ---------- 工具方法 ----------

    public static TapUnknownRecordEvent create() {
        return new TapUnknownRecordEvent().init();
    }

    public TapUnknownRecordEvent init() {
        time = System.currentTimeMillis();
        return this;
    }

    public TapUnknownRecordEvent value(Serializable data) {
        this.data = data;
        return this;
    }
}
