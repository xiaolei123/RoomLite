package me.xiaolei.room_lite.library.coverts.impls;

import me.xiaolei.room_lite.library.coverts.base.ToShortConvert;

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

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Object cursorToJavaObject(short value)
    {
        return value;
    }
}
