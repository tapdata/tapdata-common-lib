package io.tapdata.entity.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TapConstraint implements Serializable {

    private String name;
    private ConstraintType type;
    private String referencesTableName;
    private List<TapConstraintMapping> mappingFields;
    private ForeignKeyAction onUpdate;
    private ForeignKeyAction onDelete;

    public TapConstraint() {

    }

    public TapConstraint(String name, ConstraintType type) {
        this.name = name;
        this.type = type;
    }

    public TapConstraint name(String name) {
        this.name = name;
        return this;
    }

    public TapConstraint type(ConstraintType type) {
        this.type = type;
        return this;
    }

    public TapConstraint referencesTable(String referencesTableName) {
        this.referencesTableName = referencesTableName;
        return this;
    }

    public TapConstraint onUpdate(ForeignKeyAction onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    public TapConstraint onDelete(ForeignKeyAction onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public TapConstraint onUpdate(String onUpdate) {
        this.onUpdate = ForeignKeyAction.valueOf(onUpdate.replace(" ", "_").toUpperCase());
        return this;
    }

    public TapConstraint onDelete(String onDelete) {
        this.onDelete = ForeignKeyAction.valueOf(onDelete.replace(" ", "_").toUpperCase());
        ;
        return this;
    }

    public TapConstraint add(TapConstraintMapping mappingField) {
        if (mappingFields == null)
            mappingFields = new ArrayList<>();
        mappingFields.add(mappingField);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConstraintType getType() {
        return type;
    }

    public void setType(ConstraintType type) {
        this.type = type;
    }

    public String getReferencesTableName() {
        return referencesTableName;
    }

    public void setReferencesTableName(String referencesTableName) {
        this.referencesTableName = referencesTableName;
    }

    public List<TapConstraintMapping> getMappingFields() {
        return mappingFields;
    }

    public void setMappingFields(List<TapConstraintMapping> mappingFields) {
        this.mappingFields = mappingFields;
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

    public enum ConstraintType {
        PRIMARY_KEY,
        UNIQUE,
        FOREIGN_KEY,
        CHECK
    }

    public enum ForeignKeyAction {
        CASCADE,
        SET_NULL,
        NO_ACTION,
        RESTRICT,
        SET_DEFAULT
    }
}
