package me.xiaolei.myroom.library.sqlite.calls;

import android.database.sqlite.SQLiteDatabase;

public interface ResultRunnable<T>
{
    public T run(SQLiteDatabase database);
}