package me.xiaolei.roomlite.room_lite;


import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;
import me.xiaolei.room_lite.runtime.upgrade.TableUpdater;
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
        return new Class[]{User.class, People.class};
    }

    // 是否允许在主线程中执行
    @Override
    public boolean allowRunOnUIThread()
    {
        return true;
    }

    @Override
    public UpgradeOptions[] upgradeOptions()
    {
        return new UpgradeOptions[]{
                UpgradeOptions.upgrade(1, 2).update(User.class),
                UpgradeOptions.upgrade(2, 3).addTable(User3.class)
        };
    }

    // 数据库版本
    @Override
    public int version()
    {
        return 1;
    }
}