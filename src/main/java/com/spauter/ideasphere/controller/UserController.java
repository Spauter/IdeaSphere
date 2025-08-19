package com.spauter.ideasphere.controller;

import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.ideasphere.entity.User;
import com.spauter.ideasphere.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource(name = "userService")
    private UserService userService;

    @Resource
    @Qualifier("customRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping(value = "/register", name = "注册", params = {"username", "password"})
    public Map<String, Object> register(HttpServletRequest request) throws SQLException {
        var map = new HashMap<String, Object>();
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (userName == null || password == null) {
            map.put("code", 404);
            map.put("msg", "Please input username and password");
            return map;
        }
        queryWrapper.addEq("username", userName);
        User register = userService.findOne(queryWrapper);
        if (register != null) {
            map.put("code", 404);
            map.put("msg", "Username has been registered");
            return map;
        }
        try {
            User user = userService.register(userName, password);
            String token = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(token, user, 20, TimeUnit.MINUTES);
            map.put("code", 200);
            map.put("msg", "Register success");
            user.setPassword(null);
            map.put("data", user);
        } catch (Exception e) {
            map.put("code", 500);
            map.put("msg", "Register fail");
            map.put("data", e.getMessage());
        }
        return map;
    }

    @GetMapping(value = "/users", name = "查询所有用户")
    public java.util.List<User> findAll() throws SQLException {
        return userService.findAll();
    }

    @PostMapping(value = "/login", name = "登录", params = {"username", "password"})
    public Map<String, Object> login(HttpServletRequest request) throws SQLException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username == null || password == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 404);
            map.put("msg", "Please input username and password");
            return map;
        }
        User user = userService.login(username, password);
        var map = new HashMap<String, Object>();
        if (user != null) {
            String token = UUID.randomUUID().toString();
            map.put("code", 200);
            map.put("msg", "Login success");
            map.put("data", user);
            redisTemplate.opsForValue().set(token, user, 20, TimeUnit.MINUTES);
            map.put("token", token);
        } else {
            map.put("code", 404);
            map.put("msg", "Username or password is wrong");
        }
        return map;
    }

    @GetMapping(value = "/logout", name = "登出")
    public Map<String, Object> logout(HttpServletRequest request) {
        String token = request.getHeader("token");
        var map = new HashMap<String, Object>();
        try {
            redisTemplate.delete(token);
            map.put("code", 200);
            map.put("msg", "Logout success");
        } catch (Exception e) {
            map.put("code", 500);
            map.put("msg", "Logout fail");
            map.put("data", e.getMessage());
        }
        return map;
    }


    @GetMapping(value = "/loginUser", name = "获取登录用户")
    public Map<String,Object> getLoginUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        var user= (User) redisTemplate.opsForValue().get(token);
        var map=new HashMap<String,Object>();
        if(user==null){
            map.put("code",404);
            map.put("msg","Please login");
        }else {
            user.setPassword(null);
            user.setIcenterPwd(null);
            map.put("code",200);
            map.put("msg","Get login user success");
            map.put("data",user);
        }
        return map;
    }
}
