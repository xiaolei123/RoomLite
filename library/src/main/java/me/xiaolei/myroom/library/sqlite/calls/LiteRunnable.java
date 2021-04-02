package me.xiaolei.myroom.library.sqlite.calls;

import android.database.sqlite.SQLiteDatabase;

public interface LiteRunnable<T>
{
    T run(SQLiteDatabase database);
}
