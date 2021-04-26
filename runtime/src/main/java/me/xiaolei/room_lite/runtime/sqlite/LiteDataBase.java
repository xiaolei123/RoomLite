package me.xiaolei.room_lite.runtime.sqlite;

import androidx.annotation.NonNull;
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
    public void onCreate(@NonNull SupportSQLiteDatabase db)
    {
        liteDatabase.onCreate(db);
    }

    @Override
    public void onUpgrade(@NonNull SupportSQLiteDatabase db, int oldVersion, int newVersion)
    {
        liteDatabase.onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(@NonNull SupportSQLiteDatabase db)
    {
        liteDatabase.onOpen(db);
    }

    @Override
    public void onConfigure(@NonNull SupportSQLiteDatabase db)
    {
        // 启用并发读写
        db.enableWriteAheadLogging();
        // 启用外健约束
        db.setForeignKeyConstraintsEnabled(true);
        super.onConfigure(db);
    }
}