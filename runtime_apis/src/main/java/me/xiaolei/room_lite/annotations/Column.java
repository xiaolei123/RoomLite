package me.xiaolei.room_lite.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
public @interface Column
{
    /**
     * 名称
     */
    String name() default "";

    /**
     * 类型
     */
    SQLType type() default SQLType.UNDEFINED;

    /**
     * 如果设置为true，这将是唯一索引，并且任何重复项都将被拒绝。<br/><br/>
     * 如果设置为true，会在创建表的时候，自动加上 UNIQUE<br/><br/>
     * 此属性也会影响到，如果index()=true的时候的，索引SQL的生成，决定是否添加UNIQUE的关键字<br/><br/>
     */
    boolean unique() default false;

    /**
     * 声明此字段不能为NULL
     */
    boolean notNull() default false;

    /**
     * 默认值<br/>
     * 默认值不会自动加上单引号 ' <br/>
     * 如果你是个数字，则直接写比如: "100" <br/>
     * 如果你是字符串，则应该自己加上单引号比如: "'hello'"<br/>
     */
    String defaultValue() default "";

    /**
     * 是否快速新增一个索引，索引名称默认 表名+字段名+index，譬如：User_id_index<br/>
     * <br/><br/>
     * 此属性会生成SQL，而也受 unique() 的影响<br/><br/>
     */
    boolean index() default false;

    public static enum SQLType
    {
        /**
         * 未定义
         */
        UNDEFINED("UNDEFINED"),
        /**
         * 文本
         */
        TEXT("TEXT"),
        /**
         * 数字
         */
        INTEGER("INTEGER"),
        /**
         * 浮点数或双精度数的列亲和力常数
         */
        REAL("REAL"),
        /**
         * 二进制
         */
        BLOB("BLOB");

        String typeString;

        public String getTypeString()
        {
            return this.typeString;
        }

        SQLType(String str)
        {
            this.typeString = str;
        }
    }
}
