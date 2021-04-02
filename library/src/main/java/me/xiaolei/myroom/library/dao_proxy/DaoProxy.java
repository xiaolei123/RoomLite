package me.xiaolei.myroom.library.dao_proxy;

import java.lang.reflect.Method;

import me.xiaolei.myroom.library.sqlite.BaseDatabase;
import me.xiaolei.myroom.library.sqlite.RoomLiteDatabase;

public abstract class DaoProxy
{
    protected final BaseDatabase database;
    protected final RoomLiteDatabase liteDatabase;

    public DaoProxy(RoomLiteDatabase liteDatabase, BaseDatabase database)
    {
        this.liteDatabase = liteDatabase;
        this.database = database;
    }

    public abstract Object invoke(Method method, Object[] args);
}
