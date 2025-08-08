package com.spauter.extra.baseentity.init.relation;

import com.spauter.extra.baseentity.enums.RelationType;
import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;

import java.util.HashMap;
import java.util.Map;

/**
 * 主要存储所有类中的嵌套对象
 * @param srcClassName 源类名
 * @param fieldName 嵌套字段
 * @param relationTableName 被嵌套的表名（当RelationType为List时需要）
 * @param query 关联字段（源类名）
 * @param queryBy 被关联类的字段（默认为被关联类的主键）
 * @param relationType 嵌套对象（）
 */
public record RelationColumn(String srcClassName,String fieldName, String relationTableName, String query, String queryBy,
                             RelationType relationType) {

    private static final Map<String, String> fieldRelationTables = new HashMap<>();

    private static final Map<String, RelationType> relationTypes = new HashMap<>();

    private static final Map<String, String> querys = new HashMap<>();

    private static final Map<String, String> queryBys = new HashMap<>();
    //所有实体和表名字
    private static final Map<String, String> EntityTableName = new HashMap<>();


    public static RelationColumn addRelation(String srcClassName,String fieldName, String relationTableName, String query, String queryBy,
                                             RelationType relationType) {
        String key=srcClassName+"-"+fieldName;
        fieldRelationTables.put(key, relationTableName);
        relationTypes.put(key, relationType);
        querys.put(key, query);
        queryBys.put(key, queryBy);
        return new RelationColumn(srcClassName,fieldName, relationTableName, query, queryBy, relationType);
    }

    public static RelationColumn getRelationColumnByFieldName(String srcClassName,String fieldName) {
        String key=srcClassName+"-"+fieldName;
        String relationTableName = fieldRelationTables.get(key);
        RelationType relationType = relationTypes.get(key);
        String query = querys.get(key);
        String queryBy = queryBys.get(key);
        if(relationTableName==null|| relationType==null || query==null || queryBy ==null){
            return null;
        }
        return new RelationColumn(srcClassName,fieldName, relationTableName, query, queryBy, relationType);
    }

    public static void addEntityTableName(String entityName, String tableName) {
        EntityTableName.put(entityName, tableName);
    }

    public static String getDestEntity(String tableName) {
        return EntityTableName.get(tableName);
    }
}
