package io.tapdata.entity.event.ddl;

public class TapDDLUnknownEvent extends TapDDLEvent {

    public static final int TYPE = 900;

    public TapDDLUnknownEvent() {
        super(TYPE);
    }
}
