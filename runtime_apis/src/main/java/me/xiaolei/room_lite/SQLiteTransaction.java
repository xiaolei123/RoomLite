package me.xiaolei.room_lite;

import android.content.ContentValues;
import android.database.SQLException;

public interface SQLiteTransaction
{
    public void execSQL(String sql) throws SQLException;

    public void execSQL(String sql, Object[] bindArgs) throws SQLException;

    public long insert(String table, String nullColumnHack, ContentValues values);

    public int delete(String table, String whereClause, String[] whereArgs);

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs);
}
