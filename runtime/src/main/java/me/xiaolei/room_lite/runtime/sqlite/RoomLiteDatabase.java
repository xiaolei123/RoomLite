package me.xiaolei.room_lite.runtime.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import me.xiaolei.room_lite.EntityHelper;
import me.xiaolei.room_lite.Suffix;

public abstract class RoomLiteDatabase
{
    /**
     * 数据库名称
     */
    private final String dbName;
    private final File dbDir;
    private final Map<Class<?>, Object> daoCache = new ConcurrentHashMap<>();
    private final LiteDataBase database;
    private final Map<Class<?>, EntityHelper> helperCache = new HashMap<>();

    /**
     * 初始化数据库，
     *
     * @param dbName 数据库名称
     */
    public RoomLiteDatabase(String dbName)
    {
        this(dbName, InitProvider.context.getDir("databases", Context.MODE_PRIVATE));
        Class<?>[] entities = this.getEntities();
        if (entities == null || entities.length == 0)
            throw new RuntimeException(this.getClass().getCanonicalName() + "的 getEntities() 函数返回的Entities不可以为空");
        for (Class<?> entityKlass : entities)
        {
            String klassName = entityKlass.getSimpleName();
            String packageName = Objects.requireNonNull(entityKlass.getPackage()).getName();
            String helperName = packageName + "." + klassName + Suffix.helper_suffix;
            try
            {
                Class<? extends EntityHelper> helperClass = (Class<? extends EntityHelper>) Class.forName(helperName);
                helperCache.put(entityKlass, helperClass.newInstance());
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
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
            EntityHelper helper = this.helperCache.get(entity);
            assert helper != null;
            String sql = helper.getCreateSQL();
            db.execSQL(sql);
            String[] indexCreateSqls = helper.getCreateIndexSQL();
            Log.e("RoomLite", "create:" + sql);
            for (String createSql : indexCreateSqls)
            {
                Log.e("RoomLite", "indexCreate:" + createSql);
                db.execSQL(createSql);
            }
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
    public <T> T getDao(Class<T> daoClass)
    {
        T dao = (T) daoCache.get(daoClass);
        if (dao == null)
        {
            try
            {
                String className = daoClass.getSimpleName();
                String implClassName = daoClass.getPackage().getName() + "." + className + Suffix.dao_suffix;
                Class<T> daoImplClass = (Class<T>) Class.forName(implClassName);
                Constructor<T> constructor = daoImplClass.getDeclaredConstructor(RoomLiteDatabase.class, LiteDataBase.class);
                dao = (T) constructor.newInstance(this, this.database);
                daoCache.put(daoClass, dao);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return dao;
    }

    /**
     * 根据Entity获取对应的帮助类
     */
    public EntityHelper getEntityHelper(Class<?> klass)
    {
        return this.helperCache.get(klass);
    }

}
