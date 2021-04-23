package me.xiaolei.room_lite.runtime.sqlite;

import android.database.ContentObserver;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.List;

public abstract class RoomLiteContentObserver extends ContentObserver
{
    private final String dbName;
    private final String tableName;
    private final RoomLiteDatabase database;

    public RoomLiteContentObserver(RoomLiteDatabase database, String tableName)
    {
        super(database.getHandler());
        this.database = database;
        this.dbName = database.getDatabaseName();
        this.tableName = tableName;
    }

    @Override
    public void onChange(boolean selfChange, @Nullable Uri uri)
    {
        if (uri == null)
            return;
        List<String> paths = uri.getPathSegments();
        if (paths.size() != 2)
            return;

        String dbName = paths.get(0);
        String tableName = paths.get(1);
        if (this.dbName.equals(dbName) && this.tableName.equals(tableName))
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
