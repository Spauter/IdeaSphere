package com.spauter.extra.systeminit.intercept;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;

import static com.spauter.extra.baseentity.utils.ValueUtil.isBlank;

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
    private RedisTemplate<String,Object> redisTemplate;

    /**
     *验证请求是否为超级管理员发送<p>
     *首先获取request中的header有没有token,如果有，则验证token是否为超级管理员的token，如果是，则放行，否则返回403
     */
    @Override
    public boolean preHandle(HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull Object handler) throws Exception {
        //放行其它请求
        if(!request.getRequestURI().startsWith("/system")){
            return true;
        }
        String method=request.getMethod();
        //放行OPTIONS请求
        if("OPTIONS".equalsIgnoreCase(method)){
            response.setStatus(200);
            return true;
        }
        String authorizationUUID = request.getHeader("AuthorizationUUID");
        String originAuthorizationUUID= (String) redisTemplate.opsForValue().get("AuthorizationUUID");
        if(originAuthorizationUUID!=null||originAuthorizationUUID.equals(authorizationUUID)){
            return true;
        }
        //获取token
        String token = request.getHeader("token");
        if(token==null){
            response.sendError(403,"超级管理员已经注册，如果你想重新注册，请在控制台中输入redis:remove X-Device-ID,然后再试一次");
            return false;
        }
        String machineCode= (String) redisTemplate.opsForValue().get(token);
        if(isBlank(machineCode)){
            response.sendError(403,"超级管理员已经注册，如果你想重新注册，请在控制台中输入redis:remove X-Device-ID,然后再试一次");
        }
        return true;
    }
}
