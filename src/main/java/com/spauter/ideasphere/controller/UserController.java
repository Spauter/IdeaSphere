package com.spauter.ideasphere.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.spauter.ideasphere.entity.User;
import com.spauter.ideasphere.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource(name = "userService")
    private UserService userService;

    @RequestMapping(value = "/register",name = "注册测试，模拟1000个")
    public void register() throws SQLException {
        java.util.List<User>users=new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            User user=new User();
            int j= (int) (Math.random()*10000000);
            String pwd= DigestUtil.sha256Hex(j+"");
            user.setUsername(pwd);
            user.setPassword(pwd);
            user.setIcenterUser(user.getUsername());
            user.setIcenterPwd(pwd);
            user.setUserUid((int) (Math.random()*100000000));
            user.setRole("user");
            user.setCreatedAt(LocalDateTime.now());
            users.add(user);
        }
        userService.insertList(users);
    }
    @RequestMapping(value = "/users",name = "查询所有用户")
    public java.util.List<User> findAll() throws SQLException {
        return userService.findAll();
    }

    @PostMapping(value = "/login",name = "登录")
    public Map<String,Object> login(HttpServletRequest request) throws SQLException {
        String username=request.getParameter("username");
        String password=request.getParameter("password");
        if (username == null || password == null) {
            Map<String,Object>map=new HashMap<>();
            map.put("code",404);
            map.put("msg","请输入用户名和密码");
            return map;
        }
        User user=userService.login(username,password);
        Map<String,Object>map=new HashMap<>();
        if(user!=null){
            map.put("code",200);
            map.put("msg","登录成功");
            map.put("data",user);
        }else{
            map.put("code",404);
            map.put("msg","用户名或密码错误");
        }
        return map;
    }
}
