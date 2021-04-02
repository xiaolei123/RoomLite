package me.xiaolei.myroom.library.coverts.base;


import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.Convert;

public abstract class ToStringConvert extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToStringConvert(Class<?> javaType)
    {
        super(javaType, Column.SQLType.TEXT);
    }

    @Override
    public abstract String convertToString(Object javaObj);
}
