package me.xiaolei.room_lite.annotations.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.xiaolei.room_lite.annotations.Limit;
import me.xiaolei.room_lite.annotations.OrderBy;

/**
 * 查询的DAO<br/>
 * <br/>
 * 支持的返回类型：<br/>
 * <br/>
 * Entity / Entity[] / List&lt;Entity> / 八大基本类型以及其包装类 / String<br/>
 * <br/>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface Query
{
    /**
     * 查询什么
     */
    String what() default "*";

    /**
     * 在哪个表查询
     */
    Class<?> entity();

    /**
     * where语句
     */
    String whereClause() default "1=1";

    /**
     * 分页
     */
    Limit limit() default @Limit(index = "", maxLength = "");

    /**
     * 分组
     */
    String[] groupBy() default {};

    /**
     * 排序
     */
    OrderBy orderBy() default @OrderBy(columnNames = {});
}
