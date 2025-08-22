package com.spauter.extra.database.annotations;

import com.spauter.extra.baseentity.enums.RelationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体类关联关系注解
 * <p>用于标注实体类之间的关联关系，支持一对一、一对多、多对一关联配置</p>
 *
 * <p><b>使用场景示例：</b></p>
 * <pre>
 * // 一对一关联
 * {@literal @}VORelation(
 *     query = "departmentId",
 *     queryBy = "id",
 *     relationType = RelationType.ONE_TO_ONE
 * )
 * {@code  private Department department;}
 *
 * // 一对多关联（必须指定relationClass）
 * {@literal @}VORelation(
 *     query = "id",
 *     queryBy = "orderId",
 *     relationType = RelationType.ONE_TO_MANY,
 *     relationClass = OrderItem.class
 * )
 * {@code private List<OrderItem> items;}
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VORelation {

    /**
     * 被关联表的查询字段(表字段)
     * <p>默认使用被关联表的主键字段,</p>
     */
    String queryBy() default "";

    /**
     * 关联查询字段（当前实体表的字段名）
     * <p>默认自动推断为：字段名 + "Id"（如user → userId）</p>
     */
    String query() default "";

    /**
     * 关联关系类型
     * <p>默认为一对一关联</p>
     *
     * @see RelationType
     */
    RelationType relationType() default RelationType.ONE_TO_ONE;

    /**
     * 关联实体类（一对多时必须指定）
     */
    Class<?> relationClass() default Object.class;
}
