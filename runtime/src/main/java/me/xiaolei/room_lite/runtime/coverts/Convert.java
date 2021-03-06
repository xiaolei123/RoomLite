package me.xiaolei.room_lite.runtime.coverts;


import android.database.Cursor;

import me.xiaolei.room_lite.annotations.Column;

/**
 * 转换器，从自定义类型，转换成数据库基本类型的工具
 */
public abstract class Convert
{
    private final Class<?> javaType;
    private final Column.SQLType sqlType;

    /**
     * 获取对应的Java类型
     */
    public Class<?> getJavaType()
    {
        return javaType;
    }

    /**
     * 获取对应的SQL类型
     */
    public Column.SQLType getSqlType()
    {
        return sqlType;
    }

    /**
     * @param javaType 设置对应的Java类型
     * @param sqlType  设置对应的SQL类型
     */
    public Convert(Class<?> javaType, Column.SQLType sqlType)
    {
        this.javaType = javaType;
        this.sqlType = sqlType;
    }

    /**
     * 从数据库的Cursor获取数据,并转换成对应 javaType 类型的数据
     */
    public abstract Object cursorToJavaObject(Cursor cursor, int columnIndex);

    /**
     * 从Java对象转换成数据库支持的对象
     *
     * @param javaObj 要转换的Java对象
     * @return 数据库支持的对象
     */
    public abstract Object convertToDataBaseObject(Object javaObj);
}
