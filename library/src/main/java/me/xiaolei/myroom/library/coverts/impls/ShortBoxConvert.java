package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToShortConvert;

public class ShortBoxConvert extends ToShortConvert
{
    public ShortBoxConvert()
    {
        super(Short.class);
    }

    @Override
    public Short convertToShort(Object javaObj)
    {
        return (Short) javaObj;
    }
}
