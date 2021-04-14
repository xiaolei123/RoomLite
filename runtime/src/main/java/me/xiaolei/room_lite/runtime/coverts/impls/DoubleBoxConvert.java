package me.xiaolei.room_lite.runtime.coverts.impls;

import me.xiaolei.room_lite.runtime.coverts.base.ToDoubleConvert;

public class DoubleBoxConvert extends ToDoubleConvert<Double>
{
    public DoubleBoxConvert()
    {
        super(Double.class);
    }

    @Override
    public Double convertToDouble(Double javaObj)
    {
        return javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Double cursorToJavaObject(double value)
    {
        return value;
    }
}
