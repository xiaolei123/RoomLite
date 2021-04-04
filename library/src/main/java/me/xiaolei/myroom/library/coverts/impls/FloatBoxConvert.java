package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToFloatConvert;

public class FloatBoxConvert extends ToFloatConvert<Float>
{
    public FloatBoxConvert()
    {
        super(Float.class);
    }

    @Override
    public Float convertToFloat(Float javaObj)
    {
        return javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Float cursorToJavaObject(float value)
    {
        return value;
    }
}
