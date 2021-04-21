package me.xiaolei.room_lite.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 排序
 */
@Retention(RetentionPolicy.CLASS)
@Target({})
public @interface OrderBy
{
    /**
     * 需要排序的字段
     */
    String[] columnNames();

    /**
     * 排序方式是升序还是降序
     */
    Type type() default Type.ASC;

    public static enum Type
    {
        ASC, DESC
    }
}
