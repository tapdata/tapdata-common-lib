package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;

/**
 * Live state of one object a capture depends on, as observed by
 * {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcInspectFunction}.
 */
public class TriggerCdcObjectState implements Serializable {
    private static final long serialVersionUID = 1L;

    protected TriggerCdcObjectType type;
    protected String schema;
    protected String name;
    /** False when the object is missing, which breaks the capture. */
    protected boolean present;
    /**
     * For triggers: false when the trigger exists but is disabled, so changes silently stop
     * being journaled.
     */
    protected boolean enabled;
    protected String message;

    public static TriggerCdcObjectState create() {
        return new TriggerCdcObjectState();
    }

    public TriggerCdcObjectType getType() {
        return type;
    }

    public void setType(TriggerCdcObjectType type) {
        this.type = type;
    }

    public TriggerCdcObjectState type(TriggerCdcObjectType type) {
        this.type = type;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public TriggerCdcObjectState schema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TriggerCdcObjectState name(String name) {
        this.name = name;
        return this;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public TriggerCdcObjectState present(boolean present) {
        this.present = present;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public TriggerCdcObjectState enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TriggerCdcObjectState message(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return "TriggerCdcObjectState{type=" + type + ", name=" + name + ", present=" + present
                + ", enabled=" + enabled + ", message=" + message + "}";
    }
}
