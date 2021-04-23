package me.xiaolei.room_lite.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动注册，支持的类型
 *
 * @see me.xiaolei.room_lite.runtime.adapters.Adapter
 * @see me.xiaolei.room_lite.runtime.coverts.Convert
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface AutoRegister
{
    
}
