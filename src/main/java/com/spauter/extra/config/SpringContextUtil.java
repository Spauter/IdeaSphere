package com.spauter.extra.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

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
     * @throws ClassCastException 如果指定的Bean类型与期望的类型不匹配
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        if (ac == null) {
            throw new IllegalStateException("ApplicationContext is not initialized");
        }
        return ac.getBean(beanName, clazz);
    }

    public static void setApplicationContext(ApplicationContext applicationContext){
        ac = applicationContext;
    }

    /**
     * 向Spring容器动态注册一个新的Bean.
     * 如果已存在，先销毁,再注册
     *
     * @param beanName 要注册的Bean名称（在Spring容器中的唯一标识）
     * @param bean 要注册的Bean实例对象
     * @throws IllegalStateException 如果ApplicationContext尚未初始化（ac为null）
     */
    public static void registerBean(String beanName,Object bean){
        if(ac==null){
            //当spring application还没启动完成时抛出异常
            throw new IllegalStateException("ApplicationContext is not initialized");
        }
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) ac;
        // 获取bean工厂并转换为DefaultListableBeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
        // 先移除已存在的bean定义（如果有）
        if(defaultListableBeanFactory.containsBeanDefinition(beanName)) {
            defaultListableBeanFactory.removeBeanDefinition(beanName);
        }
        // 如果bean是单例且已存在实例，销毁它
        if(defaultListableBeanFactory.containsSingleton(beanName)) {
            defaultListableBeanFactory.destroySingleton(beanName);
        }
        // 通过BeanDefinitionBuilder创建bean
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(bean.getClass());
        // 注册bean到Spring容器中
        defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getRawBeanDefinition());
    }

}
