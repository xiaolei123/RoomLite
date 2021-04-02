package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToLongConvert;

public class LongBoxConvert extends ToLongConvert
{
    public LongBoxConvert()
    {
        super(Long.class);
    }

    @Override
    public Long convertToLong(Object javaObj)
    {
        return (Long) javaObj;
    }
}
