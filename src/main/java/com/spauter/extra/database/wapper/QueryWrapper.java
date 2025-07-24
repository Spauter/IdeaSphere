package com.spauter.extra.database.wapper;

import lombok.Getter;

import java.util.*;

/**
 * 用于select
 * @param <T>
 */
@Getter
public class QueryWrapper<T> implements Wrapper<T>{

    public void addEq(String key,Object value){
        eq.put(key,value);
    }


    public void addSelectedColumns(String column){
        selectedColumns.add(column);
    }


    public void addSelectedColumns(Collection<String> columns){
        selectedColumns.addAll(columns);
    }

    public void addSql(String sql) {
        sqlEnd.add(sql);
    }
}
