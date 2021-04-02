package me.xiaolei.myroom.library.coverts.base;


import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.Convert;

public abstract class ToDoubleConvert extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToDoubleConvert(Class<?> javaType)
    {
        super(javaType, Column.SQLType.REAL);
    }

    @Override
    public abstract Double convertToDouble(Object javaObj);
}