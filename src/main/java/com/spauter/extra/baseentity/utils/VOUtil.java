package com.spauter.extra.baseentity.utils;

import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;

import java.lang.reflect.Field;
import java.util.*;

import static com.spauter.extra.baseentity.utils.ValueUtil.isBlank;

public class VOUtil {


    /**
     * 获取对象数组中指定字段的值列表
     */
    public static List<Object> getVOValueList(Object[] objects, String fieldName) {
        if (isBlank(objects)) {
            return Collections.emptyList();
        }
        ClassFieldSearcher searcher =ClassFieldSearcher.getSearcher(objects[0].getClass());
        List<Object> list = new ArrayList<>();
        for (Object o : objects) {
            Object value = searcher.getValue(o, fieldName);
            list.add(value);
        }
        return list;
    }

    /**
     * 获取对象数组中的主键值列表
     *
     */
    public static List<Object> getVOPkList(Object[] objects) {
        if (isBlank(objects)) {
            return Collections.emptyList();
        }
        String tablePk=ClassFieldSearcher.getSearcher(objects[0].getClass()).getTablePk();
        return getVOValueList(objects,tablePk);
    }

    /**
     * 通过反射获取对象指定字段的值
     *
     * @param entity 目标对象，不能为null
     * @param fieldName 要获取值的字段名称
     */
    public static Object getAttribute(Object entity, String fieldName) {
        Class<?> clazz = entity.getClass();
        try {
            Field field = ClassFieldSearcher.getSearcher(clazz).getField(fieldName);
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  * 获取实体对象的主键值
     */
    public static Object getPkAttribute(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            ClassFieldSearcher searcher = ClassFieldSearcher.getSearcher(entity.getClass());
            String tablePk = searcher.getTablePk();
            if (tablePk == null) {
                return null;
            }
            return getAttribute(entity, tablePk);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置对象指定字段的值
     *
     */
    public static void setAttribute(Object entity, String fieldName, Object value) {
        Class<?> clazz = entity.getClass();
        try {
            Field field = ClassFieldSearcher.getSearcher(clazz).getField(fieldName);
            field.setAccessible(true);
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过反射设置对象指定字段的值
     */
    public static void setAttribute(Object entity,Field field,Object value){
        try{
            field.setAccessible(true);
            field.set(entity, value);
        }catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 设置实体对象的主键值
     *
     */
    public static void setPkValue(Object entity,Object pkValue){
        if(entity==null){
            return;
        }
        String tablePk=ClassFieldSearcher.getSearcher(entity.getClass()).getTablePk();
        setAttribute(entity,tablePk,pkValue);
    }
}
