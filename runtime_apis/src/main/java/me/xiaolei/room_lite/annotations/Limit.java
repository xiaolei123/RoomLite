package me.xiaolei.room_lite.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 翻页
 */
@Retention(RetentionPolicy.CLASS)
@Target({})
public @interface Limit
{
    /**
     * 从哪个下标开始计算
     */
    String index();

    /**
     * 从下标开始，需要获取的长度
     */
    String maxLength();
}
