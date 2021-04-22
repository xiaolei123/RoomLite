package me.xiaolei.room_lite.runtime.adapters;

import android.database.Cursor;

public interface Processor
{
    public Object process(Cursor cursor);

    public Cursor queryCursor(String sql, String[] args);
}
