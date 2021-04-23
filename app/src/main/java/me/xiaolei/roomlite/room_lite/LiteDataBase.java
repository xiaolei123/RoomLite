package me.xiaolei.roomlite.room_lite;


import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

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
        return new Class[]{User.class, People.class};
    }

    // 是否允许在主线程中执行
    @Override
    public boolean allowRunOnUIThread()
    {
        return true;
    }

    @Override
    public void onUpgrade(@Nullable SQLiteDatabase db, int oldVersion, int newVersion)
    {
        super.onUpgrade(db, oldVersion, newVersion);
    }

    // 数据库版本
    @Override
    public int version()
    {
        return 1;
    }
}