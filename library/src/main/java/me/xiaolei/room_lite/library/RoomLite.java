package me.xiaolei.room_lite.library;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.xiaolei.room_lite.library.adapters.Adapters;
import me.xiaolei.room_lite.library.adapters.ContainerAdapter;
import me.xiaolei.room_lite.library.coverts.Convert;
import me.xiaolei.room_lite.library.coverts.Converts;
import me.xiaolei.room_lite.library.sqlite.RoomLiteDatabase;

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
     * @param convert 转换器
     */
    public static void addConvert(Convert convert)
    {
        Converts.addConvert(convert);
    }

    /**
     * 添加适配器
     *
     * @param adapter
     */
    public static void addAdapter(ContainerAdapter<?> adapter)
    {
        Adapters.addAdapter(adapter);
    }
}
