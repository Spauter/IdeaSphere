package com.spauter.extra.systeminit.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.spauter.extra.config.SpringContextUtil;
import com.spauter.ideasphere.entity.User;
import com.spauter.ideasphere.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.ideasphere.ideasphere.DataBase.Database;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * <h3><b></b>系统操作接口</b></h3>
 * 用于获取系统信息，或者对系统的数据库，实体进行初始化<p>
 * 此Controller的任何一个操作需要超级管理员权限，所以在进入请求前会被
 * 拦截器拦截,拦截器会判断是否有超级管理员权限,如果没有则会被拦截器拦截并返回无权限。
 *
 * @author spauter
 * @see com.spauter.extra.systeminit.intercept.SystemInitInterceptor
 * @since 2025/8/18
 */
@RestController
@RequestMapping("/system")
@Slf4j
public class SystemController {

    @Resource
    @Qualifier("customRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;


    @Resource
    @Qualifier("userService")
    private UserService userService;

    /**
     * 获取java版本信息
     */
    @GetMapping(value = "javaVersion", name = "获取java版本信息")
    public Map<String, String> getJavaVersion() {
        var map = new HashMap<String, String>();
        //获取java版本
        var jdkVersion = System.getProperty("java.version");
        map.put("javaVersion", jdkVersion);
        return map;
    }

    /**
     * 获取数据库信息和版本
     */
    @GetMapping(value = "databaseVersion")
    public Map<String, String> getDatabaseInfo() throws SQLException {
        var map = new HashMap<String, String>();
        //获取前数据库版本信息
        Connection conn = SpringContextUtil.getBean("conn", Connection.class);
        DatabaseMetaData metaData = conn.getMetaData();
        Database database = SpringContextUtil.getBean("database", Database.class);
        String version = metaData.getDatabaseProductVersion();//得到数据库版本信息
        String type = database.getDbType();
        map.put("databaseType", type);
        map.put("databaseVersion", version);
        return map;
    }

    @GetMapping("requires")
    public Map<String, Object> getRequirementTxt() throws IOException {
        //todo
        var map = new HashMap<String, Object>();
        File file = new File("");
        String requrires = new String(Files.readAllBytes(file.toPath()));
        return map;
    }

    /**
     * @see com.spauter.ideasphere.controller.UserController#register(HttpServletRequest)
     */
    @PostMapping(path = "register",name = "注册超级管理员")
    public Map<String,Object>superAdminRegister(HttpServletRequest request) throws SQLException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        password= DigestUtil.sha256Hex(password);
        var user=new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("superAdmin");
        user.setCreatedAt(LocalDateTime.now());
        user.setIcenterUser(username);
        user.setIcenterPwd(password);
        var map=new HashMap<String,Object>();
        try{
            userService.insertOne(user);
            map.put("code",1);
            map.put("msg","注册成功");
        }catch (Exception e){
            log.error("注册超级管理员失败",e);
            map.put("code",-1);
            map.put("msg",e.getMessage());
        }
        return map;
    }
}
