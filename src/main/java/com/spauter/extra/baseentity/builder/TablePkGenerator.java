package com.spauter.extra.baseentity.builder;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.database.dao.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TablePkGenerator {

    private static final Map<String, Long> counts = new ConcurrentHashMap<>();

    /**
     * 使用雪花算法生成唯一ID
     * <p>
     * 该方法通过雪花算法生成分布式系统下的唯一ID，使用随机数作为工作机器ID的基础，
     * 确保在多实例部署时ID生成的唯一性。方法使用同步锁保证线程安全。
     * </p>
     *
     * @return 返回生成的64位长整型唯一ID
     */
    public synchronized static long generateIdByUUID() {
        int base = (int) (Math.random() * 32);
        Snowflake snowflake = IdUtil.getSnowflake(base);
        return snowflake.nextId();
    }

    /**
     * 通过数据库自增方式生成主键ID
     * <p>
     * 该方法通过查询指定表的当前最大ID值，然后加1作为新ID。如果表中没有记录则返回1。
     * 方法使用同步锁保证线程安全，确保在多线程环境下ID生成的正确性。
     * </p>
     *
     * @param searcher 包含表名和主键字段信息的搜索器对象
     * @return 返回生成的自增主键ID
     * @throws SQLException 如果数据库查询过程中发生错误
     */
    public synchronized static long generateIdByAutoIncrement(ClassFieldSearcher searcher) throws SQLException {
        String tableName = searcher.getTableName();
        String pk = searcher.getTablePk();
        if (counts.containsKey(tableName)) {
            counts.put(tableName, counts.get(tableName) + 1);
            return counts.get(tableName);
        } else {
            String sql = "select " + pk + " from " + tableName + " order by " + pk + " desc limit 1";
            List<Map<String, Object>> list = JdbcTemplate.select(sql);
            Object value = list.get(0).get(pk);
            long count = value == null ? 1 : (long) value + 1;
            counts.put(tableName, count);
            return count;
        }
    }
}
