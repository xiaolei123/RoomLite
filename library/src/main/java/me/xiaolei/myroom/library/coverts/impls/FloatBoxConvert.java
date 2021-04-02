package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToFloatConvert;

public class FloatBoxConvert extends ToFloatConvert
{
    public FloatBoxConvert()
    {
        super(Float.class);
    }

    @Override
    public Float convertToFloat(Object javaObj)
    {
        return (Float) javaObj;
    }
}
