package org.ideasphere.ideasphere;

import com.spauter.extra.baseentity.annotations.VOEntityScan;
import com.spauter.extra.config.SpringContextUtil;
import org.ideasphere.ideasphere.DataBase.Database;
import org.ideasphere.ideasphere.Logger.ILogger;
import org.ideasphere.ideasphere.Logger.Log4j2Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication(scanBasePackages = {"org.ideasphere.ideasphere", "com.spauter","com.bloducspauter"})
@EnableRedisHttpSession
@VOEntityScan(scanBasePackages = {"com.spauter.ideasphere.entity","com.bloducspauter.bean"})
public class IdeaSphereApplication {

    public static final ILogger logger = new Log4j2Logger(IdeaSphereApplication.class);


    public static void main(String[] args) {
        // 服务输出测试
        logger.info("main", "Loading libraries, please wait...");
        long startTime = System.currentTimeMillis();
        // 启动 Spring 应用
        SpringContextUtil.setApplicationContext(SpringApplication.run(IdeaSphereApplication.class,args));
        // 提示服务启动
        double elapsedTime = ((System.currentTimeMillis() - startTime) / 1000.0); // 确保是 double 类型
        logger.info("main", "Done (%.2f sec)! For help, type \"help\"", elapsedTime);
        // 添加 ShutdownHook 处理优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Database database = SpringContextUtil.getBean("database", Database.class);
            logger.info("main", "Shutting down the server...");
            // 可以在这里添加资源释放等逻辑
            try {
                database.close();
            } catch (Exception e) {
                logger.error("database", "Error closing database", e);
            }
        }));
    }
}