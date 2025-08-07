package com.spauter.extra.database.dao;

import com.spauter.extra.database.annotations.TableId;
import com.spauter.extra.baseentity.builder.SqlConditionBuilder;
import com.spauter.extra.baseentity.builder.TablePkGenerator;
import com.spauter.extra.baseentity.enums.IdType;
import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.database.wapper.UpdateWrapper;

import javax.naming.OperationNotSupportedException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.ideasphere.ideasphere.IdeaSphereApplication.logger;

public class JdbcTemplateBatchExecutor extends JdbcTemplate {

    public JdbcTemplateBatchExecutor() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("JdbcTemplateBatchExecutor should not be instantiated");
    }


    public static int insert(List<?> entityList, String sql, ClassFieldSearcher searcher) throws SQLException {
        //获取SpringIOC中的Connection
        Connection conn = getConnection();
        conn.setAutoCommit(false); // 关闭自动提交
        PreparedStatement pstmt = conn.prepareStatement(sql);
        String[] fields =searcher.getFiledRelation().keySet().toArray(new String[0]);
        for (int i = 0; i < entityList.size(); i++) {
            Object obj = entityList.get(i);
            for (int j = 0; j < fields.length; j++) {
                String fieldName=searcher.getFiledRelation().get(fields[j]);
                Object value = searcher.getValue(obj,fieldName);
                if (value != null) {
                    pstmt.setObject(j + 1, value);
                } else {
                    setPkValue(fieldName, searcher, j, pstmt);
                }
            }
            pstmt.addBatch();
            if (i % 1000 == 0) {
                pstmt.executeBatch();
                pstmt.clearBatch();
            }
        }
        finalExecute(conn, pstmt);
        return entityList.size();
    }

    private static void setPkValue(String field, ClassFieldSearcher searcher, int index, PreparedStatement pstmt) {
        try {
            if(!field.equals(searcher.getTablePk())){
                pstmt.setNull(index + 1, Types.NULL);
                return;
            }
            Field f = searcher.getClazz().getDeclaredField(field);
            TableId id = f.getAnnotation(TableId.class);
            if (id != null) {
                IdType idType = id.idType();
                switch (idType) {
                    case UUID -> pstmt.setObject(index + 1, TablePkGenerator.generateIdByUUID());
                    case AUTO_INCREMENT ->
                            pstmt.setObject(index + 1, TablePkGenerator.generateIdByAutoIncrement(searcher));
                }
                return;
            }
            if (field.equalsIgnoreCase("Id")) {
                pstmt.setObject(index + 1, TablePkGenerator.generateIdByAutoIncrement(searcher));
                return;
            }
            pstmt.setNull(index + 1, Types.NULL);
        } catch (NoSuchFieldException | SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void placeholderUpdateValues(ClassFieldSearcher searcher,UpdateWrapper<?> updateWrapper,int continueIndex,PreparedStatement psmt) throws SQLException {
        List<Object>setValues=new SqlConditionBuilder<>(searcher).generateSetParams(updateWrapper);
        for(Object o:setValues){
            setObject(continueIndex,o,psmt);
            continueIndex++;
        }
        placeholderWhereValues(searcher,updateWrapper,continueIndex,psmt);
    }

    private void placeholderWhereValues(ClassFieldSearcher searcher,UpdateWrapper<?> updateWrapper,int continueIndex,PreparedStatement psmt) throws SQLException {
        List<Object>whereValues=new SqlConditionBuilder<>(searcher).generateWhereParams(updateWrapper);
        for(Object o:whereValues){
            setObject(continueIndex,o,psmt);
            continueIndex++;
        }
    }

    public void setObject(int index,Object o,PreparedStatement psmt) throws SQLException {
        if(o!=null){
            psmt.setObject(index+1,o);
        }else {
            psmt.setNull(index+1,Types.NULL);
        }
    }

    public static int updateBatchById(List<?> entities, ClassFieldSearcher searcher) throws SQLException {
        String sql = new SqlConditionBuilder<>(searcher).getUpdateByIdSql();
        String[] fields = searcher.getPrivateFields().toArray(new String[0]);
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < entities.size(); i++) {
            Object obj = entities.get(i);
            for (int j = 0; j < fields.length; j++) {
                try {
                    Field f = searcher.getClazz().getDeclaredField(fields[j]);
                    f.setAccessible(true);
                    Object value = f.get(obj);
                    if (value != null) {
                        pstmt.setObject(j + 1, f.get(obj));
                    } else {
                        pstmt.setNull(j + 1, Types.NULL);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    logger.error("get field value fail", e);
                    logger.warn("current sql", sql);
                    logger.warn("current entity", obj);
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
        finalExecute(conn, pstmt);
        return entities.size();
    }

    public static int deleteBatchById(List<?> entities, ClassFieldSearcher searcher) throws SQLException {
        String sql = new SqlConditionBuilder<>(searcher).getDeleteByIdSql();
        Connection conn = getConnection();
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
        finalExecute(conn, pstmt);
        return entities.size();
    }

    private static void finalExecute(Connection conn, PreparedStatement pstmt) throws SQLException {
        pstmt.executeBatch();
        pstmt.clearBatch();
        conn.commit(); // 提交事务
        conn.setAutoCommit(true); // 恢复自动提交
        pstmt.close();
    }
}
