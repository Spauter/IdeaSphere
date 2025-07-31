package com.spauter.ideasphere.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.spauter.extra.baseentity.builder.TablePkGenerator;
import com.spauter.extra.baseentity.searcher.ClassFieldSearcher;
import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.ideasphere.entity.User;
import com.spauter.ideasphere.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource(name = "userService")
    private UserService userService;

    @RequestMapping(value = "/register", name = "注册", params = {"username", "password"})
    public Map<String, Object> register(HttpServletRequest request) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.addEq("username", userName);
        User register = userService.findOne(queryWrapper);
        if (register != null) {
            map.put("code", 404);
            map.put("msg", "Username has been registered");
            return map;
        }
        if (userName == null || password == null) {
            map.put("code", 404);
            map.put("msg", "Please input username and password");
            return map;
        }
        try {
            User user = userService.register(userName, password);
            //todo 换成JWT+Redis
            HttpSession session = request.getSession();
            String token = UUID.randomUUID().toString();
            session.setAttribute(token, user);
            // RedisTemplate.opsForValue .set(token,user);
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
        Map<String, Object> map = new HashMap<>();
        if (user != null) {
            map.put("code", 200);
            map.put("msg", "Login success");
            map.put("data", user);
        } else {
            map.put("code", 404);
            map.put("msg", "Username or password is wrong");
        }
        return map;
    }

    @GetMapping(value = "/logout", name = "登出")
    public Map<String, Object> logout(HttpServletRequest request) {
        //todo 这里使用的是JWT令牌
        String token = request.getHeader("token");
        HttpSession session = request.getSession();
        Map<String, Object> map = new HashMap<>();
        try {
            //todo 换成JWT+Redis
//            RedisTemplate.opsForValue.remove(token);
            session.removeAttribute(token);
            map.put("code", 200);
            map.put("msg", "Logout success");
        } catch (Exception e) {
            map.put("code", 500);
            map.put("msg", "Logout fail");
            map.put("data", e.getMessage());
        }
        return map;
    }

    //todo 换成JWT+Redis
    @GetMapping(value = "/loginUser", name = "获取登录用户")
    public User getLoginUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        HttpSession session = request.getSession();
        return (User) session.getAttribute(token);
    }
}
