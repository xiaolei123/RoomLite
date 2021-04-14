package me.xiaolei.room_lite.library.dao_proxy;

import java.lang.reflect.Method;

import me.xiaolei.room_lite.library.sqlite.LiteDataBase;
import me.xiaolei.room_lite.library.sqlite.RoomLiteDatabase;

public abstract class DaoProxy
{
    protected final LiteDataBase database;
    protected final RoomLiteDatabase liteDatabase;

    public DaoProxy(RoomLiteDatabase liteDatabase, LiteDataBase database)
    {
        this.liteDatabase = liteDatabase;
        this.database = database;
    }

    public abstract Object invoke(Method method, Object[] args);
}
