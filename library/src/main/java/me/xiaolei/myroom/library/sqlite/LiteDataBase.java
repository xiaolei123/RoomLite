package me.xiaolei.myroom.library.sqlite;


import android.database.sqlite.SQLiteDatabase;

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
    public void onCreate(SQLiteDatabase db)
    {
        liteDatabase.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        liteDatabase.onUpgrade(db, oldVersion, newVersion);
    }

}