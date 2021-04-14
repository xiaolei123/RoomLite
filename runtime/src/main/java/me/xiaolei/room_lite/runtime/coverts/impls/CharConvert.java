package me.xiaolei.room_lite.runtime.coverts.impls;

import me.xiaolei.room_lite.runtime.coverts.base.ToIntegerConvert;

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

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Object cursorToJavaObject(int value)
    {
        return (char) value;
    }
}
