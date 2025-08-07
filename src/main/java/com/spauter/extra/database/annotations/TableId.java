package com.spauter.extra.database.annotations;

import com.spauter.extra.baseentity.enums.IdType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记主键
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableId {
    /**
     * 标记主键字段名，默认为空，为空则根据驼峰命名法为成员变量命名，否者根据value的值命名
     */
    String value() default "";

    /**
     * 标记主键类型，默认为UUID,可选值为AUTO_INCREMENT表示自增
     */
    IdType idType() default IdType.UUID;
}
