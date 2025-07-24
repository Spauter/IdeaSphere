package com.spauter.extra.baseentity.annotation;

import com.spauter.extra.baseentity.enums.IdType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableId {
    String value() default "";
    //未实现
    IdType idType() default IdType.UUID;
}
