package com.spauter.extra.baseentity.searcher;

import com.spauter.extra.baseentity.enums.RelationType;
import com.spauter.extra.config.SpringContextUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.spauter.extra.baseentity.searcher.RelationColumns.RelationCache.*;

/**
 * 主要存储所有类中的嵌套对象
 *
 * @param srcClassName      源类名
 * @param fieldName         嵌套字段
 * @param relationTableName 被嵌套的表名（当RelationType为List时需要）
 * @param query             关联字段（源类名）
 * @param queryBy           被关联类的字段（默认为被关联类的主键）
 * @param relationType      嵌套方式{@link RelationType}
 */
public record RelationColumns(String srcClassName, String fieldName, String relationTableName, String query,
                              String queryBy,
                              RelationType relationType) {


    static final class RelationCache {
        static final Map<String, String> FIELD_RELATION_TABLES = new ConcurrentHashMap<>();
        static final Map<String, RelationType> RELATION_TYPES = new ConcurrentHashMap<>();
        static final Map<String, String> QUERIES = new ConcurrentHashMap<>();
        static final Map<String, String> QUERY_BYS = new ConcurrentHashMap<>();
        static final Map<String, Set<String>> ENTITY_FIELDS = new ConcurrentHashMap<>();
    }

    /**
     * 防止启动后修改关联关系
     */
    private static void checkInitialization() {
        if (SpringContextUtil.isInitialized()) {
            throw new IllegalStateException("Cannot modify relations after application startup");
        }
    }

    /**
     * 添加实体类字段关联关系
     *
     * @param srcClassName      源类名
     * @param fieldName         关联字段名
     * @param relationTableName 关联表名（当RelationType为List时需要）
     * @param query             关联字段（源类名）
     * @param queryBy           被关联类的字段（默认为被关联类的主键）
     * @param relationType      关联类型 {@link RelationType}
     * @return 创建的关联关系对象
     * @throws IllegalStateException 如果应用已启动后尝试修改关联关系
     */
    public static RelationColumns addRelation(String srcClassName, String fieldName, String relationTableName, String query, String queryBy,
                                              RelationType relationType) {
        checkInitialization();
        String key = srcClassName + "-" + fieldName;
        FIELD_RELATION_TABLES.put(key, relationTableName);
        RELATION_TYPES.put(key, relationType);
        QUERIES.put(key, query);
        QUERY_BYS.put(key, queryBy);
        Set<String> relationFields = ENTITY_FIELDS.getOrDefault(srcClassName, new HashSet<>());
        relationFields.add(fieldName);
        ENTITY_FIELDS.put(srcClassName, relationFields);
        return new RelationColumns(srcClassName, fieldName, relationTableName, query, queryBy, relationType);
    }

    /**
     * 根据源类名和字段名获取关联列信息
     *
     * @param srcClassName 源类名（包含关联关系的类）
     * @param fieldName 关联字段名
     * @return RelationColumns对象，包含完整的关联关系信息；如果找不到对应关系则返回null
     */
    public static RelationColumns getRelationColumnByFieldName(String srcClassName, String fieldName) {
        String key = srcClassName + "-" + fieldName;
        String relationTableName = FIELD_RELATION_TABLES.get(key);
        RelationType relationType = RELATION_TYPES.get(key);
        String query = QUERIES.get(key);
        String queryBy = QUERY_BYS.get(key);
        if (relationTableName == null || relationType == null || query == null || queryBy == null) {
            return null;
        }
        return new RelationColumns(srcClassName, fieldName, relationTableName, query, queryBy, relationType);
    }

    /**
     * 根据源类名获取该类的所有关联字段集合
     *
     * @param srcClassName 源类名（包含关联关系的类）
     * @return 返回该类的所有关联字段名称集合，如果没有关联字段则返回空集合（不会返回null）
     */
    public static Set<String> getRelationFieldsBySrcClass(String srcClassName) {
        return ENTITY_FIELDS.getOrDefault(srcClassName, new HashSet<>());
    }
}
