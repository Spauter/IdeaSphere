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


//    List<T>findListByIds(List<Object> ids);

    /**
     * 分页查询
     */
    List<T> findByPage(QueryWrapper<T> queryWrapper, int page, int size,String orderBy) throws SQLException;

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
    default int insertOne(T t) throws SQLException {
       return insertList(List.of(t));
    }

    /**
     * 插入多条数据
     */
    int insertList(List<T> entities) throws SQLException;


    /**
     * 根据条件更新
     * @param queryWrapper 更新条件，不能为null
     */
    int update(UpdateWrapper<T> queryWrapper) throws SQLException;

    /**
     * 更新多条数据(根据id)
     */
    int updateList(List<T> entities) throws SQLException;

    /**
     *  根据id更新数据
     */
    default int updateById(T t) throws SQLException {
      return   updateListById(List.of(t));
    }

    /**
     * 根据id更新多条数据
     */
    int updateListById(List<T> entities) throws SQLException;

    /**
     * 根据条件删除数据
     * @param queryWrapper 删除条件，不能为null
     */
    int delete(UpdateWrapper<T> queryWrapper) throws SQLException;

    /**
     * 根据id删除数据
     */
    default int deleteById(T t) throws SQLException {
       return deleteByIds(List.of(t));
    }

    /**
     * 根据id删除多条数据
     */
    int deleteByIds(List<T> entities) throws SQLException;


    List<Map<String, Object>> selectBySql(String sql, Object... args) throws SQLException;

    List<Map<String, Object>> selectByPage(String sql, int page, int size,String orderBy, Object... args);

    /**
     * 根据sql查询一条数据
     * @param e 返回的类型,假如返回的是User,那么e就是new User()或者是这个对象
     * @param args sql中的参数，不使用带 ？ 的占位符时，args为null
     */
    <E> E selectOne(String sql,E e,@Nullable Object ... args)throws SQLException;

    <E> List<E> selectList(String sql,E e,@Nullable Object ... args)throws SQLException;
}
