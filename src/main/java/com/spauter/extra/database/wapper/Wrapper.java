package com.spauter.extra.database.wapper;

import lombok.Getter;

import java.util.*;

import static com.spauter.extra.baseentity.utils.ValueUtil.safeAddAll;

/**
 * 顶级wrapper
 *
 */
@Getter
public sealed class Wrapper<T> permits QueryWrapper, UpdateWrapper {
    /**
     * 用于拼接
     * in (xx,xxx,xxx)
     */
    private final Map<String, Set<Object>> in = new HashMap<>();
    /**
     * 用于拼接 xxx between ? and ?
     */
    private final Map<String, Set<Object>> between = new HashMap<>();

    /**
     * 用于拼接 xxx=xxx
     */
    private final Map<String, Object> eq = new HashMap<>();

    private final List<String> sqlEnd = new ArrayList<>();

    /**
     * 用于拼接 xxx like ?
     */
    private final Map<String, String> like = new HashMap<>();

    /**
     * 用于拼接 xxx >= ?
     */
    private final Map<String, Object> ge = new HashMap<>();

    /**
     * 用于拼接 xxx > ?
     */
    private final Map<String, Object> gt = new HashMap<>();

    /**
     * 用于拼接 xxx <= ?
     */
    private final Map<String, Object> lt = new HashMap<>();

    /**
     * 用于添加 xxx< xxx
     */
    private final Map<String, Object> le = new HashMap<>();

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
     * 添加小于等于条件
     */
    public void addLe(String key, Object value) {
        le.put(key, value);
    }

    /**
     * 添加小于等于条件
     */
    public void addLt(String key, Object value) {
        lt.put(key, value);
    }

    /**
     * 添加大于条件
     */
    public void addGl(String key, Object value) {
        ge.put(key, value);
    }

    /**
     * 添加大于条件
     */
    public void addGt(String key, Object value) {
        gt.put(key, value);
    }

    /**
     * 添加like条件,用于拼接 xxx like ?
     */
    public void addLike(String key, String value) {
        like.put(key, value);
    }


    public List<Object> getAllParams() {
        var list = new ArrayList<>(eq.values());
        for (Set<?> o : in.values()) {
            list.addAll(o);
        }
        for (Set<?> o : between.values()) {
            list.addAll(o);
        }
        list.addAll(like.values());
        list.addAll(gt.values());
        list.addAll(lt.values());
        list.addAll(ge.values());
        list.addAll(le.values());
        return list;
    }
}
