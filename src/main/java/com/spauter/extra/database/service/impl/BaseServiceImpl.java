package com.spauter.extra.database.service.impl;

import com.spauter.extra.baseentity.builder.EntityBuilder;
import com.spauter.extra.baseentity.builder.SqlConditionBuilder;
import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.config.SpringContextUtil;
import com.spauter.extra.database.dao.JdbcTemplate;
import com.spauter.extra.database.dao.JdbcTemplateBatchExecutor;
import com.spauter.extra.database.service.BaseService;
import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.extra.database.wapper.UpdateWrapper;
import org.ideasphere.ideasphere.DataBase.Database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ideasphere.ideasphere.IdeaSphereApplication.logger;

public class BaseServiceImpl<T> implements BaseService<T> {
    SqlConditionBuilder<T> sqlBuilder;
    EntityBuilder entityBuilder;
    ClassFieldSearcher searcher;

    public BaseServiceImpl() {
        Class<?> clazz = getDestClazz();
        searcher = new ClassFieldSearcher(clazz);
        searcher.init();
        sqlBuilder = new SqlConditionBuilder<>(searcher);
        entityBuilder = new EntityBuilder(searcher);
    }

    private Class<?> getDestClazz() {
        //获取泛型的值
        Type type = getClass().getGenericSuperclass();
        return (Class<?>) ((java.lang.reflect.ParameterizedType) type).getActualTypeArguments()[0];
    }

    @Override
    public List<T> findAll() throws SQLException {
        String sql = sqlBuilder.getFIndAllSql();
        List<Map<String, Object>> list = JdbcTemplate.select(sql);
        try {
            return entityBuilder.getEntities(list);
        } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            logger.error("get entities fail", e);
            throw new SQLException(e);
        }
    }

    @Override
    public List<T> findList(QueryWrapper<T> queryWrapper) throws SQLException {
        String sql = sqlBuilder.getFindListSql(queryWrapper);
        Object[] params = sqlBuilder.generateWhereParams(queryWrapper).toArray();
        List<Map<String,Object>>list=JdbcTemplate.select(sql, params);
        return getResultEntities(list);
    }

    @Override
    public List<T> findByPage(QueryWrapper<T> queryWrapper, int pageNo, int pageSize,String orderBy) throws SQLException {
        String sql=sqlBuilder.getFindByPageSql(queryWrapper,pageNo,pageSize,orderBy);
        List<Object> params= sqlBuilder.generateWhereParams(queryWrapper);
        params.add(pageNo);
        params.add(pageSize);
        List<Map<String,Object>>list=JdbcTemplate.select(sql, params);
        return getResultEntities(list);
    }

    private List<T> getResultEntities(List<Map<String, Object>> selectList) throws SQLException {
        try {
            return entityBuilder.getEntities(selectList);
        } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            logger.error("get entities fail", e);
            throw new SQLException(e);
        }
    }

    @Override
    public T findById(Object id) throws SQLException {
        String sql = sqlBuilder.getFindByIdSql();
        List<Map<String, Object>> list = JdbcTemplate.select(sql, id);
        try {
            List<T> entities = entityBuilder.getEntities(list);
            if (entities.size() > 1) {
                throw new SQLException("We need only one,but we get" + entities.size());
            }
            return entities.get(0);
        } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            logger.error("get entities fail", e);
            throw new SQLException(e);
        }
    }

    @Override
    public int insertList(List<T> entities) throws SQLException {
        String sql = sqlBuilder.getInsertSql();
        return JdbcTemplateBatchExecutor.insert(entities, sql, searcher);
    }

    @Override
    public int update(UpdateWrapper<T> updateWrapper) throws SQLException {
        String sql = sqlBuilder.getUpdateSql(updateWrapper);
        List<Object> setValues = new ArrayList<>(updateWrapper.getUpdateColumns().values());
        setValues.addAll(sqlBuilder.generateWhereParams(updateWrapper));
        Object[] params = setValues.toArray();
        try {
            return JdbcTemplate.update(sql, params);
        } catch (SQLException e) {
            logger.error("update fail", e);
            throw e;
        }
    }


    @Override
    public int updateList(List<T> entities) throws SQLException {
        return JdbcTemplateBatchExecutor.updateBatchById(entities, searcher);
    }

    @Override
    public int updateListById(List<T> entities) throws SQLException {
        return JdbcTemplateBatchExecutor.updateBatchById(entities, searcher);
    }

    @Override
    public int delete(UpdateWrapper<T> updateWrapper) throws SQLException {
        String sql = sqlBuilder.getDeleteSql(updateWrapper);
        List<Object> setValues = new ArrayList<>(updateWrapper.getUpdateColumns().values());
        setValues.addAll(sqlBuilder.generateWhereParams(updateWrapper));
        Object[] params = setValues.toArray();
        try {
            return JdbcTemplate.update(sql, params);
        } catch (SQLException e) {
            logger.error("delete fail", e);
            throw e;
        }
    }


    @Override
    public int deleteByIds(List<T> entities) throws SQLException {
        return JdbcTemplateBatchExecutor.deleteBatchById(entities, searcher);
    }

    @Override
    public List<Map<String, Object>> selectBySql(String sql, Object... args) throws SQLException {
        return JdbcTemplate.select(sql, args);
    }

    @Override
    public List<Map<String, Object>> selectByPage(String sql, int page, int size, Object... args) throws SQLException {
        Database database= SpringContextUtil.getBean("database",Database.class);
        String findSql=sqlBuilder.generatePageSql(sql,page,size,database.getDbType());
        return JdbcTemplate.select(findSql,page,size);
    }

    @Override
    public <E> E selectOne(String sql, E e, Object... args) throws SQLException {
        List<E> list = selectList(sql, e, args);
        if (list.size() > 1) {
            throw new SQLException("We need only one,but we get" + list.size());
        }
        return list.size() == 1 ? list.get(0) : null;
    }

    @Override
    public <E> List<E> selectList(String sql, E e, Object... args) throws SQLException {
        ClassFieldSearcher classFieldSearcher = new ClassFieldSearcher(e.getClass());
        classFieldSearcher.init();
        List<Map<String, Object>> list = JdbcTemplate.select(sql, args);
        try {
            return new EntityBuilder(classFieldSearcher).getEntities(list);
        } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | InstantiationException |
                 IllegalAccessException ex) {
            logger.error("get entities fail", ex);
            throw new SQLException(ex);
        }
    }


}
