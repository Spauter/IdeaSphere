package com.spauter.extra.baseentity.builder;


import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.database.dao.JdbcTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ideasphere.ideasphere.IdeaSphereApplication.logger;
//将数据库查询结果转换为实体类
public class EntityBuilder {
    final ClassFieldSearcher searcher;

    public EntityBuilder(ClassFieldSearcher searcher) {
        this.searcher = searcher;
    }

    @SuppressWarnings("unchecked")
    public <T> T mapRow(ResultSet rs) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        List<Map<String, Object>> list = JdbcTemplate.TransfromRsToList(rs);
        if (list.size() != 1) {
            throw new SQLException("We need only one row,but we get " + list.size());
        }
        return (T) getEntities(list).get(0);
    }

    public <T> List<T> mapRows(ResultSet rs) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        List<Map<String, Object>> midList = JdbcTemplate.TransfromRsToList(rs);
        return getEntities(midList);
    }


    @SuppressWarnings("unchecked")
    public <T> List<T> getEntities(List<Map<String, Object>> list) throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<T> entities = new ArrayList<>();
        for (Map<String, Object> row : list) {
            T t = (T) searcher.getClazz().getDeclaredConstructor().newInstance();
            for (String key : row.keySet()) {
                if (searcher.getFiledRelation().containsKey(key)) {
                    Field field = searcher.getClazz().getDeclaredField(searcher.getFiledRelation().get(key));
                    field.setAccessible(true);
                    try {
                        Object o = row.get(key);
                        if (o != null) {
                            field.set(t, o);
                        }
                    } catch (Exception e) {
                        logger.error("set value fail", e);
                        logger.info("value is", row.get(key));
                        logger.info("field is", field.getName());
                    }
                }
            }
            entities.add(t);
        }
        return entities;
    }
}
