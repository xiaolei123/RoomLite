package me.xiaolei.room_lite.runtime.sqlite;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import me.xiaolei.room_lite.Suffix;
import me.xiaolei.room_lite.runtime.entity.EntityHelper;

public abstract class RoomLiteDatabase
{
    // 数据库名称
    private final String dbName;
    // 数据库存放的位置
    private final File dbDir;
    // 缓存DAO对象
    private final Map<Class<?>, Object> daoCache = new ConcurrentHashMap<>();
    // SQLite的执行对象
    private final LiteDataBase database;
    // 每个表的helper对象缓存
    private final Map<Class<?>, EntityHelper> helperCache = new ConcurrentHashMap<>();
    // 当前数据库的URI
    private final Uri dbUri;
    // 数据解析起
    private final ContentResolver resolver;
    // 子线程的Handler
    private final Handler handler;

    /**
     * 初始化数据库，
     *
     * @param dbName 数据库名称
     */
    public RoomLiteDatabase(String dbName)
    {
        this(dbName, DataBaseProvider.context.getDir("databases", Context.MODE_PRIVATE));
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
        Context context = DataBaseProvider.context;
        this.dbUri = new Uri.Builder().scheme("content")
                .authority(context.getPackageName() + ".room_lite.provider")
                .appendPath(dbName)
                .build();
        this.resolver = context.getContentResolver();
        HandlerThread thread = new HandlerThread(dbName + "-thread");
        thread.start();
        this.handler = new Handler(thread.getLooper());
        // 找到每个helper
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
     * 获取一个子线程的Handler
     */
    public Handler getHandler()
    {
        return handler;
    }

    /**
     * 注册数据改动监听器
     *
     * @param tableName 监听哪个表的改动
     * @param observer  监听器
     */
    public void registerContentObserver(String tableName, RoomLiteContentObserver observer)
    {
        Uri tableUri = dbUri.buildUpon()
                .appendPath(tableName).build();
        resolver.registerContentObserver(tableUri, true, observer);
    }

    /**
     * 注销数据监听器
     *
     * @param observer
     */
    public void unregisterContentObserver(RoomLiteContentObserver observer)
    {
        resolver.unregisterContentObserver(observer);
    }

    /**
     * 唤醒RoomLite，通知表已经更新
     */
    public void notifyContent(String tableName)
    {
        boolean hasTable = false;
        for (EntityHelper helper : helperCache.values())
        {
            if (tableName.equals(helper.getTableName()))
            {
                hasTable = true;
                break;
            }
        }
        if (!hasTable)
            throw new RuntimeException(tableName + "不在数据库:" + dbName + "中");
        Uri tableUri = dbUri.buildUpon()
                .appendPath(tableName)
                .build();
        resolver.notifyChange(tableUri, null);
    }

    /**
     * 第一次创建表
     *
     * @param database
     */
    protected void onCreate(@NonNull SupportSQLiteDatabase database)
    {
        Class<?>[] entities = this.getEntities();
        for (Class<?> entity : entities)
        {
            EntityHelper helper = this.helperCache.get(entity);
            assert helper != null;
            String sql = helper.getCreateSQL();
            database.execSQL(sql);
            String[] indexCreateSqls = helper.getCreateIndexSQL();
            Log.e("RoomLite", "建表:" + sql);
            for (String createSql : indexCreateSqls)
            {
                Log.e("RoomLite", "索引:" + createSql);
                database.execSQL(createSql);
            }
        }
        Log.e("XIAOLEI", "onCreate");
    }

    /**
     * 每次连接上数据库<br/>
     * 在这里就简单的对比一下表是否存在，如果涉及到复杂的索引更新，字段更新，那么要走更新的逻辑
     *
     * @param database
     */
    protected void onOpen(@NonNull SupportSQLiteDatabase database)
    {
       
    }

    /**
     * 版本升级
     *
     * @param database
     * @param oldVersion 老版本号
     * @param newVersion 新版本号
     */
    protected void onUpgrade(@Nullable SupportSQLiteDatabase database, int oldVersion, int newVersion)
    {
        Log.e("XIAOLEI", "onUpgrade");
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
