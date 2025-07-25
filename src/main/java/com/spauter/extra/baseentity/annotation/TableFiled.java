package com.spauter.extra.baseentity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableFiled {
    /**
     * 设置字段名，默认为空，为空则根据驼峰命名法为成员变量命名
     */
    String value() default "";

    /**
     * 设置字段是否存在，默认为true，
     * 设置false时不会被作为表字段
     */
    boolean exists() default true;
}
