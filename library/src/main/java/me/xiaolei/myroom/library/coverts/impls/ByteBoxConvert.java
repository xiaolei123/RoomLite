package me.xiaolei.myroom.library.coverts.impls;

import me.xiaolei.myroom.library.coverts.base.ToByteConvert;

public class ByteBoxConvert extends ToByteConvert
{
    public ByteBoxConvert()
    {
        super(Byte.class);
    }
    
    @Override
    public Byte convertToByte(Object javaObj)
    {
        return (Byte) javaObj;
    }
}
