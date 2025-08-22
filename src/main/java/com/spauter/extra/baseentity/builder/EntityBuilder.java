package com.spauter.extra.baseentity.builder;


import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.baseentity.searcher.RelationColumns;
import com.spauter.extra.baseentity.utils.ValueUtil;
import com.spauter.extra.database.annotations.VORelation;
import com.spauter.extra.database.dao.JdbcTemplate;
import com.spauter.extra.database.mapper.BaseMapper;
import com.spauter.extra.database.service.BaseService;
import com.spauter.extra.database.service.impl.BaseServiceImpl;
import com.spauter.extra.database.wapper.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
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
 * 实体构建器，主要用于构建实体类和嵌套对象处理
 * <p>{@link #mapRows(ResultSet)}、{@link #mapRow(ResultSet)} 将数据库ResultSet数据转换为实体类</p>
 * <p>{@link #getEntities(List, boolean)} 将数据库查询结果（比如jdbcTemplate.select）转化为实体类</p>
 * <p>{@link #filterRelationEntity} 嵌套对象处理，支持集合/数组类型的关联字段</p>
 *
 * @author spauter
 * @version 202507251424
 */
@Slf4j
public class EntityBuilder {
    final ClassFieldSearcher searcher;

    @Resource
    private BaseServiceImpl<?> baseService;

    public EntityBuilder(ClassFieldSearcher searcher) {
        this.searcher = searcher;
    }


    /**
     * 将数据库查询的ResultSet集合转化为实体类
     */
    @SuppressWarnings("unchecked")
    public <T> T mapRow(ResultSet rs) throws
            SQLException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        var list = JdbcTemplate.transfromRsToList(rs);
        if (list.size() != 1) {
            throw new SQLException("We need only one row,but we get " + list.size());
        }
        return (T) getEntities(list, false).get(0);
    }

    /**
     * 将数据库查询的ResultSet集合转化为实体类集合
     */
    public <T> List<T> mapRows(ResultSet rs)
            throws SQLException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        var midList = JdbcTemplate.transfromRsToList(rs);
        return getEntities(midList, false);
    }


    /**
     * 将数据库查询结果列表转换为实体对象列表
     * <p>支持处理普通字段和关联关系字段的自动映射</p>
     *
     * @param list       数据库查询结果列表，每个Map表示一行记录，key为字段名，value为字段值
     * @param needFilter 是否需要处理关联关系字段
     * @param <T>        目标实体类型
     */
    public <T> List<T> getEntities(List<Map<String, Object>> list, boolean needFilter)
            throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, InstantiationException,
            IllegalAccessException, SQLException, ClassNotFoundException {
        var entities = new ArrayList<T>();
        for (Map<String, Object> row : list) {
            T t;
            if (searcher.getClazz().isRecord()) {
                t = createRecordEntity(row);
            } else {
                t = createGeneralEntity(row);
            }
            for (String fieldString : RelationColumns.getRelationFieldsBySrcClass(t.getClass().getName())) {
                Field field = searcher.getFieldNames().get(fieldString);
                filterRelationEntity(t, field, needFilter);
            }
            entities.add(t);
        }
        return entities;
    }

    private <T> T createRecordEntity(Map<String, Object> row) {
        var types = new Class<?>[row.size()];
        var params = new Object[row.size()];
        int index = 0;
        for (String key : row.keySet()) {
            Field field = searcher.getFieldNames().get(searcher.getFieldRelation().get(key));
            field.setAccessible(true);
            Object o = row.get(key);
            types[index] = field.getType();
            params[index] = getValue(searcher.getClazz().getName(), field, o);
            index++;
        }
        return createEntity(types, params);
    }

    private <T> T createGeneralEntity(Map<String, Object> row) throws IllegalAccessException {
        T t = createEntity();
        for (String key : row.keySet()) {
            if (searcher.getFieldRelation().containsKey(key)) {
                Field field = searcher.getFieldNames().get(searcher.getFieldRelation().get(key));
                field.setAccessible(true);
                Object o = row.get(key);
                if (o != null) {
                    Object value = getValue(searcher.getClazz().getName(), field, o);
                    field.set(t, value);
                }
            }
        }
        return t;
    }

    private Object getValue(String className, Field field, Object value) {
        if (value == null) return null;
        Class<?> fieldType = field.getType();
        try {
            return switch (fieldType.getSimpleName()) {
                case "Integer" -> ValueUtil.getIntValue(value);
                case "Long" -> ValueUtil.getLongValue(value);
                case "Double" -> ValueUtil.getDoubleValue(value);
                // 处理时间类型转换
                case "LocalDateTime" -> convertToLocalDateTime(value);
                // 处理字符串类型转换
                case "String" -> value.toString();
                // 其他类型保持原样
                default -> value;
            };
        } catch (Exception e) {
            log.warn("class {} field [{}] type mismatch, expected {} but got {}",
                    className, field.getName(), fieldType, value.getClass());
            return null;
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


    /**
     * 处理实体类的嵌套对象并自动塞值,支持集合/数组类型的关联字段
     *
     * @param <T> 源实体类型
     * @param <E> 目标关联实体类型
     * @see VORelation
     * @see RelationColumns
     */
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
        // 根据关联类型确定目标实体类：
        //一对一/多对一直接使用字段类型
        //一对多需要从@VORelation注解获取实际关联类
        Class<?> destClazz = switch (relationColumn.relationType()) {
            case ONE_TO_ONE, MANY_TO_ONE -> field.getType();
            case ONE_TO_MANY -> {
                VORelation voRelation = field.getAnnotation(VORelation.class);
                yield voRelation.relationClass();
            }
        };
        E e = (E) destClazz.getDeclaredConstructor().newInstance();
        var destSearcher = ClassFieldSearcher.getSearcher(e.getClass());
        // 获取相关查询配置参数
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
        if (isBlank(list)) {
            return;
        }
        switch (relationType) {
            // 处理一对一/多对一关系
            case ONE_TO_ONE, MANY_TO_ONE -> {
                e = filerOneEntity(list, field, destSearcher);
                setAttribute(entity, field, e);
            }
            // 处理一对多关系
            case ONE_TO_MANY -> {
                String fieldType = field.getType().getSimpleName();
                //处理数组
                if (fieldType.contains("[]")) {
                    E[] es = filerMoreEntityArray(list, field, destSearcher);
                    setAttribute(entity, field, es);
                } else {
                    //处理集合
                    Collection<E> collection = filerMoreEntityCollection(list, field, destSearcher);
                    setAttribute(entity, field, collection);
                }
            }
        }
    }

    /**
     * 过滤并获取单个关联实体对象
     * <p>用于处理一对一或多对一 关系，确保结果集中只有一条记录</p>
     *
     * @param list         数据库查询结果集，每个Map表示一行记录
     * @param field        当前实体类中表示关联关系的字段
     * @param destSearcher 目标实体类的字段搜索器
     * @param <E>          目标实体类型参数
     */
    @SuppressWarnings("unchecked")
    private <E> E filerOneEntity(List<Map<String, Object>> list, Field field, ClassFieldSearcher destSearcher)
            throws SQLException, NoSuchFieldException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (list.size() > 1) {
            throw new SQLException("We need only one row,but we get " + list.size());
        }
        if (isBlank(list)) {
            return null;
        }
        return (E) new EntityBuilder(destSearcher).getEntities(list, false).get(0);
    }

    /**
     * 处理并返回集合类型的关联实体
     * <p>用于处理一对多关系中的集合类型字段</p>
     * <ul>
     *   <li>Collection接口类型：自动选择默认实现(List→ArrayList, Set→HashSet, Queue→LinkedList)</li>
     *   <li>数组类型：使用ArrayList暂存,让{@link #filerMoreEntityArray(List, Field, ClassFieldSearcher)}调用</li>
     *   <li>具体集合类型：直接实例化指定的集合类</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private <E> Collection<E> filerMoreEntityCollection(List<Map<String, Object>> list, Field field, ClassFieldSearcher destSearcher)
            throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException {
        String fieldType = field.getType().getSimpleName();
        Collection<E> result = null;
        if (field.getType().isInterface()) {
            switch (fieldType) {
                case "List" -> result = new ArrayList<>();
                case "Set" -> result = new HashSet<>();
                case "Queue" -> result = new LinkedList<>();
            }
        } else if (field.getType().isArray()) {
            result = new ArrayList<>();
        } else {
            try {
                Object o = field.getType().getDeclaredConstructor().newInstance();
                result = (Collection<E>) o;
            } catch (Exception e) {
                throw new InstantiationException("Unsupported collection type: " + field.getType());
            }
        }
        List<E> entities = new EntityBuilder(destSearcher).getEntities(list, false);
        return safeAddAll(result, entities);
    }

    /**
     * 将关联实体集合转换为数组形式
     * <p>用于处理一对多关系中的数组类型字段</p>
     */
    @SuppressWarnings("unchecked")
    private <E> E[]
    filerMoreEntityArray(List<Map<String, Object>> list, Field field, ClassFieldSearcher destSearcher)
            throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, SQLException, ClassNotFoundException {
        Collection<E> entities = filerMoreEntityCollection(list, field, destSearcher);
        Class<?> componentType = field.getType().getComponentType();
        E[] es = (E[]) Array.newInstance(componentType, list.size());
        return entities.toArray(es);
    }


    /**
     * 创建目标实体类的实例（使用无参构造函数）
     * <p>通过反射调用目标类的无参构造函数创建实例</p>
     * <p>如果创建失败会记录错误日志并返回null</p>
     *
     * @return 新创建的实体对象，如果创建失败则返回null
     * @see ClassFieldSearcher#getClazz() 通过searcher获取目标类Class对象
     * @see #createEntity(Class[], Object...) 带参数的实体创建方法
     */
    @SuppressWarnings("unchecked")
    public <T> T createEntity() {
        try {
            Constructor<T> constructor = (Constructor<T>) searcher.getClazz().getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            log.error("Create entity fail", e);
            return null;
        }
    }

    /**
     * 创建带有参数的实体对象实例
     * <p>根据给定的参数类型和参数值，通过反射调用对应的构造函数创建实体对象</p>
     * <p>需要有有参构造</p>
     *
     * @param types  构造函数参数类型数组，如果为空则调用无参构造
     * @param params 构造函数参数值数组，如果为空则调用无参构造
     * @return 创建的实体对象实例，创建失败时返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T createEntity(Class<?>[] types, Object... params) {
        if (isBlank(types) && isBlank(params)) {
            return createEntity();
        }
        if (types.length != params.length) {
            log.error("Create entity fail,params length not match");
            return null;
        }
        try {
            Constructor<T> constructor = (Constructor<T>) searcher.getClazz().getDeclaredConstructor(types);
            constructor.setAccessible(true);
            return constructor.newInstance(params);
        } catch (Exception e) {
            String paramsString = Arrays.toString(params);
            log.error("Create entity fail", e);
            log.error("""
                            You can try:
                            {} t =new {}({})
                            
                            """,
                    searcher.getClazz().getSimpleName(),
                    searcher.getClazz().getSimpleName(),
                    paramsString);
            return null;
        }
    }
}
