package com.spauter.extra.database.dao;



import com.spauter.extra.config.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.OperationNotSupportedException;
import java.sql.*;
import java.util.*;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);


    public JdbcTemplate() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("JdbcTemplate should not be instantiated");
    }


    public static Connection getConnection() {
        try {
            return SpringContextUtil.getBean("conn",Connection.class);
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
            throw new RuntimeException("sql execute fail because: "+ e.getMessage());
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
            PreparedStatement ps = prepareStatement(conn, sql, params);
            ResultSet rs = ps.executeQuery();
            return TransfromRsToList(rs);
        });

    }

    public static List<Map<String, Object>> TransfromRsToList(ResultSet rs) throws SQLException {
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
            list.add(row);
        }
        return list;
    }

    @Deprecated(since = "2.0")
    public static Map<String, Object> selectPage(String sql,
                                                 int page, int size, Object... params) throws SQLException {
        Map<String, Object> ret = new HashMap<>();

        int total = 0;
        List<Map<String, Object>> data = null;

        String sql1 = "select * from"
                + "(select a.*, rownum rn from (" + sql + ") a where rownum <= ?) "
                + "where rn >= ?";

        int begin = (page - 1) * size + 1;
        int end = page * size;

        Object[] newParams = new Object[params.length + 2];

        System.arraycopy(params, 0, newParams, 0, params.length);
        newParams[newParams.length - 2] = end;
        newParams[newParams.length - 1] = begin;
        data = JdbcTemplate.select(sql1, newParams);

        String sql2 = "select count(*) cnt from (" + sql + ")";

        List<Map<String, Object>> totalList = JdbcTemplate.select(sql2, params);
        total = Integer.parseInt(totalList.get(0).get("cnt") + "");

        ret.put("total", total);
        ret.put("data", data);
        return ret;
    }
}
