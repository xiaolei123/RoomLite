package me.xiaolei.myroom.library.coverts.base;


import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.Convert;

public abstract class ToBooleanConvert extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToBooleanConvert(Class<?> javaType)
    {
        super(javaType, Column.SQLType.INTEGER);
    }

    @Override
    public abstract Boolean convertToBoolean(Object javaObj);
}
