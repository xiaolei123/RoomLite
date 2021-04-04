package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToIntegerConvert;

public class IntegerConvert extends ToIntegerConvert<Integer>
{
    public IntegerConvert()
    {
        super(Integer.class);
    }

    @Override
    public Integer convertToInteger(Integer javaObj)
    {
        return javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Integer cursorToJavaObject(int value)
    {
        return value;
    }
}
