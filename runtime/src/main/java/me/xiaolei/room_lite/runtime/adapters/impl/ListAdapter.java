package me.xiaolei.room_lite.runtime.adapters.impl;

import android.database.Cursor;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import me.xiaolei.room_lite.EntityHelper;
import me.xiaolei.room_lite.SQLiteReader;
import me.xiaolei.room_lite.runtime.adapters.ContainerAdapter;
import me.xiaolei.room_lite.runtime.coverts.Convert;
import me.xiaolei.room_lite.runtime.coverts.Converts;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;

/**
 * List的适配器
 */
public class ListAdapter extends ContainerAdapter<List>
{
    public ListAdapter()
    {
        super(List.class);
    }

    @Override
    public List<?> newInstance(RoomLiteDatabase liteDatabase, SQLiteReader database, Type genericType, String sql, String[] args)
    {
        if (!(genericType instanceof Class))
            throw new RuntimeException("List的泛型必须是一个准确的可解析的类型");
        Class<?> klass = (Class<?>) genericType;
        Convert convert = Converts.getConvert(klass);
        EntityHelper helper = liteDatabase.getEntityHelper(klass);
        List<? super Object> list = new LinkedList<>();
        try (Cursor cursor = database.rawQuery(sql, args))
        {
            String[] columnNames = cursor.getColumnNames();
            while (cursor.moveToNext())
            {
                if (convert != null)
                {
                    int columnIndex = cursor.getColumnIndex(columnNames[0]);
                    list.add(convert.cursorToJavaObject(cursor, columnIndex));
                } else
                {
                    list.add(helper.fromCursor(cursor));
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return list;
    }
}
