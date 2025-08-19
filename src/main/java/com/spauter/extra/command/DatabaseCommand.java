package com.spauter.extra.command;

import com.spauter.extra.config.SpringContextUtil;
import com.spauter.extra.database.dao.JdbcTemplate;
import org.ideasphere.ideasphere.DataBase.Database;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.ideasphere.ideasphere.IdeaSphereApplication.logger;

/**
 * <h1>数据库命令</h1>
 * 想在控制台中执行sql.请输入:<p>
 * {@code sql:select * from table --示例}
 */
@Service
public class DatabaseCommand {

    //处理sql命令
    public void sqlCommandConfig(String sql) {
        if (sql.startsWith("select") || sql.startsWith("show")) {
            sqlQuery(sql);
        } else if (sql.startsWith("update") || sql.startsWith("delete")) {
            sqlUpdate(sql);
        } else {
            sqlUpdateOther(sql);
        }
    }


    private void sqlQuery(String sql) {
        try {
            List<Map<String, Object>> list = JdbcTemplate.select(sql);
            if (list.isEmpty()) {
                System.out.println("no data");
                return;
            }
            for (String l : list.get(0).keySet()) {
                System.out.printf(l + "\t\t");
            }
            System.out.println();
            for (Map<String, Object> map : list) {
                for (String l : map.keySet()) {
                    System.out.printf(map.get(l) + "\t");
                }
                System.out.println();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void sqlUpdate(String sql) {
        try {
            int i = JdbcTemplate.update(sql);
            System.out.println("update success,the number of rows affected is " + i);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    private void sqlUpdateOther(String sql) {
        Database database = SpringContextUtil.getBean("database", Database.class);
        try {
            database.update(sql);
            logger.info("sql execute success");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
}