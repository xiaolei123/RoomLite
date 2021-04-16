package me.xiaolei.room_lite;

public interface TransactionRunnable
{
    public void run(SQLiteTransaction transaction);
}