package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToByteConvert;

public class ByteConvert extends ToByteConvert
{
    public ByteConvert()
    {
        super(byte.class);
    }

    @Override
    public Byte convertToByte(Object javaObj)
    {
        return (byte) javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Object cursorToJavaObject(byte value)
    {
        return value;
    }
}
