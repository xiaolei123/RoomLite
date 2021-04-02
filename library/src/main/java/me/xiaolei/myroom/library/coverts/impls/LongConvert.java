package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToLongConvert;

public class LongConvert extends ToLongConvert
{
    public LongConvert()
    {
        super(long.class);
    }

    @Override
    public Long convertToLong(Object javaObj)
    {
        return (long) javaObj;
    }
}
