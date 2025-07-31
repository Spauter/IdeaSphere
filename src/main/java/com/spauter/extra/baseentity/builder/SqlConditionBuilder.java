package com.spauter.extra.baseentity.builder;

import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.config.SpringContextUtil;
import com.spauter.extra.database.dao.JdbcTemplate;
import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.extra.database.wapper.UpdateWrapper;
import com.spauter.extra.database.wapper.Wrapper;
import org.ideasphere.ideasphere.DataBase.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqlConditionBuilder<T> extends SQLBuilder {
    public SqlConditionBuilder(ClassFieldSearcher searcher) {
        super(searcher);
    }


    public String getFindListSql(QueryWrapper<T> condition) {
        if (condition == null) {
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
        return "update " + searcher.getTableName() + " set " + generateUpdateColumns(searcher.getFiledRelation().values()) + " where " + searcher.getTablePk() + " = ?";
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
        Object[] eqs = wrapper.getEq().values().toArray();
        Object[] ins = wrapper.getIn().values().toArray(new String[0]);
        Object[] betweens = wrapper.getSelectedColumns().toArray(new String[0]);
        List<Object> list = new ArrayList<>();
        list.addAll(List.of(eqs));
        list.addAll(List.of(ins));
        list.addAll(List.of(betweens));
        return list;
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
                sb.append(" and ").append(key).append(" in (").append("?".repeat(condition.getIn().get(key).size())).append(")");
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
        if (!condition.getSqlEnd().isEmpty()) {
            for (String sqlEnd : condition.getSqlEnd()) {
                sb.append(" ").append(sqlEnd).append(" ");
            }
        }
        return sb.toString();
    }
}
