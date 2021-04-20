package me.xiaolei.room_lite.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 索引<br/>
 * <br/>
 * 如果放在类上，则直接读取索引名称，字段名称，和unique属性,如果不指定name的值，那么默认 表名+index，譬如：User_index <br/>
 * <br/>
 * 如果放在字段上，则只读取索引名称，和unique属性,如果不指定name的值，那么默认表名+字段名+index。譬如：User_id_index<br/>
 * <br/>
 */
@Retention(RetentionPolicy.CLASS)
@Target({})
public @interface Index
{
    // 索引的名称,如果为空，则默认是 `表名+index`，譬如：user_index
    String name() default "";

    // 需要添加进索引的字段名称
    String[] columnNames() default {};

    /**
     * 如果设置为true，这将是唯一索引，并且任何重复项都将被拒绝。
     */
    boolean unique() default false;
}
