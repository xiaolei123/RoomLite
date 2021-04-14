package me.xiaolei.room_lite.runtime.sqlite.calls;

import me.xiaolei.room_lite.runtime.sqlite.SQLiteDatabaseWrapper;

public interface LiteRunnable<T>
{
    T run(SQLiteDatabaseWrapper database);
}
