package me.xiaolei.roomlite.room_lite;


import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;

import me.xiaolei.room_lite.runtime.sqlite.DataBaseProvider;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;
import me.xiaolei.room_lite.runtime.upgrade.UpgradeOptions;

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

    @Override
    public UpgradeOptions[] onUpgradeOptions()
    {
        return new UpgradeOptions[]{
                UpgradeOptions.upgrade(1, 2),
                UpgradeOptions.upgrade(0, 1),
                UpgradeOptions.upgrade(3, 4),
                UpgradeOptions.upgrade(2, 3),
        };
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
        return 3;
    }
}