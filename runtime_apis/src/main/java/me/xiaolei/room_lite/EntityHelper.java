package me.xiaolei.room_lite;

import android.content.ContentValues;
import android.database.Cursor;

public interface EntityHelper
{
    public String getTableName();

    public String getCreateSQL();

    public Object fromCursor(Cursor cursor);

    public ContentValues toContentValues(Object object);
}
