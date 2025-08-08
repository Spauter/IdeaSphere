package com.spauter.extra.baseentity.annotations;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 用于扫描实体类，只能放在启动类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface VOEntityScan {
    /**
     * 扫描的包名
     */
    String[] scanBasePackages() default {};

    /**
     * 指定class
     */
    Class<?>[] scanClasses() default {};
}
