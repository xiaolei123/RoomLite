package me.xiaolei.myroom.library.sqlite.calls;

import me.xiaolei.myroom.library.sqlite.SQLiteDatabaseWrapper;

public interface LiteRunnable<T>
{
    T run(SQLiteDatabaseWrapper database);
}
