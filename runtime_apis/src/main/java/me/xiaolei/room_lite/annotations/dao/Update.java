package me.xiaolei.room_lite.annotations.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>更新</b> 支持：<br/>
 * 参数：Entity / Entity[] / List&lt;Entity><br/>
 * 返回值：void / int<br/>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface Update
{
}
