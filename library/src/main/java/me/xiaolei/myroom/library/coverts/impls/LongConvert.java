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

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Object cursorToJavaObject(long value)
    {
        return value;
    }
}
