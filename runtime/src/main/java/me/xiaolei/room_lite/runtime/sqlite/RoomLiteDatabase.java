package me.xiaolei.room_lite.runtime.sqlite;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.xiaolei.room_lite.annotations.dao.Dao;
import me.xiaolei.room_lite.runtime.dao_proxy.handler.DaoProxyHandler;
import me.xiaolei.room_lite.runtime.util.RoomLiteUtil;

public abstract class RoomLiteDatabase
{
    /**
     * 数据库名称
     */
    private final String dbName;
    private final File dbDir;
    private final Map<Class<?>, Object> daoCache = new ConcurrentHashMap<>();
    private final LiteDataBase database;

    /**
     * 初始化数据库，
     *
     * @param dbName 数据库名称
     */
    public RoomLiteDatabase(String dbName)
    {
        this(dbName, InitProvider.context.getDir("databases", Context.MODE_PRIVATE));
    }

    /**
     * 初始化数据库，
     *
     * @param dbName 数据库名称
     * @param dbDir  数据库的本地目录
     */
    public RoomLiteDatabase(String dbName, File dbDir)
    {
        this.dbName = dbName;
        this.dbDir = dbDir;
        this.database = new LiteDataBase(this);
    }

    /**
     * 获取数据库名称
     */
    public String getDatabaseName()
    {
        return dbName;
    }

    /**
     * 获取数据库所在目录
     */
    public File getDatabaseDir()
    {
        return dbDir;
    }


    /**
     * 初始化
     *
     * @param db
     */
    public void onOpen(SQLiteDatabase db)
    {
        Class<?>[] entities = this.getEntities();
        for (Class<?> entity : entities)
        {
            String sql = RoomLiteUtil.Create.convertCreateSql(entity);
            Log.e("RoomLite", "create:" + sql);
            db.execSQL(sql);
        }
    }


    /**
     * 版本升级
     *
     * @param db
     * @param oldVersion 老版本号
     * @param newVersion 新版本号
     */
    public void onUpgrade(@Nullable SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    /**
     * 获取表
     *
     * @return
     */
    public abstract Class<?>[] getEntities();

    /**
     * 当前版本
     */
    public abstract int version();

    /**
     * 是否允许在主线程中执行,
     * 这里不建议在主线程中执行,因为如果频繁的IO操作时会阻塞当前线程造成一定程度的卡顿
     * <br/>
     * <br/>
     * true : 允许在主线程中执行<br/>
     * false : 不允许在主线程中执行
     * 
     */
    public boolean allowRunOnUIThread()
    {
        return false;
    }

    /**
     * 获取一个Dao
     *
     * @param daoClass
     * @param <T>
     * @return
     */
    public <T> T getDao(Class<?> daoClass)
    {
        if (!daoClass.isInterface() || !daoClass.isAnnotationPresent(Dao.class))
        {
            throw new RuntimeException("Dao仅支持接口,并且使用@Dao进行修饰");
        }
        T dao = (T) daoCache.get(daoClass);
        if (dao == null)
        {
            ClassLoader loader = daoClass.getClassLoader();
            Object obj = Proxy.newProxyInstance(loader,
                    new Class[]{daoClass},
                    DaoProxyHandler.getHandler(this, this.database));
            dao = (T) obj;
            daoCache.put(daoClass, dao);
        }
        return dao;
    }
}
