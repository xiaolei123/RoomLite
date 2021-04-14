package me.xiaolei.room_lite.library.coverts.impls;

import me.xiaolei.room_lite.library.coverts.base.ToFloatConvert;

public class FloatConvert extends ToFloatConvert
{
    public FloatConvert()
    {
        super(float.class);
    }

    @Override
    public Float convertToFloat(Object javaObj)
    {
        return (float) javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Object cursorToJavaObject(float value)
    {
        return value;
    }
}
