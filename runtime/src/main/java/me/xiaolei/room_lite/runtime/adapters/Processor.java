package me.xiaolei.room_lite.runtime.adapters;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

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
    private final ContentResolver resolver;

    public Processor(RoomLiteDatabase database, LiteDataBase sqLite, String tableName, String querySql, String[] args)
    {
        this.database = database;
        this.sqLite = sqLite;
        this.tableName = tableName;
        this.querySql = querySql;
        this.args = args;
        this.resolver = this.database.getResolver();
    }

    protected abstract Object cursorToObject(Cursor cursor);

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

    public void registerLiveProcess(OnLiveProcessListener listener)
    {
        Uri dbUri = this.database.getDbUri();
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
                
                Uri tableUri = dbUri.buildUpon()
                        .appendQueryParameter("tableName", tableName)
                        .appendQueryParameter("uid", observer.getUid())
                        .build();
                resolver.registerContentObserver(tableUri, false, observer);
            }
        }
    }

    public void unRegisterLiveProcess()
    {
        if (observer != null)
            resolver.unregisterContentObserver(observer);
    }
}
