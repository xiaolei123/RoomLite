package me.xiaolei.myroom.library.coverts.base;


import android.database.Cursor;

import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.coverts.Convert;

public abstract class ToFloatConvert extends Convert
{
    /**
     * @param javaType 设置对应的Java类型
     */
    public ToFloatConvert(Class<?> javaType)
    {
        super(javaType, Column.SQLType.REAL);
    }

    @Override
    public abstract Float convertToFloat(Object javaObj);

    @Override
    public Object cursorToJava(Cursor cursor, int columnIndex)
    {
        return (float) cursor.getFloat(columnIndex);
    }
}
