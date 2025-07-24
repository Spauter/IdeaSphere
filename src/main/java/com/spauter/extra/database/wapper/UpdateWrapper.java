package com.spauter.extra.database.wapper;

import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于update和delete
 */
@Getter
public class UpdateWrapper<T> implements Wrapper<T> {

    Map<String, Object>updateColumns = new HashMap<>();

    /**
     *  添加需要更新的字段,用于拼接 set xxx = xxx
     * @param column
     * @param value
     */
    public void addUpdateColumn(String column, Object value) {
        updateColumns.put(column, value);
    }

    public void addEq(String column, Object value) {
        eq.put(column, value);
    }

    @Override
    public void addSelectedColumns(String column) {

    }

    @Override
    public void addSelectedColumns(Collection<String> columns) {

    }

    @Override
    public void addSql(String sql) {

    }
}
