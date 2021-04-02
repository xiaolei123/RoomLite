package me.xiaolei.myroom.library.coverts.impls;

import android.database.Cursor;

import me.xiaolei.myroom.library.coverts.base.ToByteArrayConvert;

public class ByteBoxArrayConvert extends ToByteArrayConvert
{
    public ByteBoxArrayConvert()
    {
        super(Byte[].class);
    }

    @Override
    public byte[] convertToByteArray(Object javaObj)
    {
        if (javaObj == null) return new byte[0];
        Byte[] obj = (Byte[]) javaObj;
        byte[] result = new byte[obj.length];
        for (int i = 0; i < obj.length; i++)
        {
            result[i] = obj[i];
        }
        return result;
    }

    @Override
    public Object cursorToJava(Cursor cursor, int columnIndex)
    {
        byte[] datas = (byte[]) super.cursorToJava(cursor, columnIndex);
        if (datas == null)
        {
            return null;
        }
        Byte[] result = new Byte[datas.length];
        for (int i = 0; i < datas.length; i++)
        {
            result[i] = datas[i];
        }
        return result;
    }
}
