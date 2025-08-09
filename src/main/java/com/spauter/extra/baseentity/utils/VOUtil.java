package com.spauter.extra.baseentity.utils;

import com.spauter.extra.baseentity.builder.EntityBuilder;
import com.spauter.extra.baseentity.init.relation.RelationColumn;
import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.database.annotations.VORelation;
import com.spauter.extra.database.dao.JdbcTemplate;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;

import static com.spauter.extra.baseentity.utils.ValueUtil.isBlank;

public class VOUtil {


    public static List<Object> getVOValueList(Object[] objects, String fieldName) {
        if (isBlank(objects)) {
            return Collections.emptyList();
        }
        ClassFieldSearcher searcher = new ClassFieldSearcher(objects[0].getClass());
        List<Object> list = new ArrayList<>();
        for (Object o : objects) {
            Object value = searcher.getValue(o, fieldName);
            list.add(value);
        }
        return list;
    }


    public static List<Object> getVOPkList(Object[] objects) {
        if (isBlank(objects)) {
            return Collections.emptyList();
        }
        ClassFieldSearcher searcher = new ClassFieldSearcher(objects[0].getClass());
        List<Object> list = new ArrayList<>();
        for (Object o : objects) {
            Object value = searcher.getPkValue(o);
            list.add(value);
        }
        return list;
    }


    public static void filter(Object entity) throws Exception {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            VORelation relation = field.getAnnotation(VORelation.class);
            if (relation == null) {
                continue;
            }
            field.setAccessible(true);
            var relationColumn = RelationColumn.getRelationColumnByFieldName(clazz.getName(), field.getName());
            if (relationColumn == null) {
                continue;
            }
            handleRelation(entity, field, relationColumn);
        }
    }

    public static Object getAttribute(Object entity, String fieldName) {
        Class<?> clazz = entity.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setAttribute(Object entity, String fieldName, Object value) {
        Class<?> clazz = entity.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleRelation(Object entity, Field field, RelationColumn relationColumn)
            throws SQLException, ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, NoSuchFieldException {
        var sql = "";
        switch (relationColumn.relationType()) {
            case SINGLE -> {
                Class<?> fieldType = field.getType();
                String tableName = new ClassFieldSearcher(fieldType).getTableName();
                sql = "select * from " + tableName + " where " + relationColumn.queryBy() + " = ?";
            }
            case ARRAY -> {
                var destClazz = Class.forName(field.getName().substring(2));
                String tableName = new ClassFieldSearcher(destClazz).getTableName();
                sql = "select * from " + tableName + " where " + relationColumn.queryBy() + " = ?";
            }
            case LIST -> sql = buildQuerySql(relationColumn);
        }
        Object value = getAttribute(entity, relationColumn.query());
        var list = JdbcTemplate.select(sql, value);
        switch (relationColumn.relationType()) {
            case LIST -> handleListRelation(entity, field, relationColumn, list);
            case SINGLE -> handleSingleRelation(entity, field, list);
            case ARRAY -> handleArrayRelation(entity, field, list);
        }
    }

    private static String buildQuerySql(RelationColumn relationColumn) {
        return "select * from " + relationColumn.relationTableName() +
                " where " + relationColumn.queryBy() + " = ?";
    }

    private static void handleListRelation(Object entity, Field field, RelationColumn relationColumn, List<Map<String, Object>> list)
            throws ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, NoSuchFieldException {
        String dest = RelationColumn.getDestEntity(relationColumn.relationTableName());
        Class<?> destClazz = Class.forName(dest);
        ClassFieldSearcher searcher = new ClassFieldSearcher(destClazz);
        var result = new EntityBuilder(searcher).getEntities(list);
        setAttribute(entity, field.getName(), result);
    }

    private static void handleSingleRelation(Object entity, Field field, List<Map<String, Object>> list)
            throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, NoSuchFieldException {
        Class<?> destClazz = field.getType();
        ClassFieldSearcher searcher = new ClassFieldSearcher(destClazz);
        var result = new EntityBuilder(searcher).getEntities(list);
        setAttribute(entity, field.getName(), result.isEmpty() ? null : result.get(0));
    }

    private static void handleArrayRelation(Object entity, Field field, List<Map<String, Object>> list)
            throws ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, NoSuchFieldException {
        String fieldName = field.getName();
        String singleName = fieldName.substring(1);
        var destClazz = Class.forName(singleName);
        ClassFieldSearcher searcher = new ClassFieldSearcher(destClazz);
        var result = new EntityBuilder(searcher).getEntities(list);
        Object[] array = (Object[]) Array.newInstance(destClazz, result.size());
        for (int i = 0; i < result.size(); i++) {
            Array.set(array, i, result.get(i));
        }
        setAttribute(entity, field.getName(), array);
    }

}
