package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToIntegerConvert;

public class CharConvert extends ToIntegerConvert
{
    public CharConvert()
    {
        super(char.class);
    }

    @Override
    public Integer convertToInteger(Object javaObj)
    {
        return (int) (char) javaObj;
    }
}
