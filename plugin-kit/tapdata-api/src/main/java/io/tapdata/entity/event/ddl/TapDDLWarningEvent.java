package io.tapdata.entity.event.ddl;

public class TapDDLWarningEvent extends TapDDLEvent {

    public static final int TYPE = 901;

    private String warningMessage;

    public TapDDLWarningEvent() {
        super(TYPE);
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }
}
