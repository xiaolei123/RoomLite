package me.xiaolei.myroom.library.coverts.base;


import android.database.Cursor;

import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.Convert;

public abstract class ToBooleanConvert<T> extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToBooleanConvert(Class<T> javaType)
    {
        super(javaType, Column.SQLType.INTEGER);
    }

    /**
     * 把自定义对象转换成数据库基本支持类型
     */
    public abstract Boolean convertToBoolean(T javaObj);

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     */
    public abstract T cursorToJavaObject(boolean value);

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     */
    @Override
    public Object cursorToJavaObject(Cursor cursor, int columnIndex)
    {
        return this.cursorToJavaObject(cursor.getInt(columnIndex) != 0);
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
        return this.convertToBoolean((T) javaObj);
    }
}
