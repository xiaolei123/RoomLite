package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToIntegerConvert;

public class CharBoxConvert extends ToIntegerConvert
{
    public CharBoxConvert()
    {
        super(Character.class);
    }

    @Override
    public Integer convertToInteger(Object javaObj)
    {
        return (int) (Character) javaObj;
    }
}
