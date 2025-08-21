package com.spauter.extra.baseentity.builder;


import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.baseentity.searcher.RelationColumns;
import com.spauter.extra.baseentity.utils.ValueUtil;
import com.spauter.extra.database.annotations.VORelation;
import com.spauter.extra.database.dao.JdbcTemplate;
import com.spauter.extra.database.wapper.QueryWrapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.spauter.extra.baseentity.utils.VOUtil.setAttribute;
import static com.spauter.extra.baseentity.utils.ValueUtil.isBlank;
import static com.spauter.extra.baseentity.utils.ValueUtil.safeAddAll;

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
    public <T> T mapRow(ResultSet rs) throws
            SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        var list = JdbcTemplate.transfromRsToList(rs);
        if (list.size() != 1) {
            throw new SQLException("We need only one row,but we get " + list.size());
        }
        return (T) getEntities(list, false).get(0);
    }

    public <T> List<T> mapRows(ResultSet rs)
            throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        var midList = JdbcTemplate.transfromRsToList(rs);
        return getEntities(midList, false);
    }


    /**
     * 将{@code JdbcTemplate.select(String sql)}的返回值转换为实体类<p>
     * 也可以使用其它的list，保证map的key为数据库字段名
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getEntities(List<Map<String, Object>> list, boolean needFilter)
            throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException {
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
            for (String fieldString : RelationColumns.getRelationFieldsBySrcClass(t.getClass().getName())) {
                Field field=searcher.getFieldNames().get(fieldString);
                filterRelationEntity(t,field,needFilter);
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
        } else if (value instanceof Date) {
            return LocalDateTime.ofInstant(
                    ((Date) value).toInstant(),
                    ZoneId.systemDefault()
            );
        }
        throw new IllegalArgumentException("Unsupported time type: " + value.getClass());
    }


    @SuppressWarnings("unchecked")
    private <T, E> void filterRelationEntity(T entity, Field field, boolean needFilter)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, SQLException, NoSuchFieldException {
        if (!needFilter) {
            return;
        }
        RelationColumns relationColumn = RelationColumns.getRelationColumnByFieldName(entity.getClass().getName(), field.getName());
        if (relationColumn == null) {
            return;
        }
        Class<?>destClazz=switch (relationColumn.relationType()){
            case ONE_TO_ONE,MANY_TO_ONE -> field.getType();
            case ONE_TO_MANY -> {
                VORelation voRelation=field.getAnnotation(VORelation.class);
                yield voRelation.relationClass();
            }
        };
        E e = (E)destClazz.getDeclaredConstructor().newInstance();
        var destSearcher = ClassFieldSearcher.getSearcher(e.getClass());
        var query = relationColumn.query();
        var queryBy = relationColumn.queryBy();
        var relationType = relationColumn.relationType();
        var sqlConditionBuilder = new SqlConditionBuilder<E>(destSearcher);
        var wrapper = new QueryWrapper<E>();
        String findSql = "";
        if (queryBy.equalsIgnoreCase(destSearcher.getTableName())) {
            findSql = sqlConditionBuilder.getFindByIdSql();
        } else {
            Object value = searcher.getValue(entity, query);
            wrapper.addEq(queryBy, value);
            findSql = sqlConditionBuilder.getFindListSql(wrapper);
        }
        var list = JdbcTemplate.select(findSql, wrapper.getAllParams().toArray());
        if(isBlank(list)){
            return;
        }
        switch (relationType) {
            case ONE_TO_ONE, MANY_TO_ONE -> {
                e = filerOneEntity(list, field, destSearcher);
                setAttribute(entity,field,e);
            }
            case ONE_TO_MANY -> {
                String fieldType = field.getType().getSimpleName();
                if (fieldType.startsWith("[")) {
                    E[] es = filerMoreEntityArray(list, field, destSearcher);
                    setAttribute(entity,field,es);
                } else {
                    Collection<E> collection = filerMoreEntityCollection(list, field, destSearcher);
                    setAttribute(entity,field,collection);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <E> E filerOneEntity(List<Map<String, Object>> list, Field field, ClassFieldSearcher destSearcher)
            throws SQLException, NoSuchFieldException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (list.size() > 1) {
            throw new SQLException("We need only one row,but we get " + list.size());
        }
        if (isBlank(list)){
            return null;
        }
        return (E) new EntityBuilder(destSearcher).getEntities(list, false).get(0);
    }


    private <E> Collection<E> filerMoreEntityCollection(List<Map<String, Object>> list, Field field, ClassFieldSearcher destSearcher)
            throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException {
        String fieldType = field.getType().getSimpleName();
        Collection<E> result = null;
        switch (fieldType) {
            case "List" -> result = new ArrayList<>();
            case "Set" -> result = new HashSet<>();
            case "Queue" -> result = new LinkedList<>();
        }
        List<E> entities = new EntityBuilder(destSearcher).getEntities(list, false);
        return safeAddAll(result, entities);
    }

    @SuppressWarnings("unchecked")
    private <E> E[] filerMoreEntityArray(List<Map<String, Object>> list, Field field, ClassFieldSearcher destSearcher)
            throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException {
        Collection<E> entities = filerMoreEntityCollection(list, field, destSearcher);
        E[] es = (E[]) Array.newInstance(field.getType(), list.size());
        return entities.toArray(es);
    }
}
