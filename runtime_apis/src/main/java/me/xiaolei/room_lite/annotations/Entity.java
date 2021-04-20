package me.xiaolei.room_lite.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Entity
{
    /**
     * 表名
     */
    String name() default "";

    /**
     * 索引
     */
    Index[] indices() default {};
}
