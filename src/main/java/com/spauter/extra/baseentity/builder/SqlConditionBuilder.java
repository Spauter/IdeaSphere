package com.spauter.extra.baseentity.builder;

import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.extra.database.wapper.UpdateWrapper;
import com.spauter.extra.database.wapper.Wrapper;

import java.util.Collection;

public class SqlConditionBuilder<T> extends SQLBuilder {
    public SqlConditionBuilder(ClassFieldSearcher searcher) {
        super(searcher);
    }

    public String getFindListSql(QueryWrapper<T> condition) {
        if(condition==null){
            return getFIndAllSql();
        }
        StringBuilder sb = new StringBuilder("select ");
        if (!condition.getSelectedColumns().isEmpty()) {
            sb.append(generateSelectColumns(condition.getSelectedColumns()));
        } else {
            sb.append(" * ");
        }
        sb.append(" from ").append(searcher.getTableName());
        sb.append(generateSqlWhere(condition));
        return sb.toString();
    }


    public String getFindByPageSql(QueryWrapper<T> condition, int page, int size) {
        //todo
        return "";
    }

    public String getUpdateSql(QueryWrapper<T> condition) {
        if (condition == null) {
            throw new IllegalArgumentException("entity and condition can not be null");
        }
        StringBuilder sb = new StringBuilder("update ").append(searcher.getTableName()).append(" set ");
        sb.append(generateUpdateColumns(condition.getEq().keySet()));
        sb.append(generateSqlWhere(condition));
        return sb.toString();
    }

    public String getUpdateByIdSql() {
        return "update " + searcher.getTableName() + " set " + generateUpdateColumns(searcher.getFiledRelation().values()) + " where " + searcher.getTablePk() + " = ?";
    }

    public String getUpdateSql(UpdateWrapper<T> updateWrapper) {
        if (updateWrapper == null) {
            throw new IllegalArgumentException("Update condition can not be null");
        }
        StringBuilder sb = new StringBuilder("update ").append(searcher.getTableName()).append(" set ");
        sb.append(generateUpdateColumns(updateWrapper.getUpdateColumns().keySet()));
        sb.append(generateSqlWhere(updateWrapper));
        return sb.toString();
    }

    public String getDeleteByIdSql() {
        return "delete from " + searcher.getTableName() + " where " + searcher.getTablePk() + " = ?";
    }

    public String getDeleteSql(UpdateWrapper<T> condition) {
        if (condition == null) {
            throw new IllegalArgumentException("condition can not be null");
        }
        StringBuilder sb = new StringBuilder("delete from ").append(searcher.getTableName());
        sb.append(generateSqlWhere(condition));
        return sb.toString();
    }

    private String generateUpdateColumns(Collection<String> columns) {
        StringBuilder s = new StringBuilder();
        for (String column : columns) {
            s.append(column).append("= ?,");
        }
        return s.substring(0, s.length() - 1);
    }

    private String generateSelectColumns(Collection<String> columns) {
        StringBuilder s = new StringBuilder();
        for (String column : columns) {
            s.append(column).append(",");
        }
        return s.substring(0, s.length() - 1);
    }

    private String generateWhereColumns(Collection<String> columns) {
        StringBuilder s = new StringBuilder();
        for (String column : columns) {
            s.append(column).append("= ? and ");
        }
        return s.substring(0, s.length() - 4);
    }


    public String generateSelectColumns(QueryWrapper<T> condition) {
        if (condition == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (!condition.getSelectedColumns().isEmpty()) {
            sb.append(generateSelectColumns(condition.getSelectedColumns()));
        } else {
            sb.append(" * ");
        }
        return sb.toString();
    }


    /**
     * 生成where条件
     */
    public String generateSqlWhere(Wrapper<T> condition){
        if(condition==null){
            return "";
        }
        StringBuilder sb = new StringBuilder(" where ");
        if (!condition.getEq().isEmpty()) {
            sb.append(generateWhereColumns(condition.getEq().keySet()));
        }
        if (!condition.getIn().isEmpty()) {
            for (String key : condition.getIn().keySet()) {
                sb.append(" and ").append(key).append(" in (").append("?".repeat(condition.getIn().get(key).size())).append(")");
            }
        }
        if(!condition.getBetween().isEmpty()){
            for (String key : condition.getBetween().keySet()) {
                sb.append(" and ").append(key).append(" between ? and ?");
            }
        }
        if(!condition.getLike().isEmpty()){
            for (String key : condition.getLike().keySet()) {
                sb.append(" and ").append(key).append(" like ? ");
            }
        }
        if (!condition.getSqlEnd().isEmpty()) {
            for (String sqlEnd : condition.getSqlEnd()) {
                sb.append(" ").append(sqlEnd).append(" ");
            }
        }
        return sb.toString();
    }
}
