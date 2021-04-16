package me.xiaolei.room_lite;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * 表辅助类
 */
public interface EntityHelper
{
    /**
     * 获取表名
     */
    public String getTableName();

    /**
     * 获取表生动生成的建表SQL语句
     */
    public String getCreateSQL();

    /**
     * 从Cursor里自动取出对应的字段，并设置进Entity对象中
     */
    public Object fromCursor(Cursor cursor);

    /**
     * 把Entity对象内容取出来，封装成ContentValues
     */
    public ContentValues toContentValues(Object object);

    /**
     * 获取表的所有的主键名称
     */
    public String[] getKeyNames();
    
}
