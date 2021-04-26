package me.xiaolei.room_lite.runtime.sqlite;

import android.content.ContentValues;
import android.database.SQLException;

import androidx.sqlite.db.SupportSQLiteDatabase;


/**
 * 事务，因为后续所有的有关于写的操作，都需要通过这个类完成
 */
public class Transaction implements SQLiteWriter
{
    // 真实操作的数据库
    private final SupportSQLiteDatabase database;

    public Transaction(SupportSQLiteDatabase database)
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
    public long insert(String table, int conflictAlgorithm, ContentValues values)
    {
        return this.database.insert(table, conflictAlgorithm, values);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs)
    {
        return this.database.delete(table, whereClause, whereArgs);
    }
    
    @Override
    public int update(String table, int conflictAlgorithm, ContentValues values, String whereClause, String[] whereArgs)
    {
        return this.database.update(table, conflictAlgorithm, values, whereClause, whereArgs);
    }
}