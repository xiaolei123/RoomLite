package me.xiaolei.myroom.library.anno.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询的DAO<br/>
 * <br/>
 * 支持的返回类型：<br/>
 * <br/>
 * Entity / Entity[] / List&lt;Entity> / 八大基本类型以及其包装类 / String<br/>
 * <br/>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Query
{
    String what() default "*";

    Class<?> entity();

    String whereClause() default "1=1";

    String limit() default "0,2000";
}
