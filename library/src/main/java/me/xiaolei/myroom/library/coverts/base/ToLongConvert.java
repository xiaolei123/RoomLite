package me.xiaolei.myroom.library.coverts.base;


import android.database.Cursor;

import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.Convert;

public abstract class ToLongConvert<T> extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToLongConvert(Class<T> javaType)
    {
        super(javaType, Column.SQLType.INTEGER);
    }

    public abstract Long convertToLong(T javaObj);

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     */
    public abstract T cursorToJavaObject(long value);

    @Override
    public Object cursorToJavaObject(Cursor cursor, int columnIndex)
    {
        long value = cursor.getLong(columnIndex);
        return this.cursorToJavaObject(value);
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
        return this.convertToLong((T) javaObj);
    }
}
