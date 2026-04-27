package io.tapdata.entity.event.ddl.table;

import io.tapdata.entity.event.TapEvent;

import java.time.Duration;

public class TapAlterTableTTLEvent extends TapTableEvent {

    public static final int TYPE = 211;

    public TapAlterTableTTLEvent() {
        super(TYPE);
    }

    private Duration duration;

    public TapAlterTableTTLEvent duration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public void clone(TapEvent tapEvent) {
        super.clone(tapEvent);
        if (tapEvent instanceof TapAlterTableTTLEvent) {
            TapAlterTableTTLEvent tapAlterTableTTLEvent = (TapAlterTableTTLEvent) tapEvent;
            if (duration != null)
                tapAlterTableTTLEvent.duration = duration;
        }
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

}
