package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToShortConvert;

public class ShortConvert extends ToShortConvert
{
    public ShortConvert()
    {
        super(short.class);
    }

    @Override
    public Short convertToShort(Object javaObj)
    {
        return (short) javaObj;
    }
}
