package com.spauter.extra.database.annotations;

import com.spauter.extra.baseentity.enums.RelationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VORelation {
    /**
     * 关联字段名,为实体字段
     */
    String queryBy() default "";

    String query() default "";

    RelationType relationType() default RelationType.SINGLE;

    Class<?> relationClass() default Object.class;
}
