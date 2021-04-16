package me.xiaolei.room_lite;

public interface WriterRunnable
{
    public void run(SQLiteWriter transaction) throws Exception;
}