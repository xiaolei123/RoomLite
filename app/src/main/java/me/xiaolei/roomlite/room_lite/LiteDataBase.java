package me.xiaolei.roomlite.room_lite;


import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;

import me.xiaolei.room_lite.runtime.sqlite.DataBaseProvider;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;

public class LiteDataBase extends RoomLiteDatabase
{
    public LiteDataBase()
    {
        // 数据库名称
        super("school");
    }

    // 所有的表Entity
    @Override
    public Class<?>[] getEntities()
    {
        return new Class[]{};
    }

    // 是否允许在主线程中执行
    @Override
    public boolean allowRunOnUIThread()
    {
        return true;
    }

    // 数据库版本
    @Override
    public int version()
    {
        return 1;
    }
}