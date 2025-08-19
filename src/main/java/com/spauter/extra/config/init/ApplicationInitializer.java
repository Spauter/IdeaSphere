package com.spauter.extra.config.init;

import com.spauter.extra.command.DatabaseCommand;
import com.spauter.extra.command.RedisCommand;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.ideasphere.ideasphere.Config.Config;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ideasphere.ideasphere.IdeaSphereApplication.logger;

@Configuration
public class ApplicationInitializer {

    @Resource
    private RedisCommand redisCommand;

    @Resource
    private DatabaseCommand databaseCommand;

    // 主目录路径
    String mainDirPath = Paths.get(".").toAbsolutePath().normalize().toString();

    @PostConstruct
    public void initApplicationConfig() {
        // 获取主目录路径
        logger.info("main", "Main directory path: " + mainDirPath);
        // 调用 Config 模块检查并创建 config 文件夹
        Config.checkAndCreateConfigDir(mainDirPath);

        // 验证 config 文件夹是否创建成功
        Path configPath = Paths.get(mainDirPath, "config");
        if (!Files.exists(configPath)) {
            logger.error("main", "Failed to create config directory: " + configPath);
        }
    }

    @PostConstruct
    public void commandConfig() {
        Thread commandConfig =
                // 处理用户输入停止服务
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                        String input;
                        while ((input = reader.readLine()) != null) {
                            if ("stop".equalsIgnoreCase(input)) {
                                logger.info("main", "Stopping the server...");
                                System.exit(0);
                            } else if (input.startsWith("sql:")) {
                                databaseCommand.sqlCommandConfig(input.substring(4));
                            } else if (input.startsWith("redis:")) {
                                redisCommand.redisCommand(input.substring(6));
                            }
                        }
                    } catch (IOException e) {
                        logger.error("main", "Error reading input", e);
                    }
                });
        commandConfig.setDaemon(true);
        commandConfig.setName("commandConfig");
        commandConfig.start();
    }
}
