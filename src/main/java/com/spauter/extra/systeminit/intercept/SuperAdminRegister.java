package com.spauter.extra.systeminit.intercept;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;

import static com.spauter.extra.baseentity.utils.ValueUtil.isBlank;

/**
 * <h3><b>超级管理员注册</b></h3>
 * 系统启动后，前端第一个发送的请求会是{@code /system/javaVersion}。
 * 拦截到这个请求后，会获取发起这个请求的浏览器作为临时超级管理员，直到登录<p>
 *     前端会生成这个uuid让浏览器保存，在同一个设备下不同浏览器的uuid都不相同。
 * <p>此操作会发第一个发送该请求的作为超级管理员，因此第一个打开初始化页面的为超级管理员</p>
 * <p>如果向重新设置，请在控制台输</p>
 * {@code redis:remove AuthorizationUUID}<p>
 * {@code sql:delete from user where role='superAdmin'}
 * <p>然后重试
 */
@Configuration
public class SuperAdminRegister implements HandlerInterceptor {

    @Resource
    @Qualifier("customRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String method=request.getMethod();
        if("OPTIONS".equalsIgnoreCase(method)){
            response.setStatus(200);
            return true;
        }
        String authorizationUUID = request.getHeader("AuthorizationUUID");
        String originAuthorizationUUID = (String) redisTemplate.opsForValue().get("AuthorizationUUID");
        if (authorizationUUID == null) {
            response.sendError(403, "请先登录");
            return false;
        }
        if (!isBlank(originAuthorizationUUID) && !originAuthorizationUUID.equals(authorizationUUID)) {
            //提示
            response.sendError(403, "超级管理员已经注册，如果你想重新注册，请在控制台中输入redis:remove AuthorizationUUID,然后再试一次");
            return false;
        }
        redisTemplate.opsForValue().set("AuthorizationUUID", authorizationUUID);
        return true;
    }

}
