package me.xiaolei.room_lite.runtime.coverts.impls;

import me.xiaolei.room_lite.runtime.coverts.base.ToByteArrayConvert;

public class ByteBoxArrayConvert extends ToByteArrayConvert<Byte[]>
{
    public ByteBoxArrayConvert()
    {
        super(Byte[].class);
    }

    @Override
    public byte[] convertToByteArray(Byte[] javaObj)
    {
        if (javaObj == null) return new byte[0];
        byte[] result = new byte[javaObj.length];
        for (int i = 0; i < javaObj.length; i++)
        {
            result[i] = javaObj[i];
        }
        return result;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     *
     * @param value
     */
    @Override
    public Byte[] cursorToJavaObject(byte[] value)
    {
        if (value == null)
        {
            return null;
        }
        Byte[] result = new Byte[value.length];
        for (int i = 0; i < value.length; i++)
        {
            result[i] = value[i];
        }
        return result;
    }
}
