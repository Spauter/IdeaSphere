package com.spauter.extra.database.config;


import com.spauter.extra.database.wapper.Wrapper;
import jakarta.annotation.Resource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(value = 0)
public class BaseServiceAspect {

    @Resource(name = "customRedisTemplate")
    private RedisTemplate<String,Object> redisTemplate;
    /**
     * 环绕通知 - 处理BaseServiceImpl中以select开头的方法的缓存逻辑
     * <p>
     * 1. 先从Redis缓存中查询数据
     * 2. 如果缓存命中则直接返回缓存数据
     * 3. 如果缓存未命中则执行实际方法，并将结果存入Redis缓存
     *
     * @param joinPoint 连接点，包含被拦截方法的相关信息
     * @return 方法执行结果或缓存数据
     */
    @Around("execution(* com.spauter.extra.database.service.impl.BaseServiceImpl.select*(..)) || "+
            "execution(* com.spauter.extra.database.service.impl.BaseServiceImpl.find*(..))")
    public Object executeBefore(ProceedingJoinPoint joinPoint) throws Throwable {
        Object [] args=joinPoint.getArgs();
        Class<?>[] paramTypes=new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i]=args[i].getClass();
        }
        Method method = joinPoint.getSignature().getDeclaringType().getMethod(joinPoint.getSignature().getName(),paramTypes);
        String name= method.getName();
        Class<?>entityClass=deduceEntityClass(joinPoint);
        String key=generateCacheKey(name,args,entityClass);
        Object o=redisTemplate.opsForValue().get(key);
        if (o != null) {
            return o;
        } else {
            Object result= joinPoint.proceed();
            if (result == null) {
                return null;
            }
            redisTemplate.opsForValue().set(key,result,10, TimeUnit.MINUTES);
            return result;
        }
    }

    /**
     * 生成缓存键，采用安全的方式构建唯一键值
     * <p>
     * 键格式为：实体类名:方法名:参数1:参数2:...:参数N
     *
     * @param methodName 方法名称，用于标识缓存来源
     * @param args 方法参数数组，用于构建唯一键
     * @param entityClass 实体类类型，作为缓存键的前缀
     * @return 生成的缓存键字符串
     */
    private String generateCacheKey(String methodName, Object[] args, Class<?> entityClass) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(entityClass.getSimpleName()).append(":");
        keyBuilder.append(methodName).append(":");

        for (Object arg : args) {
            if (arg instanceof Wrapper<?> wrapper) {
                // 对Wrapper进行更稳定的序列化
                keyBuilder.append(wrapper.getAllParams());
            } else if (arg != null) {
                keyBuilder.append(arg.toString().hashCode());
            } else {
                keyBuilder.append("null");
            }
            keyBuilder.append(":");
        }

        return keyBuilder.toString();
    }

    /**
     * 后置通知 - 处理BaseServiceImpl中insert、update、delete开头方法的缓存清除逻辑
     * <p>
     * 当执行增删改操作后，清除该实体类相关的所有缓存数据
     * 使用通配符匹配所有以实体类名为前缀的缓存键进行批量删除
     *
     * @param joinPoint 连接点，包含被拦截方法的相关信息
     */
    @After("execution(* com.spauter.extra.database.service.impl.BaseServiceImpl.insert*(..)) || " +
            "execution(* com.spauter.extra.database.service.impl.BaseServiceImpl.update*(..)) || " +
            "execution(* com.spauter.extra.database.service.impl.BaseServiceImpl.delete*(..))")
    public void evictCache(JoinPoint joinPoint) {
        Class<?> entityClass = deduceEntityClass(joinPoint);
        String pattern = entityClass.getSimpleName() + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // 推断实体类型
    private Class<?> deduceEntityClass(JoinPoint joinPoint) {
        // 尝试从泛型参数获取实体类型
        Type returnType = joinPoint.getTarget().getClass().getGenericSuperclass();
        if (returnType instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
            if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                return (Class<?>) typeArguments[0];
            }
        }
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] != null) {
            return args[0].getClass();
        }
        return Object.class;
    }
}


