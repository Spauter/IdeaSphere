package com.spauter.extra.config;

import org.springframework.context.ApplicationContext;

public class SpringContextUtil {

    private static ApplicationContext ac;

    /**
     * 从Spring容器中获取指定名称和类型的Bean实例
     *
     * @param <T> 返回的Bean类型
     * @param beanName 要获取的Bean的名称（在Spring容器中注册的名称）
     * @param clazz 期望返回的Bean类型（用于类型转换和验证）
     * @return 匹配的Bean实例
     * @throws IllegalStateException 如果ApplicationContext尚未初始化（ac为null）
     * @throws org.springframework.beans.BeansException 如果找不到指定名称的Bean，或Bean类型不匹配
     * @throws NullPointerException 再bean配置中使用此方法会报该错误，因为SpringBoot还未启动完成
     * @throws ClassCastException 如果指定的Bean类型与期望的类型不匹配
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        return ac.getBean(beanName, clazz);
    }

    public static void setApplicationContext(ApplicationContext applicationContext){
        ac = applicationContext;
    }
}
