package com.spauter.extra.database.wapper;

import lombok.Getter;

import java.util.*;

/**
 * 顶级wrapper
 *
 * @param <T>
 */
@Getter
public sealed class Wrapper<T> permits QueryWrapper, UpdateWrapper {
    private final Map<String, Set<Object>> in = new HashMap<>();
    private final Map<String, Set<Object>> between = new HashMap<>();
    //被选中的字段
    private final List<String> selectedColumns = new ArrayList<>();
    //条件
    private final Map<String, Object> eq = new HashMap<>();

    private final List<String> sqlEnd = new ArrayList<>();

    private final Map<String,String>like=new HashMap<>();
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

    /**
     * 添加in条件,用于拼接 xxx in (?,?,?)
     */
    public void addIn(String key, Object... values) {
        Set<Object> set = in.getOrDefault(key, new HashSet<>());
        set.addAll(Arrays.asList(values));
        in.put(key, set);
    }

    /**
     * 添加between条件,用于拼接 xxx between ? and ?
     */
    public void addBetween(String key, Object start, Object end) {
        Set<Object> set = between.getOrDefault(key, new HashSet<>());
        set.add(start);
        set.add(end);
        between.put(key, set);
    }

    /**
     * 添加like条件,用于拼接 xxx like ?
     */
    public void addLike(String key,String value){
        like.put(key,value);
    }
}
