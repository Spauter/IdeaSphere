package com.spauter.extra.database.wapper;

import lombok.Getter;

import java.util.*;

/**
 * 顶级wrapper
 *
 * @param <T>
 */
@Getter
public class Wrapper<T> {
    //被选中的字段
    private final List<String> selectedColumns = new ArrayList<>();
    //条件
    private final Map<String, Object> eq = new HashMap<>();

    private final List<String> sqlEnd = new ArrayList<>();

    /**
     * 添加条件，用于拼接 where xxx = xxx
     * 如果加多个条件，自动加上 and
     *
     * @param key   字段名
     * @param value 字段值
     */
    public void addEq(String key, Object value) {
        eq.put(key, value);
    }

    /**
     * 添加需要查询的字段，不加默认查询所有字段
     *
     * @param column 字段名
     */
    public void addSelectedColumns(String column) {
        selectedColumns.add(column);
    }

    /**
     * 一次性添加需要查询的字段
     */
    public void addSelectedColumns(Collection<String> columns) {
        selectedColumns.addAll(columns);
    }

    /**
     * 用于在sql末尾添加条件，如 order by xxx desc
     *
     * @param sql
     */
    public void addSql(String sql) {
        sqlEnd.add(sql);
    }


}
