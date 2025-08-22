package com.spauter.extra.baseentity.searcher;

import com.spauter.extra.baseentity.enums.RelationType;
import com.spauter.extra.config.SpringContextUtil;
import com.spauter.extra.database.annotations.TableFiled;
import com.spauter.extra.database.annotations.TableId;
import com.spauter.extra.database.annotations.TableName;
import com.spauter.extra.database.annotations.VORelation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 类字段搜索器
 * <p>用于搜索和操作类的字段信息，包括表名、主键、字段映射关系等</p>
 */
@Slf4j
public final class ClassFieldSearcher {

    /**
     * 字段关系映射表：数据库字段名 -> 类字段名
     */
    @Getter
    private final Map<String, String> fieldRelation = new HashMap<>();


    /**
     * 数据库表名
     */
    @Getter
    private String tableName;

    /**
     * 表主键字段名
     */
    @Getter
    private String tablePk;

    /**
     * 目标类对象
     */
    @Getter
    private final Class<?> clazz;

    /**
     * 字段名到Field对象的映射
     */
    @Getter
    private final Map<String, Field> fieldNames = new HashMap<>();

    /**
     * 类搜索器缓存
     */
    private static final Map<String, ClassFieldSearcher> searchers = new HashMap<>();

    /**
     * 构造函数
     *
     * @param clazz 要处理的类对象
     * @throws RuntimeException 如果Spring应用已初始化
     */
    public ClassFieldSearcher(Class<?> clazz) {
        if (SpringContextUtil.isInitialized()) {
            throw new RuntimeException("Do not this after spring application started");
        }
        this.clazz = clazz;
        init();
    }

    public static ClassFieldSearcher getSearcher(Class<?> clazz) {
        return searchers.get(clazz.getName());
    }


    /**
     * 添加类搜索器到缓存
     *
     * @param clazz 要添加的类
     * @return 创建的ClassFieldSearcher实例
     */
    public static ClassFieldSearcher addSearcher(Class<?> clazz) {
        ClassFieldSearcher searcher = new ClassFieldSearcher(clazz);
        searchers.put(clazz.getName(), searcher);
        return searcher;
    }


    public void init() {
        this.initTable();
        this.initField();
    }

    private void initTable() {
        this.tableName = getTableName(this.clazz);
    }

    public static String getTableName(Class<?> clazz) {
        String table_name = clazz.getSimpleName().replaceAll("([A-Z])", "_$1").toLowerCase().substring(1);
        TableName table = clazz.getAnnotation(TableName.class);
        String tableName;
        if (table != null) {
            tableName = Objects.equals(table.value(), "") ? table_name : table.value();
        } else {
            tableName = table_name;
        }

        return tableName;
    }


    private void initField() {
        List<String> pkFields = new ArrayList<>();
        Field[] fields = this.clazz.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.put(field.getName(), field);
            //有VOrelation自动忽略
            VORelation relation = field.getAnnotation(VORelation.class);
            if (relation != null) {
                initVoRelation(field, relation);
                continue;
            }
            TableFiled f = field.getAnnotation(TableFiled.class);
            String lowerCase = field.getName().replaceAll("([A-Z])", "_$1").toLowerCase();
            if (f == null || f.exists()) {
                if (f != null) {
                    String value = f.value();
                    if (value != null && !value.isEmpty()) {
                        this.fieldRelation.put(value, field.getName());
                    } else {
                        this.fieldRelation.put(lowerCase, field.getName());
                    }
                } else {
                    this.fieldRelation.put(lowerCase, field.getName());
                }
                TableId id = field.getAnnotation(TableId.class);
                if (id != null) {
                    pkFields.add(field.getName());
                    this.tablePk = id.value().isEmpty() ? field.getName() : id.value();
                }
                if (this.tablePk == null && field.getName().equalsIgnoreCase("id")) {
                    this.tablePk = "id";
                }
            }
        }
        if (pkFields.size() > 1) {
            throw new IllegalStateException("Multiple @TableId fields found: " + pkFields + " in class " + this.clazz.getName());
        }
    }

    private void initVoRelation(Field field, VORelation relation) {
        String queryBy = relation.queryBy();
        String query = relation.query();
        String relationTable = "";
        if ("".equals(query)) {
            query=tablePk;
        }
        RelationType relationType = relation.relationType();
        if (relationType.equals(RelationType.ONE_TO_MANY) && relation.relationClass() == Object.class) {
            //由于技术限制，不能直接获取泛型的值，因此RelationType为List时请定义表名
            log.error("""
                    Query value is empty when you're using OneToMany
                           public class {}{
                             @VORelation(relationClass= ? )
                              private {} {}
                           }
                    
                    """, clazz.getName(), field.getType().getName(), field.getName());
            throw new IllegalStateException();
        }
        Class<?> destclass;
        switch (relationType){
            case ONE_TO_ONE,MANY_TO_ONE->{
                destclass=field.getType();
            }
            case ONE_TO_MANY -> {
                destclass=relation.relationClass();
            }
            default -> throw new IllegalStateException("Unexpected value: " + relationType);
        }
       if("".equals(queryBy)){
           queryBy=getPkFieldName(destclass);
       }
        RelationColumns.addRelation(clazz.getName(), field.getName(), relationTable, query, queryBy, relationType);
    }


    /**
     * 获取类的主键字段名
     *
     * @param clazz 要处理的类
     * @return 主键字段名
     * @throws IllegalStateException 如果找到多个@TableId注解字段
     */
    public static String getPkFieldName(Class<?> clazz) {
        String tablePk = "";
        List<String> pkFields = new ArrayList<>();
        // 遍历所有字段查找@TableId注解
        for (Field field : clazz.getDeclaredFields()) {
            TableId id = (TableId) field.getAnnotation(TableId.class);
            if (id != null) {
                pkFields.add(field.getName());
                // 使用注解值或转换后的字段名
                String fieldValue = id.value().isEmpty() ?
                        field.getName().replaceAll("([A-Z])", "_$1").toLowerCase() : id.value();
                if (tablePk.isEmpty()) {
                    tablePk = fieldValue;
                }
            }
        }
        if (pkFields.size() > 1) {
            throw new IllegalStateException("Multiple @TableId fields found: " + pkFields + " in class " + clazz.getName());
        } else {
            // 如果没有找到@TableId注解，尝试查找名为"id"的字段
            if (tablePk.isEmpty()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getName().equalsIgnoreCase("id")) {
                        return "id";
                    }
                }
            }
            return tablePk;
        }
    }

    /**
     * 获取字段值
     *
     * @param obj       对象实例
     * @param fieldName 字段名
     * @return 字段值，如果字段不存在返回null
     */
    public Object getValue(Object obj, String fieldName) {
        try {
            Field field = this.getField(fieldName);
            if (field == null) {
                return null;
            } else {
                field.setAccessible(true);
                return field.get(obj);
            }
        } catch (IllegalAccessException e) {
            log.error("get field value fail", e);
            return null;
        }
    }

    /**
     * 获取主键值
     *
     * @param obj 对象实例
     * @return 主键值，如果没有主键返回null
     */
    public Object getPkValue(Object obj) {
        if (this.tablePk == null) {
            log.error("No primary key found in class:{}", this.clazz.getName());
            return null;
        } else {
            return this.getValue(obj, this.tablePk);
        }
    }

    /**
     * 获取字段的setter方法
     *
     * @param fieldName 字段名
     * @return setter方法，如果找不到返回null
     */
    public Method getSetter(String fieldName) {
        Field field = this.getField(fieldName);
        try {
            if (field == null) {
                return null;
            } else if (this.isRecordClass()) {
                log.warn("Record classes do not have setter methods: {}", this.clazz.getName());
                // 对于record类，尝试获取字段同名方法
                String setterName = field.getName();
                return this.clazz.getDeclaredMethod(setterName, field.getType());
            } else {
                if (this.isAbstractClass() || this.isInterface()) {
                    log.debug("Abstract classes and interfaces may not have setter methods: {}", this.clazz.getName());
                }
                // 构造标准的setter方法名
                String var10000 = field.getName().substring(0, 1).toUpperCase();
                String setterName = "set" + var10000 + field.getName().substring(1);
                return this.clazz.getDeclaredMethod(setterName, field.getType());
            }
        } catch (NoSuchMethodException var4) {
            log.error("No setter method for field {} in class {}", fieldName, this.clazz.getName());
            return null;
        }
    }

    public Field getField(String fieldName) {
        Field field = this.fieldNames.get(fieldName);
        if (field == null) {
            log.error("No such field:{} in class:{}", fieldName, this.clazz.getName());
        }
        return field;
    }

    private boolean isRecordClass() {
        return this.clazz.isRecord();
    }

    private boolean isAbstractClass() {
        return Modifier.isAbstract(this.clazz.getModifiers());
    }

    private boolean isInterface() {
        return this.clazz.isInterface();
    }

}
