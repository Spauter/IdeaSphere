package org.ideasphere.ideasphere.DataBase;

import com.spauter.extra.baseentity.builder.EntityBuilder;
import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import lombok.extern.slf4j.Slf4j;
import org.ideasphere.ideasphere.DataBase.RowMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 写其它类时用一个类继承该类，然后使用database.query(sql,new BaseRowService<T>())
 * 比如：database.query("select * from user",new UserRowService())
 * 其中UserRowService继承BaseRowService<User>,可以不写任何方法
 *
 * @param <T>
 */
@Slf4j
public class BaseRowService<T> implements RowMapper<T> {
    /**
     * 获取泛型类型
     *
     * @return 泛型类型
     */
    @SuppressWarnings("unchecked")
    private Class<T> getType() {
        // 获取子类定义的泛型类型
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) type.getActualTypeArguments()[0];
    }

    /**
     * 将结果集映射为实体类
     *
     * @param rs 结果集
     * @return 实体类
     */
    @Override
    public T mapRow(ResultSet rs, String dbType) throws SQLException {
        Class<T> clazz = this.getType();
        ClassFieldSearcher searcher = ClassFieldSearcher.getSearcher(clazz);
        try {
            return new EntityBuilder(searcher).mapRow(rs);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException | ClassNotFoundException |
                 NoSuchFieldException e) {
            log.error("query fail", e);
            throw new SQLException(e);
        }
    }
}
