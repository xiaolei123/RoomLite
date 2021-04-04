package me.xiaolei.myroom.library;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.xiaolei.myroom.library.coverts.Convert;
import me.xiaolei.myroom.library.coverts.Converts;
import me.xiaolei.myroom.library.sqlite.RoomLiteDatabase;

public class RoomLite
{
    private static final Map<Class<? extends RoomLiteDatabase>, RoomLiteDatabase> dataBaseCache = new ConcurrentHashMap<>();

    public static <T extends RoomLiteDatabase> T build(Class<T> klass)
    {
        T database = (T) dataBaseCache.get(klass);
        if (database == null)
        {
            try
            {
                database = klass.newInstance();
                dataBaseCache.put(klass, database);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return database;
    }

    /**
     * 配置转换器
     *
     * @param convertKlass 转换器
     */
    public static void addConvert(Class<? extends Convert> convertKlass)
    {
        Converts.addConvert(convertKlass);
    }
}
