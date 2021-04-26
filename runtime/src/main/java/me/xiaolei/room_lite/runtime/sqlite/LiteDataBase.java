package me.xiaolei.room_lite.runtime.sqlite;

import android.util.Log;

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
        Log.e("XIAOLEI", "LiteDataBase.onCreate:" + db);
        liteDatabase.onOpen(db);
    }

    @Override
    public void onUpgrade(@NonNull SupportSQLiteDatabase db, int oldVersion, int newVersion)
    {
        liteDatabase.onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(@NonNull SupportSQLiteDatabase db)
    {
        Log.e("XIAOLEI", "LiteDataBase.onOpen:" + db);
        super.onOpen(db);
    }

    @Override
    public void onConfigure(@NonNull SupportSQLiteDatabase db)
    {
        db.enableWriteAheadLogging();
        super.onConfigure(db);
        Log.e("XIAOLEI", "LiteDataBase.onConfigure:" + db);
    }
}