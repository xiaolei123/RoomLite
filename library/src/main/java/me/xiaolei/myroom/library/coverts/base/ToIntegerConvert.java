package me.xiaolei.myroom.library.coverts.base;


import android.database.Cursor;

import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.Convert;

public abstract class ToIntegerConvert extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToIntegerConvert(Class<?> javaType)
    {
        super(javaType, Column.SQLType.INTEGER);
    }

    @Override
    public abstract Integer convertToInteger(Object javaObj);

    @Override
    public Object cursorToJava(Cursor cursor, int columnIndex)
    {
        return (int) cursor.getInt(columnIndex);
    }
}
