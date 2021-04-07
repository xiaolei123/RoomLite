package me.xiaolei.myroom.library.sqlite;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import java.util.concurrent.atomic.AtomicBoolean;

public class SQLiteDatabaseWrapper
{
    private final AtomicBoolean read = new AtomicBoolean(true);
    private final SQLiteDatabase database;

    public SQLiteDatabaseWrapper(SQLiteDatabase database)
    {
        this.database = database;
    }
    

}

class CursorWrap implements Cursor
{
    private final Cursor cursor;
    
    public CursorWrap(Cursor cursor)
    {
        this.cursor = cursor;
    }

    @Override
    public int getCount()
    {
        return this.cursor.getCount();
    }

    @Override
    public int getPosition()
    {
        return this.cursor.getPosition();
    }

    @Override
    public boolean move(int offset)
    {
        return this.cursor.move(offset);
    }

    @Override
    public boolean moveToPosition(int position)
    {
        return this.cursor.moveToPosition(position);
    }

    @Override
    public boolean moveToFirst()
    {
        return this.cursor.moveToNext();
    }

    @Override
    public boolean moveToLast()
    {
        return this.cursor.moveToLast();
    }

    @Override
    public boolean moveToNext()
    {
        return this.cursor.moveToNext();
    }

    @Override
    public boolean moveToPrevious()
    {
        return this.cursor.moveToPrevious();
    }

    @Override
    public boolean isFirst()
    {
        return this.cursor.isFirst();
    }

    @Override
    public boolean isLast()
    {
        return this.cursor.isLast();
    }

    @Override
    public boolean isBeforeFirst()
    {
        return this.cursor.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast()
    {
        return this.cursor.isAfterLast();
    }

    @Override
    public int getColumnIndex(String columnName)
    {
        return this.cursor.getColumnIndex(columnName);
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException
    {
        return this.cursor.getColumnIndexOrThrow(columnName);
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        return this.cursor.getColumnName(columnIndex);
    }

    @Override
    public String[] getColumnNames()
    {
        return this.cursor.getColumnNames();
    }

    @Override
    public int getColumnCount()
    {
        return this.cursor.getColumnCount();
    }

    @Override
    public byte[] getBlob(int columnIndex)
    {
        return this.cursor.getBlob(columnIndex);
    }

    @Override
    public String getString(int columnIndex)
    {
        return this.cursor.getString(columnIndex);
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer)
    {
        this.cursor.copyStringToBuffer(columnIndex, buffer);
    }

    @Override
    public short getShort(int columnIndex)
    {
        return this.cursor.getShort(columnIndex);
    }

    @Override
    public int getInt(int columnIndex)
    {
        return this.cursor.getInt(columnIndex);
    }

    @Override
    public long getLong(int columnIndex)
    {
        return this.cursor.getLong(columnIndex);
    }

    @Override
    public float getFloat(int columnIndex)
    {
        return this.cursor.getFloat(columnIndex);
    }

    @Override
    public double getDouble(int columnIndex)
    {
        return this.cursor.getDouble(columnIndex);
    }

    @Override
    public int getType(int columnIndex)
    {
        return this.cursor.getType(columnIndex);
    }

    @Override
    public boolean isNull(int columnIndex)
    {
        return this.cursor.isNull(columnIndex);
    }

    @Override
    public void deactivate()
    {
        this.cursor.deactivate();
    }

    @Override
    public boolean requery()
    {
        return this.cursor.requery();
    }

    @Override
    public void close()
    {
        this.cursor.close();
    }

    @Override
    public boolean isClosed()
    {
        return this.cursor.isClosed();
    }

    @Override
    public void registerContentObserver(ContentObserver observer)
    {
        this.cursor.registerContentObserver(observer);
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer)
    {
        this.cursor.unregisterContentObserver(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer)
    {
        this.cursor.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer)
    {
        this.cursor.unregisterDataSetObserver(observer);
    }

    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri)
    {
        this.cursor.setNotificationUri(cr, uri);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public Uri getNotificationUri()
    {
        return this.cursor.getNotificationUri();
    }

    @Override
    public boolean getWantsAllOnMoveCalls()
    {
        return this.cursor.getWantsAllOnMoveCalls();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void setExtras(Bundle extras)
    {
        this.cursor.setExtras(extras);
    }

    @Override
    public Bundle getExtras()
    {
        return this.cursor.getExtras();
    }

    @Override
    public Bundle respond(Bundle extras)
    {
        return this.cursor.respond(extras);
    }
}