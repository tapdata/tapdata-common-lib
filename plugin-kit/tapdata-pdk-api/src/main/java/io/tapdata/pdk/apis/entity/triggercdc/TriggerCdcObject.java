package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;

/**
 * A database object that a capture owns or touches.
 *
 * <p>{@code ownedByTapdata} is the guard rail for uninstall: the framework may only drop
 * objects it created itself, and must never drop an entire customer schema.</p>
 */
public class TriggerCdcObject implements Serializable {
    private static final long serialVersionUID = 1L;

    protected TriggerCdcObjectType type;
    protected String catalog;
    protected String schema;
    /** Physical object name, as the connector will really create it. */
    protected String name;
    /**
     * True when this object is created and owned by Tapdata, so releasing may drop it.
     *
     * <p>Defaults to {@code false} on purpose: the guard rail has to fail closed. A connector that
     * forgets to set the flag leaves an existing customer object untouchable instead of
     * accidentally licensing a {@code DROP} on it.</p>
     */
    protected boolean ownedByTapdata;
    /** True when the object already exists and will be reused instead of created. */
    protected boolean alreadyExists;
    /** Human readable note, for example why a name differs from the suggested one. */
    protected String comment;

    public static TriggerCdcObject create() {
        return new TriggerCdcObject();
    }

    public TriggerCdcObjectType getType() {
        return type;
    }

    public void setType(TriggerCdcObjectType type) {
        this.type = type;
    }

    public TriggerCdcObject type(TriggerCdcObjectType type) {
        this.type = type;
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public TriggerCdcObject catalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public TriggerCdcObject schema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TriggerCdcObject name(String name) {
        this.name = name;
        return this;
    }

    public boolean isOwnedByTapdata() {
        return ownedByTapdata;
    }

    public void setOwnedByTapdata(boolean ownedByTapdata) {
        this.ownedByTapdata = ownedByTapdata;
    }

    public TriggerCdcObject ownedByTapdata(boolean ownedByTapdata) {
        this.ownedByTapdata = ownedByTapdata;
        return this;
    }

    public boolean isAlreadyExists() {
        return alreadyExists;
    }

    public void setAlreadyExists(boolean alreadyExists) {
        this.alreadyExists = alreadyExists;
    }

    public TriggerCdcObject alreadyExists(boolean alreadyExists) {
        this.alreadyExists = alreadyExists;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TriggerCdcObject comment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public String toString() {
        return "TriggerCdcObject{type=" + type + ", schema=" + schema + ", name=" + name
                + ", ownedByTapdata=" + ownedByTapdata + ", alreadyExists=" + alreadyExists + "}";
    }
}
