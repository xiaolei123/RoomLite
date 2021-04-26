package me.xiaolei.room_lite.runtime.sqlite;

import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * 对数据库的一层包装
 */
public class LiteDataBase extends SQLiteDatabaseWrapper
{
    private final RoomLiteDatabase liteDatabase;

    public LiteDataBase(RoomLiteDatabase liteDatabase)
    {
        super(liteDatabase.getDatabaseDir().getAbsolutePath(), liteDatabase.getDatabaseName(), liteDatabase.version());
        this.liteDatabase = liteDatabase;
    }
    
    @Override
    public void onCreate(SupportSQLiteDatabase db)
    {
        liteDatabase.onOpen(db);
    }

    @Override
    public void onUpgrade(SupportSQLiteDatabase db, int oldVersion, int newVersion)
    {
        liteDatabase.onUpgrade(db, oldVersion, newVersion);
    }
}