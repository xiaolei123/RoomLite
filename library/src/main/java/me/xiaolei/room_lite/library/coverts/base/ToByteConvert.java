package me.xiaolei.room_lite.library.coverts.base;


import android.database.Cursor;

import me.xiaolei.room_lite.annotations.Column;
import me.xiaolei.room_lite.library.coverts.Convert;

public abstract class ToByteConvert<T> extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToByteConvert(Class<T> javaType)
    {
        super(javaType, Column.SQLType.INTEGER);
    }

    public abstract Byte convertToByte(T javaObj);

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     */
    public abstract T cursorToJavaObject(byte value);

    @Override
    public Object cursorToJavaObject(Cursor cursor, int columnIndex)
    {
        return this.cursorToJavaObject((byte) cursor.getInt(columnIndex));
    }

    /**
     * 从Java对象转换成数据库支持的对象
     *
     * @param javaObj 要转换的Java对象
     * @return 数据库支持的对象
     */
    @Override
    public Object convertToDataBaseObject(Object javaObj)
    {
        return this.convertToByte((T) javaObj);
    }
}
