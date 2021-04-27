package me.xiaolei.room_lite.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.xiaolei.room_lite.runtime.adapters.Adapters;
import me.xiaolei.room_lite.runtime.adapters.Adapter;
import me.xiaolei.room_lite.runtime.coverts.Convert;
import me.xiaolei.room_lite.runtime.coverts.Converts;
import me.xiaolei.room_lite.runtime.entity.EntityHelper;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;
import me.xiaolei.room_lite.runtime.util.AutoGenera;

public class RoomLite
{
    // 缓存每个数据库
    private static final Map<Class<? extends RoomLiteDatabase>, RoomLiteDatabase> dataBaseCache = new ConcurrentHashMap<>();
    // 对每个表对应的helper进行缓存
    public static final Map<Class<?>, EntityHelper> entityHelpers = new ConcurrentHashMap<Class<?>, EntityHelper>()
    {
        {
            try
            {
                Class<?> helperClass = Class.forName("me.xiaolei.room_lite.runtime.auto_genera.EntityHelpers");
                AutoGenera autoGenera = (AutoGenera) helperClass.newInstance();
                Map<Class, Object> helpers = autoGenera.maps();
                for (Map.Entry<Class, Object> entry : helpers.entrySet())
                {
                    Class<?> klass = entry.getKey();
                    EntityHelper value = (EntityHelper) entry.getValue();
                    put(klass, value);
                }
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    };
    // 对每个dao的class进行缓存
    public static final Map<Class<?>, Class> daoHelpers = new ConcurrentHashMap<Class<?>, Class>()
    {
        {
            try
            {
                Class<?> helperClass = Class.forName("me.xiaolei.room_lite.runtime.auto_genera.DaoHelpers");
                AutoGenera autoGenera = (AutoGenera) helperClass.newInstance();
                Map<Class, Object> helpers = autoGenera.maps();
                for (Map.Entry<Class, Object> entry : helpers.entrySet())
                {
                    Class<?> klass = entry.getKey();
                    Class value = (Class) entry.getValue();
                    put(klass, value);
                }
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    };

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
    public static void addAdapter(Adapter<?> adapter)
    {
        Adapters.addAdapter(adapter);
    }
}
