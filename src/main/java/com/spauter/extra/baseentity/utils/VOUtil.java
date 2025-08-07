package com.spauter.extra.baseentity.utils;

import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;

import java.util.*;

public class VOUtil {


    public static List<Object> getVOValueList(Object[] objects, String fieldName) {
        if (ValueUtil.isBlank(objects)) {
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
        if (ValueUtil.isBlank(objects)) {
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
}
