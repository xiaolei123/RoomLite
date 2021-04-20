package me.xiaolei.room_lite.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 索引<br/>
 * 如果放在类上，则直接读取索引名称，字段名称，和unique属性<br/>
 * 如果放在字段上，则只读取索引名称，和unique属性<br/>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Index
{
    // 索引的名称,如果为空，则默认是 `表名+index`，譬如：user_index
    String name() default "";

    // 需要添加进索引的字段名称
    String[] columnNames() default {};
}
