package com.spauter.extra.baseentity.builder;

import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.config.SpringContextUtil;
import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.extra.database.wapper.UpdateWrapper;
import com.spauter.extra.database.wapper.Wrapper;
import org.ideasphere.ideasphere.DataBase.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.spauter.extra.baseentity.utils.ValueUtil.isBlank;

/**
 * 带占位符的sql生成器
 */
public class SqlConditionBuilder<T> extends SQLBuilder {
    public SqlConditionBuilder(ClassFieldSearcher searcher) {
        super(searcher);
    }


    /**
     * 生成查询sql
     *
     * @param condition
     * @return
     */
    public String getFindListSql(QueryWrapper<T> condition) {
        if (condition == null) {
            return getFIndAllSql();
        }
        StringBuilder sb = new StringBuilder("select ");
        if (!isBlank(condition.getSelectedColumns())) {
            sb.append(generateSelectColumns(condition));
            sb.append(",");
        } else if(!isBlank(condition.getCountColumns())){
           sb.append(generateCountColumns(condition));
            sb.append(",");
        }else if(!isBlank(condition.getSumColumns())){
            sb.append(generateSumColumns(condition));
            sb.append(",");
        }else {
            sb.append("*,");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" from ").append(searcher.getTableName());
        sb.append(generateSqlWhere(condition));
        if(!isBlank(condition.getGroupColumns())){
            sb.append(generateGroupByColumns(condition));
        }
        return sb.toString();
    }


    public String getFindByPageSql(QueryWrapper<T> condition, int pageNo, int pageSize, String orderBy) throws SQLException {
        if (condition == null) {
            throw new IllegalArgumentException("entity and condition can not be null");
        }
        Database database = SpringContextUtil.getBean("database", Database.class);
        String dbType = database.getDbType();
        String baseSql = "select * from (" + getFindListSql(condition) + ") order by " + orderBy;
        return generatePageSql(baseSql, pageNo, pageSize, dbType);
    }

    public String generatePageSql(String baseSql, int pageNo, int pageSize, String dbType) {
        return switch (dbType) {
            //todo 使用基于游标的分页
            case "mysql", "mariadb" -> baseSql + " limit ? , ?";
            case "postgresql", "sqlite" -> baseSql + " LIMIT ? OFFSET ?";
            //预留,虽然用不上
            case "oracle" -> {
                String sqlq1 = """
                        select * from
                        (select a.*, rownum rn from (" + %s + ") a where rownum <= ?) "
                        where rn >= ?;
                        """;
                int begin = (pageNo - 1) * pageSize + 1;
                int end = pageNo * pageSize;
                pageSize = end;
                pageNo = begin;
                yield String.format(sqlq1, baseSql);
            }
            default -> throw new IllegalStateException("Unexpected value: " + dbType);
        };
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
        return "update " + searcher.getTableName() + " set " + generateUpdateColumns(searcher.getFieldRelation().values()) + " where " + searcher.getTablePk() + " = ?";
    }

    public String getUpdateSql(UpdateWrapper<T> updateWrapper) {
        if (updateWrapper == null || updateWrapper.getUpdateColumns().isEmpty()) {
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

    private String generateCountColumns(QueryWrapper<T> condition) {
        StringBuilder s = new StringBuilder();
        for (String column : condition.getCountColumns()) {
            s.append("count(").append(column).append("),");
        }
        return s.substring(0, s.length() - 1);
    }

    private String generateSumColumns(QueryWrapper<T> condition) {
        StringBuilder s = new StringBuilder();
        for (String column : condition.getSumColumns()) {
            s.append("sum(").append(column).append("),");
        }
        return s.substring(0, s.length() - 1);
    }

    private String generateGroupByColumns(QueryWrapper<T> condition) {
        StringBuilder s = new StringBuilder(" group by(");
        for (String column : condition.getGroupColumns()) {
            s.append(column).append(",");
        }
        s.deleteCharAt(s.length() - 1);
        s.append(")");
        return s.toString();
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


    public List<Object> generateWhereParams(Wrapper wrapper) {
        return wrapper.getAllParams();
    }

    public List<Object> generateSetParams(UpdateWrapper updateWrapper) {
        return new ArrayList<Object>(updateWrapper.getUpdateColumns().values());
    }

    /**
     * 生成where条件
     */
    private String generateSqlWhere(Wrapper<T> condition) {
        if (condition == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" where ");
        // xxx = ?
        if (!condition.getEq().isEmpty()) {
            sb.append(generateWhereColumns(condition.getEq().keySet()));
        }
        // xxx in (?,?,?)
        if (!condition.getIn().isEmpty()) {
            for (String key : condition.getIn().keySet()) {
                sb.append(" and ").append(key).append(" in (").append("?,".repeat(condition.getIn().get(key).size())).deleteCharAt(sb.length() - 1).append(")");
            }
        }
        // xxx between ? and ?
        if (!condition.getBetween().isEmpty()) {
            for (String key : condition.getBetween().keySet()) {
                sb.append(" and ").append(key).append(" between ? and ?");
            }
        }
        if (!condition.getLike().isEmpty()) {
            for (String key : condition.getLike().keySet()) {
                sb.append(" and ").append(key).append(" like ? ");
            }
        }
        if (!condition.getGt().isEmpty()) {
            for (String key : condition.getGt().keySet()) {
                sb.append(" and ").append(key).append(" > ? ");
            }
        }
        if (!condition.getLt().isEmpty()) {
            for (String key : condition.getLt().keySet()) {
                sb.append(" and ").append(key).append(" < ? ");
            }
        }
        if (!condition.getGe().isEmpty()) {
            for (String key : condition.getGe().keySet()) {
                sb.append(" and ").append(key).append(" >= ? ");
            }
        }
        if (!condition.getLe().isEmpty()) {
            for (String key : condition.getLe().keySet()) {
                sb.append(" and ").append(key).append(" <= ? ");
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
