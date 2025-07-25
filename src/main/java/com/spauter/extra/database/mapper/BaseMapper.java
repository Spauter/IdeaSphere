package com.spauter.extra.database.mapper;

import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.extra.database.wapper.UpdateWrapper;
import jakarta.annotation.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface BaseMapper<T> {

    /**
     * 查询所有
     */
    List<T> findAll() throws SQLException;

    /**
     * 根据queryWrapper查询对应的数据
     * @param queryWrapper 查询条件，为null时查询所有，等同于findAll()
     */
    List<T> findList(@Nullable QueryWrapper<T> queryWrapper) throws SQLException;

    /**
     * 分页查询
     */
    List<T> findByPage(QueryWrapper<T> queryWrapper, int page, int size);

    /**
     * 查询一条数据
     */
    default T findOne(QueryWrapper<T> queryWrapper) throws SQLException{
        List<T>entities=findList(queryWrapper);
        if(entities.size()>1){
            throw new SQLException("We need only one,but we get "+entities.size());
        }
        return entities.size()==1?entities.get(0):null;
    }

    /**
     * 根据id查询一条数据
     */
    T findById(Object id) throws SQLException;

    /**
     * 插入一条数据
     */
    default void insertOne(T t) throws SQLException {
        insertList(List.of(t));
    }

    /**
     * 插入多条数据
     */
    void insertList(List<T> entities) throws SQLException;


    /**
     * 根据条件更新
     * @param queryWrapper 更新条件，不能为null
     */
    void update(UpdateWrapper<T> queryWrapper) throws SQLException;

    /**
     * 根据条件更新
     * @param updateWrapper 更新条件，不能为null
     * @param entities 需要更新的数据
     */
    void updateList(@Nullable UpdateWrapper<T> updateWrapper,List<T> entities) throws SQLException;

    /**
     * 更新多条数据(根据id)
     */
    void updateList(List<T> entities) throws SQLException;

    /**
     *  根据id更新数据
     */
    default void updateById(T t) throws SQLException {
        updateListById(List.of(t));
    }

    /**
     * 根据id更新多条数据
     */
    void updateListById(List<T> entities) throws SQLException;

    /**
     * 根据条件删除数据
     * @param queryWrapper 删除条件，不能为null
     */
    void delete(UpdateWrapper<T> queryWrapper) throws SQLException;

    /**
     * 根据条件删除数据
     * @param updateWrapper 删除条件，为null或者为加where条件时根据id删除
     */
    void delete(@Nullable UpdateWrapper<T> updateWrapper, List<T> entities)throws SQLException;

    /**
     * 根据id删除数据
     */
    default void deleteById(T t) throws SQLException {
        deleteByIds(List.of(t));
    }

    /**
     * 根据id删除多条数据
     */
    void deleteByIds(List<T> entities) throws SQLException;


    List<Map<String, Object>> selectBySql(String sql, Object... args) throws SQLException;

    List<Map<String, Object>> selectByPage(String sql, int page, int size, Object... args);

    /**
     * 根据sql查询一条数据
     * @param e 返回的类型,假如返回的是User,那么e就是new User()或者是这个对象
     * @param args sql中的参数，不使用带 ？ 的占位符时，args为null
     */
    <E> E selectOne(String sql,E e,@Nullable Object ... args)throws SQLException;

    <E> List<E> selectList(String sql,E e,@Nullable Object ... args)throws SQLException;
}
