package com.spauter.extra.database.dao;


import com.spauter.extra.config.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.OperationNotSupportedException;
import java.sql.*;
import java.util.*;

public sealed class JdbcTemplate permits JdbcTemplateBatchExecutor {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);


    public JdbcTemplate() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("JdbcTemplate should not be instantiated");
    }


    public static Connection getConnection() {
        try {
            return SpringContextUtil.getBean("conn", Connection.class);
        } catch (Exception e) {
            throw new RuntimeException("create connection fail", e);
        }
    }

    public static Object execute(SqlExecutor executor) throws SQLException {

        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            Object ret = executor.execute(conn);
            conn.commit();
            return ret;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception e1) {
                throw new RuntimeException("roll back fail", e1);
            }
            throw new RuntimeException("sql execute fail because: " + e.getMessage());
        }
    }


    public static interface SqlExecutor {
        Object execute(Connection conn) throws Exception;
    }

    public static int update(String sql, Object... params) throws SQLException {
        return (int) execute(conn -> {
            PreparedStatement ps = prepareStatement(conn, sql, params);
            return ps.executeUpdate();
        });
    }

    public static PreparedStatement prepareStatement(
            Connection conn, String sql, Object... params) throws SQLException {
        System.out.println("params:" + Arrays.toString(params));
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }

    public static PreparedStatement prepareInsertStatement(
            Connection conn, String sql, String keyName, Object... params) throws SQLException {
        System.out.println("params:" + Arrays.toString(params));
        PreparedStatement ps = conn.prepareStatement(sql, new String[]{keyName});
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }

    public static long insert(String sql, String keyName, Object... params) throws SQLException {
        return (long) execute(conn -> {
            PreparedStatement ps = prepareInsertStatement(conn, sql, keyName, params);
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                return rs.getLong(1);
            } else {
                return 0;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> select(String sql, Object... params) throws SQLException {
        return (List<Map<String, Object>>) execute(conn -> {
            log.info("current sql:{}", sql);
            PreparedStatement ps = prepareStatement(conn, sql, params);
            ResultSet rs = ps.executeQuery();
            return transfromRsToList(rs);
        });

    }

    public static List<Map<String, Object>> transfromRsToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 0; i < md.getColumnCount(); i++) {
                String columnName = md.getColumnName(i + 1);
                columnName = columnName.toLowerCase();
                Object columnValue = rs.getObject(i + 1);
                row.put(columnName, columnValue);
            }
            System.out.println(row);
            list.add(row);
        }
        return list;
    }


    @SuppressWarnings("unchecked")
    public static <E> E selectOneColumn(String sql, E e, Object... params) throws SQLException {
        List<Map<String, Object>> list = select(sql, params);
        if (list.size() > 1) {
            throw new SQLException("We need only one row,but we get " + list.size());
        }
        if (list.isEmpty()) {
            return e;
        }
        return (E) list.get(0).values().toArray()[0];
    }
}
