package com.spauter.extra.systeminit.intercept;


import com.spauter.extra.database.wapper.QueryWrapper;
import com.spauter.ideasphere.entity.User;
import com.spauter.ideasphere.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;

import java.sql.SQLException;

import static com.spauter.extra.baseentity.utils.ValueUtil.getIntValue;

/**
 * <h3><b>系统初始化拦截器</b></h3>
 * 在发送/system/**的请求时，拦截请求，判断身份；
 * 如果不是超级管理员,返回403。<p>
 * 在登录后只会验证登录用户身份
 * <p>保证{@link com.spauter.extra.systeminit.controller.SystemController}</p>的请求只能由超级管理员发送
 */
@Component
public class SystemInitInterceptor implements HandlerInterceptor {

    @Resource
    @Qualifier("customRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;


    @Resource(name = "userService")
    private UserService userService;


    /**
     * 验证请求是否为超级管理员发送<p>
     * 首先获取request中的header有没有token,如果有，则验证token是否为超级管理员的token，如果是，则放行，否则返回403
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        //放行其它请求
        if (!request.getRequestURI().startsWith("/system")) {
            return true;
        }
        String method = request.getMethod();
        //放行OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(method)) {
            response.setStatus(200);
            return true;
        }
        //先看有没有超级管理员用户,没有继续判断
        if (hasSuperAdmin()) {
            response.sendError(403, "超级管理员已经注册，如果你想重新注册，请在控制台中输入redis:remove AuthorizationUUID,然后再试一次");
            return false;
        }
        String authorizationUUID = request.getHeader("AuthorizationUUID");
        if (authorizationUUID == null) {
            response.sendError(403, "无效的请求");
            return false;
        }
        String originAuthorizationUUID = (String) redisTemplate.opsForValue().get("AuthorizationUUID");
        if (originAuthorizationUUID != null && originAuthorizationUUID.equals(authorizationUUID)) {
            return true;
        }
        //获取token
        String userId = request.getHeader("userId");
        var superUser=(User)redisTemplate.opsForValue().get("superAdmin");
        if (superUser != null && getIntValue(userId).intValue() != getIntValue(superUser.getId())) {
            response.sendError(403, "非法请求");
            return false;
        }
        return true;
    }

    private boolean hasSuperAdmin() throws SQLException {
        if (redisTemplate.hasKey("superAdmin")) {
            return true;
        }
        var wrapper = new QueryWrapper<User>();
        wrapper.addEq("role", "superAdmin");
        var user = userService.findOne(wrapper);
        if (user == null) {
            return false;
        }
        redisTemplate.opsForValue().set("superAdmin", user.getId());
        return true;
    }
}
