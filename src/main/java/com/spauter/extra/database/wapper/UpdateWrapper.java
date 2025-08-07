package com.spauter.extra.database.wapper;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于update和delete
 */
@Getter
public final class UpdateWrapper<T> extends Wrapper<T> {

    private final Map<String, Object> updateColumns = new HashMap<>();

    /**
     * 添加需要更新的字段,用于拼接 set xxx = xxx
     *
     * @param column
     * @param value
     */
    public void addUpdateColumn(String column, Object value) {
        updateColumns.put(column, value);
    }
}
