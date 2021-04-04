package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToByteConvert;

public class ByteBoxConvert extends ToByteConvert<Byte>
{
    public ByteBoxConvert()
    {
        super(Byte.class);
    }
    
    @Override
    public Byte convertToByte(Byte javaObj)
    {
        return javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Byte cursorToJavaObject(byte value)
    {
        return value;
    }
}
