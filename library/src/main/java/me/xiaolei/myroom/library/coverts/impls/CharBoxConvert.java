package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToIntegerConvert;

public class CharBoxConvert extends ToIntegerConvert<Character>
{
    public CharBoxConvert()
    {
        super(Character.class);
    }

    @Override
    public Integer convertToInteger(Character javaObj)
    {
        return (int) javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Character cursorToJavaObject(int value)
    {
        return (char) value;
    }
}
