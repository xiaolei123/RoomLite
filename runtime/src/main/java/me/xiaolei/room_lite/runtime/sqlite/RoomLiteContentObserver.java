package me.xiaolei.room_lite.runtime.sqlite;

import android.database.ContentObserver;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.UUID;

public abstract class RoomLiteContentObserver extends ContentObserver
{
    private final String dbName;
    private final String tableName;
    private final String uid = UUID.randomUUID().toString();
    private final RoomLiteDatabase database;

    public RoomLiteContentObserver(RoomLiteDatabase database, String tableName)
    {
        super(database.getHandler());
        this.database = database;
        this.dbName = database.getDatabaseName();
        this.tableName = tableName;
    }

    public String getUid()
    {
        return uid;
    }

    @Override
    public void onChange(boolean selfChange, @Nullable Uri uri)
    {
        if (uri == null)
            return;
        String dbName = uri.getQueryParameter("dbName");
        String tableName = uri.getQueryParameter("tableName");
        String uid = uri.getQueryParameter("uid");
        if (this.dbName.equals(dbName) && this.tableName.equals(tableName) && (uid == null || uid.equals(this.uid)))
        {
            super.onChange(selfChange, uri);
        }
    }

    @Override
    public boolean deliverSelfNotifications()
    {
        return super.deliverSelfNotifications();
    }

    @Override
    public abstract void onChange(boolean selfChange);
}
