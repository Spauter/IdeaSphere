package com.spauter.extra.baseentity.builder;


import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.baseentity.utils.ValueUtil;
import com.spauter.extra.database.dao.JdbcTemplate;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ideasphere.ideasphere.IdeaSphereApplication.logger;

/**
 * 将数据库查询结果转换为实体类
 *
 * @author spauter
 * @version 202507251424
 */
@Slf4j
public class EntityBuilder {
    final ClassFieldSearcher searcher;

    public EntityBuilder(ClassFieldSearcher searcher) {
        this.searcher = searcher;
    }

    @SuppressWarnings("unchecked")
    public <T> T mapRow(ResultSet rs) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        var list = JdbcTemplate.TransfromRsToList(rs);
        if (list.size() != 1) {
            throw new SQLException("We need only one row,but we get " + list.size());
        }
        return (T) getEntities(list).get(0);
    }

    public <T> List<T> mapRows(ResultSet rs) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        var midList = JdbcTemplate.TransfromRsToList(rs);
        return getEntities(midList);
    }


    /**
     * 将{@code JdbcTemplate.select(String sql)}的返回值转换为实体类<p>
     * 也可以使用其它的list，保证map的key为数据库字段名
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getEntities(List<Map<String, Object>> list) throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var entities = new ArrayList<T>();
        for (Map<String, Object> row : list) {
            T t = (T) searcher.getClazz().getDeclaredConstructor().newInstance();
            for (String key : row.keySet()) {
                if (searcher.getFieldRelation().containsKey(key)) {
                    Field field = searcher.getFieldNames().get(searcher.getFieldRelation().get(key));
                    field.setAccessible(true);
                    Object o = row.get(key);
                    if (o != null) {
                        setValue(t, field, o);
                    }
                }
            }
            entities.add(t);
        }
        return entities;
    }


    private void setValue(Object entity, Field field, Object value) {
        if (value == null) return;
        Class<?> fieldType = field.getType();
        try {
            switch (fieldType.getSimpleName()) {
                case "Integer" -> field.set(entity, ValueUtil.getIntValue(value));
                case "Long" -> field.set(entity, ValueUtil.getLongValue(value));
                case "Double" -> field.set(entity, ValueUtil.getDoubleValue(value));
                // 处理时间类型转换
                case "LocalDateTime" -> field.set(entity, convertToLocalDateTime(value));
                // 处理字符串类型转换
                case "String" -> field.set(entity, value.toString());
                // 其他类型保持原样
                default -> field.set(entity, value);
            }
        } catch (Exception e) {
            log.warn("Field [{}] type mismatch, expected {} but got {}",
                    field.getName(), fieldType, value.getClass());
        }
    }

    /**
     * 统一时间类型转换
     */
    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        } else if (value instanceof String) {
            return LocalDateTime.parse((String) value);
        } else if (value instanceof java.util.Date) {
            return LocalDateTime.ofInstant(
                    ((java.util.Date) value).toInstant(),
                    ZoneId.systemDefault()
            );
        }
        throw new IllegalArgumentException("Unsupported time type: " + value.getClass());
    }
}
