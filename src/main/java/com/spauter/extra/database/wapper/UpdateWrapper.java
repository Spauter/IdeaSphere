package com.spauter.extra.database.wapper;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     */
    public void addUpdateColumn(String column, Object value) {
        updateColumns.put(column, value);
    }


    @Override
    public List<Object> getAllParams() {
        var list = new ArrayList<>(updateColumns.values());
        list.addAll(super.getAllParams());
        return list;
    }
}
