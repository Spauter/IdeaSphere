package com.spauter.extra.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记字段,设置该字段在数据库对应的字段<p>
 * 在ClassFieldSearcher加载实体类，会获取私有属性字段的注解。
 * 如果没有这个注解，会根据驼峰命名法为成员变量命名；
 * 如果设置了这个注解，但是没设置value，依然根据驼峰命名法为成员变量命名；
 * 设置了value，则以value为准。
 * <p>exists是用于标记该字段是否存在，默认为true，设置false时不会被作为表字段</p>
 */
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
