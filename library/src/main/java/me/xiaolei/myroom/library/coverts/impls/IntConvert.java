package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToIntegerConvert;

public class IntConvert extends ToIntegerConvert
{
    public IntConvert()
    {
        super(int.class);
    }

    @Override
    public Integer convertToInteger(Object javaObj)
    {
        return (int) javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Object cursorToJavaObject(int value)
    {
        return value;
    }
}
