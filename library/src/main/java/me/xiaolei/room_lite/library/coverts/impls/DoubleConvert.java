package me.xiaolei.room_lite.library.coverts.impls;

import me.xiaolei.room_lite.library.coverts.base.ToDoubleConvert;

public class DoubleConvert extends ToDoubleConvert
{
    public DoubleConvert()
    {
        super(double.class);
    }

    @Override
    public Double convertToDouble(Object javaObj)
    {
        return (double) javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Object cursorToJavaObject(double value)
    {
        return value;
    }
}
