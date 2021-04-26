package me.xiaolei.room_lite.runtime.sqlite;

public interface WriterRunnable
{
    public void run(SQLiteWriter transaction) throws Exception;
}