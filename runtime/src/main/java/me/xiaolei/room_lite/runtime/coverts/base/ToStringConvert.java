package me.xiaolei.room_lite.runtime.coverts.base;


import android.database.Cursor;

import me.xiaolei.room_lite.annotations.Column;
import me.xiaolei.room_lite.runtime.coverts.Convert;

public abstract class ToStringConvert<T> extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToStringConvert(Class<T> javaType)
    {
        super(javaType, Column.SQLType.TEXT);
    }

    public abstract String convertToString(T javaObj);

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     */
    public abstract T cursorToJavaObject(String value);

    @Override
    public Object cursorToJavaObject(Cursor cursor, int columnIndex)
    {
        return this.cursorToJavaObject(cursor.getString(columnIndex));
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
        return this.convertToString((T) javaObj);
    }
}
