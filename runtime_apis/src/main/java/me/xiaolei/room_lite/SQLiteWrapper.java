package me.xiaolei.room_lite;

import android.database.Cursor;

public interface SQLiteWrapper
{
    public Cursor rawQuery(String sql, String[] selectionArgs);

    public int getVersion();

    public void setVersion(int version);

    public void doTransaction(TransactionRunnable runnable);
}
