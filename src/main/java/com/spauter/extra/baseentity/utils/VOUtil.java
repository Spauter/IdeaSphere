package com.spauter.extra.baseentity.utils;

import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;

import java.lang.reflect.Field;
import java.util.*;

import static com.spauter.extra.baseentity.utils.ValueUtil.isBlank;

public class VOUtil {


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


    public static List<Object> getVOPkList(Object[] objects) {
        if (isBlank(objects)) {
            return Collections.emptyList();
        }
        String tablePk=ClassFieldSearcher.getPkFieldName(objects[0].getClass());
        return getVOValueList(objects,tablePk);
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


    public static void setAttribute(Object entity,Field field,Object value){
        try{
            field.setAccessible(true);
            field.set(entity, value);
        }catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
