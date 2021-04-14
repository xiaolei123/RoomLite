package me.xiaolei.room_lite.library.coverts.impls;

import me.xiaolei.room_lite.library.coverts.base.ToByteArrayConvert;

public class ByteArrayConvert extends ToByteArrayConvert<byte[]>
{
    public ByteArrayConvert()
    {
        super(byte[].class);
    }

    @Override
    public byte[] convertToByteArray(byte[] javaObj)
    {
        return javaObj;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public byte[] cursorToJavaObject(byte[] value)
    {
        return value;
    }
}
