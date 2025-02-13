package io.tapdata.entity;

import io.tapdata.entity.schema.TapConstraint;

import java.util.ArrayList;
import java.util.List;

public class TapConstraintException extends RuntimeException {

    private final String tableName;
    private List<TapConstraint> constraintList;
    private List<String> sqlList;
    private List<Throwable> exceptions;

    public TapConstraintException(String tableName) {
        this.tableName = tableName;
    }

    public TapConstraintException(String tableName, List<TapConstraint> constraintList, List<String> sqlList, List<Throwable> exceptions) {
        this.tableName = tableName;
        this.constraintList = constraintList;
        this.sqlList = sqlList;
        this.exceptions = exceptions;
    }

    public TapConstraintException addException(TapConstraint constraint, String sql, Throwable e) {
        if (constraintList == null) {
            constraintList = new ArrayList<>();
        }
        constraintList.add(constraint);
        if (sqlList == null) {
            sqlList = new ArrayList<>();
        }
        sqlList.add(sql);
        if (exceptions == null) {
            exceptions = new ArrayList<>();
        }
        exceptions.add(e);
        return this;
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
