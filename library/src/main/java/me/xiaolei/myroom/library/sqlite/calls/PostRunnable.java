package me.xiaolei.myroom.library.sqlite.calls;

import android.database.sqlite.SQLiteDatabase;

public interface PostRunnable
{
    public void run(SQLiteDatabase database);
}