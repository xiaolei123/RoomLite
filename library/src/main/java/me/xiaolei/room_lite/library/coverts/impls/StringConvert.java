package me.xiaolei.room_lite.library.coverts.impls;

import me.xiaolei.room_lite.library.coverts.base.ToStringConvert;

public class StringConvert extends ToStringConvert<String>
{
    public StringConvert()
    {
        super(String.class);
    }

    @Override
    public String convertToString(String javaObj)
    {
        if (javaObj == null)
            return null;
        return javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public String cursorToJavaObject(String value)
    {
        return value;
    }
}
