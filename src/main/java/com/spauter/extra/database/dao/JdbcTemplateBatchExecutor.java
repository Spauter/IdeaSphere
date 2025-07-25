package com.spauter.extra.database.dao;

import com.spauter.extra.baseentity.builder.SqlConditionBuilder;
import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.database.wapper.UpdateWrapper;

import javax.naming.OperationNotSupportedException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Set;

import static org.ideasphere.ideasphere.IdeaSphereApplication.logger;

public class JdbcTemplateBatchExecutor extends JdbcTemplate {

    public JdbcTemplateBatchExecutor() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("JdbcTemplateBatchExecutor should not be instantiated");
    }


    public static void insert(List<?> entityList, String sql, ClassFieldSearcher searcher) throws SQLException {
        //获取SpringIOC中的Connection
        Connection conn =getConnection();
        conn.setAutoCommit(false); // 关闭自动提交
        PreparedStatement pstmt = conn.prepareStatement(sql);
        String[] fields = searcher.getPrivateFields().toArray(new String[0]);
        for (int i = 0; i < entityList.size(); i++) {
            Object obj = entityList.get(i);
            for (int j = 0; j < fields.length; j++) {
                Object value = searcher.getValue(obj, fields[j]);
                if (value != null) {
                    pstmt.setObject(j + 1, value);
                } else {
                    pstmt.setNull(j + 1, Types.NULL);
                }
            }
            pstmt.addBatch();
            if (i % 1000 == 0) {
                pstmt.executeBatch();
                pstmt.clearBatch();
            }
        }
        finalExecute(conn,pstmt);
    }

    public static void updateBatch(List<?> entities,String sql, UpdateWrapper<?> updateWrapper,ClassFieldSearcher searcher) throws SQLException {
        if(updateWrapper==null){
            updateBatchById(entities,searcher);
            return;
        }
        Connection conn =getConnection();
        Set<String> selectedColumns=updateWrapper.getUpdateColumns().keySet();
        Set<String>whereColumns=updateWrapper.getEq().keySet();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < entities.size(); i++) {
            Object obj = entities.get(i);
            for (int j = 0; j < selectedColumns.size(); j++) {
                Object value = searcher.getValue(obj, selectedColumns.toArray(new String[0])[j]);
                if (value != null) {
                    pstmt.setObject(j + 1, value);
                } else {
                    pstmt.setNull(j + 1, Types.NULL);
                }
            }
            for (int j = 0; j < whereColumns.size(); j++) {
                Object value = searcher.getValue(obj, whereColumns.toArray(new String[0])[j]);
                if (value != null) {
                    pstmt.setObject(j + selectedColumns.size() + 1, value);
                } else {
                    pstmt.setNull(j + selectedColumns.size() + 1, Types.NULL);
                }
            }
            pstmt.addBatch();
            if (i % 1000 == 0) {
                pstmt.executeBatch();
                pstmt.clearBatch();
            }
        }
        finalExecute(conn,pstmt);
    }

    public static void deleteBatch(List<?> entities,String sql,UpdateWrapper<?> updateWrapper,ClassFieldSearcher searcher) throws SQLException {
        if(updateWrapper==null){
            deleteBatchById(entities,searcher);
            return;
        }
        Connection conn =getConnection();
        Set<String>whereColumns=updateWrapper.getEq().keySet();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < entities.size(); i++) {
            Object obj = entities.get(i);
            for (int j = 0; j < whereColumns.size(); j++) {
             try{
                 Object value = searcher.getValue(obj, whereColumns.toArray(new String[0])[j]);
                 if (value != null) {
                     pstmt.setObject(j + 1, value);
                 } else {
                     pstmt.setNull(j + 1, Types.NULL);
                 }
             }catch (Exception e){
                 logger.error("get field value fail",e);
                 logger.warn("current sql",sql);
                 logger.warn("current entity",obj);
                 throw new SQLException(e);
             }
            }
            pstmt.addBatch();
            if (i % 1000 == 0) {
                pstmt.executeBatch();
                pstmt.clearBatch();
            }
        }
        finalExecute(conn,pstmt);
    }


    public static void updateBatchById(List<?> entities,ClassFieldSearcher searcher) throws SQLException {
        String sql=new SqlConditionBuilder<>(searcher).getUpdateByIdSql();
        String[] fields=searcher.getPrivateFields().toArray(new String[0]);
        Connection conn =getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < entities.size(); i++) {
            Object obj = entities.get(i);
            for (int j = 0; j < fields.length; j++) {
                try {
                    Field f = searcher.clazz().getDeclaredField(fields[j]);
                    f.setAccessible(true);
                    Object value=f.get(obj);
                    if (value != null) {
                        pstmt.setObject(j + 1, f.get(obj));
                    } else {
                        pstmt.setNull(j + 1, Types.NULL);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                   logger.error("get field value fail",e);
                   logger.warn("current sql",sql);
                   logger.warn("current entity",obj);
                   throw new SQLException(e);
                }
            }
            pstmt.setObject(fields.length + 1, searcher.getPkValue(obj));
            pstmt.addBatch();
            if (i % 1000 == 0) {
                pstmt.executeBatch();
                pstmt.clearBatch();
            }
        }
      finalExecute(conn,pstmt);
    }

    public static void deleteBatchById(List<?> entities,ClassFieldSearcher searcher) throws SQLException {
        String sql=new SqlConditionBuilder<>(searcher).getDeleteByIdSql();
        Connection conn =getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < entities.size(); i++) {
            Object obj = entities.get(i);
            pstmt.setObject(1, searcher.getPkValue(obj));
            pstmt.addBatch();
            if (i % 1000 == 0) {
                pstmt.executeBatch();
                pstmt.clearBatch();
            }
        }
        finalExecute(conn,pstmt);
    }

    private static void finalExecute(Connection conn, PreparedStatement pstmt) throws SQLException {
        pstmt.executeBatch();
        pstmt.clearBatch();
        conn.commit(); // 提交事务
        conn.setAutoCommit(true); // 恢复自动提交
        pstmt.close();
    }
}
