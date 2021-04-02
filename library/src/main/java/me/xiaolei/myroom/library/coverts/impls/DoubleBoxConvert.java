package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToDoubleConvert;

public class DoubleBoxConvert extends ToDoubleConvert
{
    public DoubleBoxConvert()
    {
        super(Double.class);
    }

    @Override
    public Double convertToDouble(Object javaObj)
    {
        return (Double) javaObj;
    }
}
