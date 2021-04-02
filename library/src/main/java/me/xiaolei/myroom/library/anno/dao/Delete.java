package me.xiaolei.myroom.library.anno.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>删除记录</b> 支持：<br/>
 * 参数：Entity / Entity[] / List&lt;Entity><br/>
 * 返回值：void / int<br/>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Delete
{
}
