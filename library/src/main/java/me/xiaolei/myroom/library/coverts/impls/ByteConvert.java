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
}
