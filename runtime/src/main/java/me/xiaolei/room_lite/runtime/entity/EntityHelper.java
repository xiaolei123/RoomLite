package me.xiaolei.room_lite.runtime.entity;

import android.content.ContentValues;
import android.database.Cursor;

import me.xiaolei.room_lite.ConflictAlgorithm;
import me.xiaolei.room_lite.runtime.sqlite.SQLiteWriter;

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
     * 创建索引的SQL语句
     */
    public String[] getCreateIndexSQL();

    /**
     * 从Cursor里自动取出对应的字段，并设置进Entity对象中
     */
    public Object fromCursor(Cursor cursor);

    /**
     * 把Entity对象内容取出来，封装成ContentValues
     */
    public ContentValues toContentValues(Object object);

    /**
     * 删除数据库的数据
     */
    public int delete(SQLiteWriter sqLite, Object obj) throws Exception;

    /**
     * 更新数据库的数据
     */
    public int update(SQLiteWriter sqLite, ConflictAlgorithm conflict, Object obj) throws Exception;

    /**
     * 插入数据
     */
    public int insert(SQLiteWriter sqLite, ConflictAlgorithm conflict, Object obj) throws Exception;
}
