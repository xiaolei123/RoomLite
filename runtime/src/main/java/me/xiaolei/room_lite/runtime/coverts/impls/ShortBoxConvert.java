package me.xiaolei.room_lite.runtime.coverts.impls;

import me.xiaolei.room_lite.runtime.coverts.base.ToShortConvert;

public class ShortBoxConvert extends ToShortConvert<Short>
{
    public ShortBoxConvert()
    {
        super(Short.class);
    }

    @Override
    public Short convertToShort(Short javaObj)
    {
        return javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Short cursorToJavaObject(short value)
    {
        return value;
    }
}
