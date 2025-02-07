package io.tapdata.entity.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TapForeignKeyConstraint extends TapConstraint<TapForeignKeyConstraint> implements Serializable {

    private List<String> foreignKeyFields;
    private String referencesTableName;
    private List<String> referencesFields;
    private ForeignKeyAction onUpdate;
    private ForeignKeyAction onDelete;

    public TapForeignKeyConstraint foreignKeyField(String foreignKeyField) {
        if (foreignKeyFields == null)
            foreignKeyFields = new ArrayList<>();
        foreignKeyFields.add(foreignKeyField);
        return this;
    }

    public TapForeignKeyConstraint referencesField(String referencesField) {
        if (referencesFields == null)
            referencesFields = new ArrayList<>();
        referencesFields.add(referencesField);
        return this;
    }

    public TapForeignKeyConstraint referencesTable(String referencesTableName) {
        this.referencesTableName = referencesTableName;
        return this;
    }

    public TapForeignKeyConstraint onUpdate(ForeignKeyAction onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    public TapForeignKeyConstraint onDelete(ForeignKeyAction onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public TapForeignKeyConstraint onUpdate(String onUpdate) {
        this.onUpdate = ForeignKeyAction.valueOf(onUpdate.replace(" ", "_").toUpperCase());
        return this;
    }

    public TapForeignKeyConstraint onDelete(String onDelete) {
        this.onDelete = ForeignKeyAction.valueOf(onDelete.replace(" ", "_").toUpperCase());;
        return this;
    }

    public List<String> getForeignKeyFields() {
        return foreignKeyFields;
    }

    public void setForeignKeyFields(List<String> foreignKeyFields) {
        this.foreignKeyFields = foreignKeyFields;
    }

    public String getReferencesTableName() {
        return referencesTableName;
    }

    public void setReferencesTableName(String referencesTableName) {
        this.referencesTableName = referencesTableName;
    }

    public List<String> getReferencesFields() {
        return referencesFields;
    }

    public void setReferencesFields(List<String> referencesFields) {
        this.referencesFields = referencesFields;
    }

    public ForeignKeyAction getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(ForeignKeyAction onUpdate) {
        this.onUpdate = onUpdate;
    }

    public ForeignKeyAction getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(ForeignKeyAction onDelete) {
        this.onDelete = onDelete;
    }

    public enum ForeignKeyAction {
        CASCADE,
        SET_NULL,
        NO_ACTION,
        RESTRICT,
        SET_DEFAULT
    }
}
