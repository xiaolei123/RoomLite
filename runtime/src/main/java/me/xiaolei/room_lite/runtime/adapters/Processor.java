package me.xiaolei.room_lite.runtime.adapters;

import android.database.Cursor;

import me.xiaolei.room_lite.runtime.sqlite.LiteDataBase;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteContentObserver;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;

/**
 * 用来解决自定义类型的情况，提供了两种数据获取方式，直接获取或者是监控获取
 */
public abstract class Processor
{
    private final RoomLiteDatabase database;
    private final LiteDataBase sqLite;
    private final String tableName;
    private final String querySql;
    private final String[] args;
    private RoomLiteContentObserver observer;

    public Processor(RoomLiteDatabase database, LiteDataBase sqLite, String tableName, String querySql, String[] args)
    {
        this.database = database;
        this.sqLite = sqLite;
        this.tableName = tableName;
        this.querySql = querySql;
        this.args = args;
    }

    /**
     * 由Cursor对象转换成Java对象，这里是由编译期自动生成对应的代码执行
     */
    protected abstract Object cursorToObject(Cursor cursor);

    /**
     * 直接查询，获取对应的对象
     */
    public Object process()
    {
        try (Cursor cursor = this.sqLite.rawQuery(this.querySql, args))
        {
            return this.cursorToObject(cursor);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册数据改动监听器
     */
    public void registerLiveProcess(OnLiveProcessListener listener)
    {
        if (observer == null)
        {
            synchronized (this)
            {
                observer = new RoomLiteContentObserver(this.database, tableName)
                {
                    @Override
                    public void onChange(boolean selfChange)
                    {
                        listener.onLiveObject(process());
                    }
                };
                listener.onLiveObject(process());
                this.database.registerContentObserver(tableName, observer);
            }
        }
    }

    /**
     * 取消注册数据改动监听器
     */
    public void unRegisterLiveProcess()
    {
        if (observer != null)
        {
            this.database.unregisterContentObserver(observer);
            observer = null;
        }
    }
}
