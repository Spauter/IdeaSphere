package com.spauter.extra.baseentity.searcher;

import com.spauter.extra.baseentity.annotation.TableFiled;
import com.spauter.extra.baseentity.annotation.TableId;
import com.spauter.extra.baseentity.annotation.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 类字段搜索器
 */
public record ClassFieldSearcher(Class<?> clazz) {
    private static final Logger log = LoggerFactory.getLogger(ClassFieldSearcher.class);
    private static final Map<String, String> filedRelation = new HashMap<>();
    private static final TreeSet<String> privateFields = new TreeSet<>();
    private static String tableName;
    private static String tablePk;

    public void init() {
        // 获取类的注解
        TableName table = clazz.getAnnotation(TableName.class);
        //如果table为null,则将tablename设置为这个class的名字并根据驼峰命名法命名
        String table_name = clazz.getSimpleName().replaceAll("([A-Z])", "_$1").toLowerCase();
        table_name = table_name.substring(1);
        if (table != null) {
            tableName = Objects.equals(table.value(), "") ? table_name : table.value();
        } else {
            tableName = table_name;
        }
        // 获取类的所有字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            TableFiled f = field.getAnnotation(TableFiled.class);
            if (f != null) {
                filedRelation.put(f.value(), field.getName());
            } else {
                //根据驼峰命名法命名
                String name = field.getName().replaceAll("([A-Z])", "_$1").toLowerCase();
                filedRelation.put(name, field.getName());
            }
            TableId id = field.getAnnotation(TableId.class);
            if (id != null) {
                tablePk = field.getName();
            } else {
                if (field.getName().equalsIgnoreCase("Id")) {
                    tablePk = "id";
                }
            }
            privateFields.add(field.getName());
        }
    }

    /**
     * 获取表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 获取表主键
     */
    public String getTablePk() {
        return tablePk;
    }

    public Map<String, String> getFiledRelation() {
        return filedRelation;
    }

    /**
     * 获取字段值
     */
    public Object getValue(Object obj, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("get field value fail", e);
        }
        return null;
    }

    public Object getPkValue(Object obj) {
        return getValue(obj, tablePk);
    }

    public Method getSetter(String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            return clazz.getDeclaredMethod(setterName, field.getType());
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            log.error("get setter method fail", e);
        }
        return null;
    }

    public void setValue(Object obj, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("set value fail", e);
        }
    }

    public void removeField(String fieldName) {
        privateFields.remove(fieldName);
        filedRelation.remove(fieldName);
    }

    public Set<String> getPrivateFields(){
        return privateFields;
    }
}
