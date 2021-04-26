package me.xiaolei.room_lite.runtime.sqlite;

import android.database.Cursor;

public interface SQLiteReader
{
    public Cursor rawQuery(String sql, String[] selectionArgs);

    public int getVersion();

    public void doTransaction(WriterRunnable runnable);
}
