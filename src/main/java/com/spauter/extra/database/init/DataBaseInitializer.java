package com.spauter.extra.database.init;

import lombok.extern.slf4j.Slf4j;
import org.ideasphere.ideasphere.DataBase.Database;
import org.ideasphere.ideasphere.DataBase.DatabaseManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.lang.reflect.Field;
import java.sql.Connection;

/**
 * 初始化数据库,将DatabaseManager和Connection注入到spring ioc容器中<p>
 * 通过SpringContextUtil获取，比如<p>
 * {@code DatabaseManager manager = SpringContextUtil.getBean("manager", DatabaseManager.class);}
 * <p>
 * {@code Connection conn = SpringContextUtil.getBean("conn", Connection.class);}
 * <p>
 */
@Slf4j
@Configuration
public class DataBaseInitializer {

    private DatabaseManager manager;

    private Connection conn;

    private Database database;


    @Bean
    public DatabaseManager databaseManager() {
        log.info("Initializing database...");
        manager = new DatabaseManager("./");
        return manager;
    }

    //将Connection注入到spring容器中
    @Bean
    public Connection conn() {
        try {
            Field field = manager.getClass().getDeclaredField("database");
            //绕过访问权限检查
            field.setAccessible(true);
            database = (Database) field.get(manager);
            Field connectionField = database.getClass().getDeclaredField("connection");
            connectionField.setAccessible(true);
            conn = (Connection) connectionField.get(database);
            return conn;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Error getting database", e);
        }
        return null;
    }

    @Bean
    @DependsOn("conn")
    public Database database() {
        try {
            initTable();
            log.info("Database initialized");
            log.info("""
                \n
                Database initialization completed successfully!
                You can verify the tables with these commands:
                sql:select * from user;  -- Query sample data
                sql:show tables;       -- List all tables if you're using MySQL or MariaDB
                sql:select count(*) from user;  -- Check record count
                """);
        } catch (Exception e) {
            log.error("Error initializing database", e);
        }
        return database;
    }


    public void initTable() throws Exception {
        log.info("current database is {}",database.getDbType());
        database.initialize();
    }
}
