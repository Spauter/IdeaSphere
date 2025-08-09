package com.spauter.extra.baseentity.searcher;

import com.spauter.extra.database.annotations.TableFiled;
import com.spauter.extra.database.annotations.TableId;
import com.spauter.extra.database.annotations.TableName;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 类字段搜索器
 */
public final class ClassFieldSearcher {

    private final Logger log = LoggerFactory.getLogger(ClassFieldSearcher.class);
    @Getter
    private final Map<String, String> fieldRelation = new HashMap<>();
    @Getter
    private final TreeSet<String> privateFields = new TreeSet<>();
    @Getter
    private String tableName;
    @Getter
    private String tablePk;
    @Getter
    private final Class<?> clazz;

    public ClassFieldSearcher(Class<?> clazz) {
        this.clazz = clazz;
        init();
    }

    public void init() {
        initTable();
        initField();
    }

    private void initTable() {
        this.tableName = getTableName(clazz);
    }

    /**
     * 获取类对应的数据库表名。
     * <p>
     * 优先使用 {@link TableName} 注解中配置的值，
     * 如果没有注解，则根据类名按照驼峰命名转下划线的方式生成默认表名。
     *
     * @param clazz 实体类 Class 对象
     * @return 数据库表名
     */
    public static String getTableName(Class<?> clazz) {
        // 先根据类名进行驼峰命名法转换为下划线格式
        String table_name = clazz.getSimpleName()
                .replaceAll("([A-Z])", "_$1") // 将每个大写字母替换为下划线 + 小写
                .toLowerCase().substring(1);               // 转换为全小写
        // 从类中获取 @TableName 注解
        TableName table = clazz.getAnnotation(TableName.class);
        String tableName;

        if (table != null) {
            // 如果有注解且 value 不为空，则使用注解值；否则使用默认生成的表名
            tableName = Objects.equals(table.value(), "") ? table_name : table.value();
        } else {
            // 没有注解时，直接使用转换后的表名
            tableName = table_name;
        }

        return tableName;
    }


    /**
     * 获取类中主键字段的数据库列名。
     * <p>
     * 该方法会扫描当前类的所有声明字段，寻找带有 {@link TableId} 注解的字段，
     * 并优先使用注解中定义的 value 值作为数据库列名。如果没有设置 value，则使用
     * 字段名根据驼峰命名法转换为下划线格式的名称。
     * <p>
     * 如果未发现任何带有 {@link TableId} 的字段，但存在名为 "Id" 的字段，也尝试将其作为主键处理。
     *
     * @param clazz 要获取主键字段信息的实体类 Class 对象
     * @return 数据库中对应的主键列名（小写格式），若未找到则返回空字符串
     */
    public static String getPkFiledName(Class<?> clazz) {
        String tablePk = "";
        for (Field field : clazz.getDeclaredFields()) {
            String lowerCase = field.getName().replaceAll("([A-Z])", "_$1").toLowerCase();
            TableId id = field.getAnnotation(TableId.class);
            if (id != null) {
                tablePk = id.value().isEmpty() ? lowerCase : id.value();
            } else {
                if (field.getName().equalsIgnoreCase("Id")) {
                    tablePk = "id";
                }
            }
        }
        return tablePk;
    }

    private void initField() {
        // 获取类的所有字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            TableFiled f = field.getAnnotation(TableFiled.class);
            String lowerCase = field.getName().replaceAll("([A-Z])", "_$1").toLowerCase();
            if (f != null && !f.exists()) {
                continue;
            }
            if (f != null) {
                String value = f.value();
                if (value == null || value.isEmpty()) {
                    fieldRelation.put(lowerCase, field.getName());
                } else {
                    fieldRelation.put(value, field.getName());
                }
            } else {
                //根据驼峰命名法命名
                fieldRelation.put(lowerCase, field.getName());
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
     * 获取字段值
     *
     * @param obj       实体类
     * @param fieldName 字段名
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

    /**
     * 获取主键值
     *
     * @param obj 实体类
     * @return 主键值
     */
    public Object getPkValue(Object obj) {
        return getValue(obj, tablePk);
    }

    /**
     * 获取setter方法
     */
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

    /**
     * 为字段设置值
     *
     * @param obj       实体类
     * @param fieldName 字段名
     * @param value     值
     */
    public void setValue(Object obj, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("set value fail", e);
        }
    }

    /**
     * 设置实体对象的主键值
     *
     * @param obj   要设置主键值的实体对象
     * @param value 要设置的主键值
     */
    public void setPkValue(Object obj, Object value) {
        setValue(obj, tablePk, value);
    }

    public void removeField(String fieldName) {
        privateFields.remove(fieldName);
        fieldRelation.remove(fieldName);
    }

}
