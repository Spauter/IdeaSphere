package com.spauter.extra.database.wapper;

import java.util.*;

/**
 * 顶级wrapper
 * @param <T>
 */
public interface Wrapper<T> {
    //被选中的字段
    List<String>selectedColumns=new ArrayList<>();
    //条件
    Map<String,Object> eq=new HashMap<>();

    List<String> sqlEnd=new ArrayList<>();
    /**
     * 添加条件，用于拼接 where xxx = xxx
     * 如果加多个条件，自动加上 and
     * @param key 字段名
     * @param value 字段值
     */
    void addEq(String key,Object value);

    /**
     *  添加需要查询的字段，不加默认查询所有字段
     * @param column 字段名
     */
    void addSelectedColumns(String column);

    /**
     * 一次性添加需要查询的字段
     */
    void addSelectedColumns(Collection<String> columns);

    /**
     * 用于在sql末尾添加条件，如 order by xxx desc
     * @param sql
     */
    void addSql(String sql);
}
