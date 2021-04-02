package me.xiaolei.myroom.library.coverts.base;


import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.Convert;

public abstract class ToLongConvert extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToLongConvert(Class<?> javaType)
    {
        super(javaType, Column.SQLType.INTEGER);
    }

    @Override
    public abstract Long convertToLong(Object javaObj);
}
