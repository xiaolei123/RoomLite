package me.xiaolei.room_lite.runtime.sqlite;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import me.xiaolei.room_lite.SQLiteWriter;

/**
 * 事务，因为后续所有的有关于写的操作，都需要通过这个类完成
 */
public class Transaction implements SQLiteWriter
{
    // 真实操作的数据库
    private final SQLiteDatabase database;

    public Transaction(SQLiteDatabase database)
    {
        this.database = database;
    }

    @Override
    public void execSQL(String sql) throws SQLException
    {
        this.database.execSQL(sql);
    }

    @Override
    public void execSQL(String sql, Object[] bindArgs) throws SQLException
    {
        this.database.execSQL(sql, bindArgs);
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values)
    {
        return this.database.insert(table, nullColumnHack, values);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs)
    {
        return this.database.delete(table, whereClause, whereArgs);
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs)
    {
        return this.database.update(table, values, whereClause, whereArgs);
    }
}