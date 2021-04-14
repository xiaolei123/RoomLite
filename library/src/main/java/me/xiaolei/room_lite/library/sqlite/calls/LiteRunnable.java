package me.xiaolei.room_lite.library.sqlite.calls;

import me.xiaolei.room_lite.library.sqlite.SQLiteDatabaseWrapper;

public interface LiteRunnable<T>
{
    T run(SQLiteDatabaseWrapper database);
}
