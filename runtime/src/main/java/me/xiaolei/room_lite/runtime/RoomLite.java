package me.xiaolei.room_lite.runtime;


import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.xiaolei.room_lite.runtime.entity.EntityHelper;
import me.xiaolei.room_lite.runtime.sqlite.LiteDataBase;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;
import me.xiaolei.room_lite.runtime.util.AutoGenera;
import me.xiaolei.room_lite.runtime.util.AutoRegister;

public class RoomLite
{
    // 缓存每个数据库
    private static final Map<Class<? extends RoomLiteDatabase>, RoomLiteDatabase> dataBaseCache = new ConcurrentHashMap<>();
    // 对每个表对应的helper进行缓存
    private static final Map<Class<?>, EntityHelper> entityHelpers = new ConcurrentHashMap<>();
    // 对每个dao的class进行缓存
    private static final Map<Class<?>, Class> daoHelpers = new ConcurrentHashMap<>();
    // 缓存DAO对象
    private static final Map<Class<?>, Object> daoCache = new ConcurrentHashMap<>();

    static
    {
        try
        {
            Class<?> helperClass = Class.forName("me.xiaolei.room_lite.runtime.auto_genera.AutoRegisterHelpers");
            AutoRegister autoRegister = (AutoRegister) helperClass.newInstance();
            autoRegister.register();
        } catch (Exception e)
        {
            // 忽略反射自动注册类的异常
        }
    }

    private static Map<Class<?>, EntityHelper> getEntityHelpers()
    {
        if (entityHelpers.isEmpty())
        {
            synchronized (entityHelpers)
            {
                if (entityHelpers.isEmpty())
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
                            entityHelpers.put(klass, value);
                        }
                    } catch (Exception e)
                    {
                        // 忽略反射表帮助类的异常
                    }
                }
            }
        }
        return entityHelpers;
    }

    private static Map<Class<?>, Class> getDaoHelpers()
    {
        if (daoHelpers.isEmpty())
        {
            synchronized (daoHelpers)
            {
                if (daoHelpers.isEmpty())
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
                            daoHelpers.put(klass, value);
                        }
                    } catch (Exception e)
                    {
                        // 忽略反射获取Dao帮助类的异常
                    }
                }
            }
        }
        return daoHelpers;
    }

    public static <T> T getDao(Class<T> daoClass, RoomLiteDatabase database, LiteDataBase sqLite)
    {
        T dao = (T) daoCache.get(daoClass);
        if (dao == null)
        {
            try
            {
                Map<Class<?>, Class> daoHelpers = getDaoHelpers();
                Class<T> daoImplClass = daoHelpers.get(daoClass);
                Constructor<T> constructor = daoImplClass.getDeclaredConstructor(RoomLiteDatabase.class, LiteDataBase.class);
                dao = (T) constructor.newInstance(database, sqLite);
                daoCache.put(daoClass, dao);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return dao;
    }

    public static <T extends RoomLiteDatabase> T build(Class<T> klass)
    {
        T database = (T) dataBaseCache.get(klass);
        if (database == null)
        {
            try
            {
                database = klass.newInstance();
                database.helperCache = getEntityHelpers();
                dataBaseCache.put(klass, database);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return database;
    }
}
