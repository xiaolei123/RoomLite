package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToFloatConvert;

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
}
