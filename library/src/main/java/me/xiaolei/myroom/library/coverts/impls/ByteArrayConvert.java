package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToByteArrayConvert;

public class ByteArrayConvert extends ToByteArrayConvert
{
    public ByteArrayConvert()
    {
        super(byte[].class);
    }

    @Override
    public byte[] convertToByteArray(Object javaObj)
    {
        return (byte[]) javaObj;
    }
}
