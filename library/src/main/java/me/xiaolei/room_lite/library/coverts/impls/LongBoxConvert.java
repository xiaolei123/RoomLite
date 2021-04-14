package me.xiaolei.room_lite.library.coverts.impls;

import me.xiaolei.room_lite.library.coverts.base.ToLongConvert;

public class LongBoxConvert extends ToLongConvert<Long>
{
    public LongBoxConvert()
    {
        super(Long.class);
    }

    @Override
    public Long convertToLong(Long javaObj)
    {
        return javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Long cursorToJavaObject(long value)
    {
        return value;
    }
}
