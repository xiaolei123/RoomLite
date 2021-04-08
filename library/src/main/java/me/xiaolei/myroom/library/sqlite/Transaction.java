package me.xiaolei.myroom.library.sqlite;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * 事务
 */
public class Transaction
{
    // 真实操作的数据库
    private final SQLiteDatabase database;

    public Transaction(SQLiteDatabase database)
    {
        this.database = database;
    }

    public void execSQL(String sql) throws SQLException
    {
        this.database.execSQL(sql);
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException
    {
        this.database.execSQL(sql, bindArgs);
    }

    public long insert(String table, String nullColumnHack, ContentValues values)
    {
        return this.database.insert(table, nullColumnHack, values);
    }

    public int delete(String table, String whereClause, String[] whereArgs)
    {
        return this.database.delete(table, whereClause, whereArgs);
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs)
    {
        return this.database.update(table, values, whereClause, whereArgs);
    }

    public interface TransactionRunnable
    {
        public void run(Transaction transaction);
    }
}