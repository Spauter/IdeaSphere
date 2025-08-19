package com.spauter.extra.systeminit.controller;

import com.spauter.extra.config.SpringContextUtil;
import org.ideasphere.ideasphere.DataBase.Database;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * <h3><b></b>系统操作接口</b></h3>
 * 用于获取系统信息，或者对系统的数据库，实体进行初始化<p>
 * 此Controller的任何一个操作需要超级管理员权限，所以在进入请求前会被
 * 拦截器拦截,拦截器会判断是否有超级管理员权限,如果没有则会被拦截器拦截并返回无权限。
 * @see com.spauter.extra.systeminit.intercept.SystemInitInterceptor
 * @author spauter
 * @since 2025/8/18
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    @Resource
    private RedisTemplate<String,Object>redisTemplate;

    /**
     * 获取java版本信息
     */
    @GetMapping(value = "javaVersion",name = "获取java版本信息")
    public Map<String,String> getJavaVersion() {
        var map=new HashMap<String,String>();
        //获取java版本
        var jdkVersion=System.getProperty("java.version");
        map.put("javaVersion",jdkVersion);
        return map;
    }

    /**
     * 获取数据库信息和版本
     */
    @GetMapping(value = "databaseVersion")
    public Map<String,String>getDatabaseInfo() throws SQLException {
        var map=new HashMap<String,String>();
        //获取前数据库版本信息
        Connection conn= SpringContextUtil.getBean("conn",Connection.class);
        DatabaseMetaData metaData = conn.getMetaData();
        Database database=SpringContextUtil.getBean("database", Database.class);
        String version = metaData.getDatabaseProductVersion();//得到数据库版本信息
        String type=database.getDbType();
        map.put("databaseType",type);
        map.put("databaseVersion",version);
        return map;
    }

    @GetMapping("requires")
    public Map<String,Object> getRequirementTxt(){
        //todo
        var map=new HashMap<String,Object>();
        return map;
    }
}
