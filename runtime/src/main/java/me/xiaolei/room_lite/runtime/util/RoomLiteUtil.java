package me.xiaolei.room_lite.runtime.util;

import android.database.Cursor;

import androidx.sqlite.db.SupportSQLiteDatabase;

public class RoomLiteUtil
{
    /**
     * 根据类型，获取默认值
     *
     * @param klass
     */
    public static Object defaultValue(Class<?> klass)
    {
        if (klass == int.class)
        {
            return 0;
        } else if (klass == boolean.class)
        {
            return false;
        } else if (klass == byte.class)
        {
            return (byte) 0;
        } else if (klass == char.class)
        {
            return (char) 0;
        } else if (klass == float.class)
        {
            return 0f;
        } else if (klass == double.class)
        {
            return 0d;
        } else if (klass == long.class)
        {
            return 0L;
        } else if (klass == short.class)
        {
            return (short) 0;
        } else
        {
            return null;
        }
    }

    /**
     * 检查表是否存在
     *
     * @param db        数据库
     * @param tableName 表名
     * @return 表是否存在
     */
    public static boolean checkTableExist(SupportSQLiteDatabase db, String tableName)
    {
        try (Cursor cursor = db.query("SELECT count(type) as count FROM sqlite_master WHERE type = 'table' AND name=?", new String[]{tableName}))
        {
            cursor.moveToNext();
            int count = cursor.getInt(cursor.getColumnIndex("count"));
            return count > 0;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
