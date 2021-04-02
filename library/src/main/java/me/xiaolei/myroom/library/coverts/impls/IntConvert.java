package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToIntegerConvert;

public class IntConvert extends ToIntegerConvert
{
    public IntConvert()
    {
        super(int.class);
    }

    @Override
    public Integer convertToInteger(Object javaObj)
    {
        return (int) javaObj;
    }
}
