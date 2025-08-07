package com.spauter.extra.database.init;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.ideasphere.ideasphere.DataBase.Database;
import org.ideasphere.ideasphere.DataBase.DatabaseManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

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

    @Resource
    @Lazy
    private DatabaseManager manager;

    @Resource
    @Lazy
    private Connection conn;

    @Resource
    @Lazy
    private Database database;


    @Bean
    public DatabaseManager databaseManager() {
        log.info("Initializing database...");
        manager = new DatabaseManager("");
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
    public Database database() {
        initTable();
        return database;
    }


    public void initTable() {
        String dbType = database.getDbType();
        log.info("The current database type is: {}", dbType);
        log.info("creating table...");
        File file =
                switch (dbType) {
                    case "mysql" -> new File("SQL/mysql.sql");
                    case "mariadb" -> new File("SQL/mariadb.sql");
                    case "postgresql" -> new File("SQL/postgresql.sql");
                    case "sqlite" -> new File("SQL/sqlite.sql");
                    default -> throw new RuntimeException("数据库类型不存在");
                };
        String sql;
        try {
            sql = new String(Files.readAllBytes(file.toPath()));
            String[] sqls = sql.split(";");
            for (String s : sqls) {
                try {
                    if (!s.trim().isEmpty()) {
                        log.info("sql:\n {}", s);
                        database.update(s);
                        log.info("sql execute success");
                    }
                } catch (SQLException e) {
                    log.error("Error creating table because: \n{} {}", e.getClass().getSimpleName(), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("error reading sql file", e);
            return;
        }
        log.info("""
                \n
                Database initialization completed successfully!
                You can verify the tables with these commands:
                sql:select * from user;  -- Query sample data
                sql:show tables;       -- List all tables if you're using MySQL or MariaDB
                sql:select count(*) from user;  -- Check record count
                """);
    }
}
