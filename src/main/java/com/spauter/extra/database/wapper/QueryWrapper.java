package com.spauter.extra.database.wapper;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.spauter.extra.baseentity.utils.ValueUtil.safeAddAll;

/**
 * 用于select
 * @param <T>
 */
@Getter
public final class QueryWrapper<T> extends Wrapper<T>{

    /**
     * 用于拼接 select xxx,xxx,xxx<p>
     * 不使用默认select *
     */
    private final List<String> selectedColumns = new ArrayList<>();

    Set<String> sumColumns =new HashSet<>();

    Set<String>groupColumns=new HashSet<>();

    Set<String>countColumns=new HashSet<>();

    /**
     * 添加需要查询的字段，不加默认查询所有字段
     *
     * @param column 字段名
     */
    public void addSelectedColumns(String... column) {
        safeAddAll(selectedColumns,column);
    }


    public void sum(String... columns) {
        safeAddAll(sumColumns, columns);
    }

    public void group(String... columns) {
        safeAddAll(groupColumns, columns);
    }

    public void count(String... columns) {
        safeAddAll(countColumns, columns);
    }
}
