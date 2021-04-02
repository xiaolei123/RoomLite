package me.xiaolei.myroom.library.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主键
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PrimaryKey
{
    /**
     * 自增长，只有当类型为int 或者是 long时候，才会生效
     */
    boolean autoGenerate() default false;
}
