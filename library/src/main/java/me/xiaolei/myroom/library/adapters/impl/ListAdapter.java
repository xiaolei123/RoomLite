package me.xiaolei.myroom.library.adapters.impl;

import android.database.Cursor;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import me.xiaolei.myroom.library.adapters.ContainerAdapter;
import me.xiaolei.myroom.library.sqlite.LiteDataBase;
import me.xiaolei.myroom.library.util.QueryUtil;

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
    public List<?> newInstance(LiteDataBase database, Type genericType, String sql, String[] args)
    {
        if (!(genericType instanceof Class))
            throw new RuntimeException("List的泛型必须是一个准确的可解析的类型");
        Class<?> klass = (Class<?>) genericType;
        return database.postWait(sqLite ->
        {
            List<? super Object> list = new LinkedList<>();
            try (Cursor cursor = sqLite.rawQuery(sql, args))
            {
                while (cursor.moveToNext())
                {
                    list.add(QueryUtil.parseObject(cursor, klass));
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return list;
        });
    }
}
