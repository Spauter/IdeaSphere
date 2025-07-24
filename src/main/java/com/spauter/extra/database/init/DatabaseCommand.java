package com.spauter.extra.database.init;

import com.spauter.extra.config.SpringContextUtil;
import com.spauter.extra.database.dao.JdbcTemplate;
import org.ideasphere.ideasphere.DataBase.Database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.ideasphere.ideasphere.IdeaSphereApplication.logger;


public class DatabaseCommand {

    //处理sql命令
    public void sqlCommandConfig(String input) {
        String front=input.substring(0,4);
        if("sql:".equalsIgnoreCase(front)){
            String sql = input.substring(4).toLowerCase();
            if (sql.startsWith("select") || sql.startsWith("show")) {
                sqlQuery(sql);
            } else if (sql.startsWith("update") || sql.startsWith("delete")) {
                sqlUpdate(sql);
            } else {
                sqlUpdateOther(sql);
            }
        }
    }


    public void sqlQuery(String sql) {
        try {
            List<Map<String, Object>> list = JdbcTemplate.select(sql);
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
            logger.error("query fail", e);
        }
    }

    public void sqlUpdate(String sql) {
        try {
            int i =JdbcTemplate.update(sql);
            System.out.println("update success,the number of rows affected is " + i);
        } catch (SQLException e) {
            logger.error("update fail", e);
        }
    }

    public void sqlUpdateOther(String sql) {
        Database database = SpringContextUtil.getBean("database", Database.class);
        try {
            database.update(sql);
            logger.info("sql execute success");
        } catch (SQLException e) {
            logger.error("sql execute fail", e);
        }
    }
}