package me.xiaolei.myroom.library.coverts.base;


import android.database.Cursor;

import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.Convert;

public abstract class ToShortConvert extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToShortConvert(Class<?> javaType)
    {
        super(javaType, Column.SQLType.INTEGER);
    }

    @Override
    public abstract Short convertToShort(Object javaObj);

    @Override
    public Object cursorToJava(Cursor cursor, int columnIndex)
    {
        return (short) cursor.getShort(columnIndex);
    }
}
