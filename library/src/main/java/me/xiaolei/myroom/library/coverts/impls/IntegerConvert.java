package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToIntegerConvert;

public class IntegerConvert extends ToIntegerConvert
{
    public IntegerConvert()
    {
        super(Integer.class);
    }

    @Override
    public Integer convertToInteger(Object javaObj)
    {
        return (Integer) javaObj;
    }
}
