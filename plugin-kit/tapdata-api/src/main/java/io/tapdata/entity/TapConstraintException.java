package io.tapdata.entity;

import io.tapdata.entity.schema.TapConstraint;

import java.util.List;

public class TapConstraintException extends RuntimeException {

    private final String tableName;
    private final List<TapConstraint> constraintList;
    private final List<String> sqlList;
    private final List<Throwable> exceptions;

    public TapConstraintException(String tableName, List<TapConstraint> constraintList, List<String> sqlList, List<Throwable> exceptions) {
        this.tableName = tableName;
        this.constraintList = constraintList;
        this.sqlList = sqlList;
        this.exceptions = exceptions;
    }

    public String getTableName() {
        return tableName;
    }

    public List<TapConstraint> getConstraintList() {
        return constraintList;
    }

    public List<String> getSqlList() {
        return sqlList;
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    @Override
    public String getMessage() {
        return "Table: " + tableName + ", Constraints: " + constraintList + ", SQLs: " + sqlList + ", Exceptions: " + exceptions;
    }

}
