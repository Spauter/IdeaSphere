package com.spauter.extra.baseentity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableName {
    /**
     *设置表名，默认为空，为空则根据驼峰命名法为类名命名
     */
    String value() default "";
}
