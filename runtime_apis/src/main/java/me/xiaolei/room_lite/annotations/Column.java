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
