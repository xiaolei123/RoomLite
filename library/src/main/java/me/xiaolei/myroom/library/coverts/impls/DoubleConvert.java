package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToDoubleConvert;

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
}
