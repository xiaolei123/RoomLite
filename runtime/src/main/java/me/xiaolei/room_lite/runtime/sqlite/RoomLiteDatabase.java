package me.xiaolei.room_lite.runtime.sqlite;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;
import java.util.Map;

import me.xiaolei.room_lite.ConflictAlgorithm;
import me.xiaolei.room_lite.runtime.RoomLite;
import me.xiaolei.room_lite.runtime.entity.EntityHelper;
import me.xiaolei.room_lite.runtime.config.RoomLiteConfig;
import me.xiaolei.room_lite.runtime.upgrade.TableUpdater;
import me.xiaolei.room_lite.runtime.upgrade.UpgradeOptions;

public abstract class RoomLiteDatabase
{
    // 数据库名称
    private final String dbName;
    // 数据库存放的位置
    private final File dbDir;
    // SQLite的执行对象
    private final LiteDataBase database;
    // 每个表的helper对象缓存
    public Map<Class<?>, EntityHelper> helperCache;
    // 当前数据库的URI
    private final Uri dbUri;
    // 数据解析起
    private final ContentResolver resolver;
    // 子线程的Handler
    private final Handler handler;
    // context
    private final Context context;
    // 配置
    private final RoomLiteConfig liteConfig;

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
        this.context = DataBaseProvider.context;
        this.dbUri = new Uri.Builder().scheme("content")
                .authority(context.getPackageName() + ".room_lite.provider")
                .appendPath(dbName)
                .build();
        this.resolver = context.getContentResolver();
        HandlerThread thread = new HandlerThread(dbName + "-thread");
        thread.start();
        this.handler = new Handler(thread.getLooper());
        this.liteConfig = new RoomLiteConfig(context, dbName);
    }

    /**
     * 获取上下文
     */
    public Context getContext()
    {
        return context;
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
            // 保存对应的配置
            liteConfig.saveOrUpdateEntityMsg(helper);
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
        Log.e("XIAOLEI", "onOpen");
        Class<?>[] entities = this.getEntities();
        for (Class<?> entity : entities)
        {
            EntityHelper helper = this.helperCache.get(entity);
            assert helper != null;
            // 对比对应的配置
            if (!liteConfig.isSame(helper))
                throw new RuntimeException("@Entity与数据库内置的不一致，请使用数据库升级机制修复（升级数据库版本）");
        }
    }

    /**
     * 版本升级
     *
     * @param database   数据库操作类
     * @param oldVersion 老版本号
     * @param newVersion 新版本号
     */
    protected void onUpgrade(@Nullable SupportSQLiteDatabase database, int oldVersion, int newVersion)
    {
        Log.e("XIAOLEI", "onUpgrade");
        UpgradeOptions[] options = upgradeOptions();
        for (UpgradeOptions option : options)
        {
            // 建表
            for (Class<?> entity : option.addTable)
            {
                EntityHelper helper = this.helperCache.get(entity);
                assert helper != null;
                String sql = helper.getCreateSQL();
                database.execSQL(sql);
                String[] indexCreateSqls = helper.getCreateIndexSQL();
                Log.e("RoomLite", "升级建表:" + sql);
                for (String createSql : indexCreateSqls)
                {
                    Log.e("RoomLite", "升级添加索引:" + createSql);
                    database.execSQL(createSql);
                }
                // 保存对应的配置
                liteConfig.saveOrUpdateEntityMsg(helper);
            }
            // 删除表
            for (String tableName : option.dropTable)
            {
                database.execSQL("DROP TABLE " + tableName);
                Log.e("RoomLite", "删除表:" + tableName);
                liteConfig.delMsg(tableName);
            }
            // 更新表
            for (TableUpdater updater : option.updater)
            {
                String oldTableName = updater.getOldTableName();
                Class<?> entity = updater.getEntity();
                EntityHelper helper = this.helperCache.get(entity);
                if (oldTableName == null || oldTableName.isEmpty()) // 从旧表迁移数据
                {
                    // 定义临时表名
                    oldTableName = "RoomLite_tmp";
                    // 获取表名
                    String tableName = helper.getTableName();
                    // 表改名为临时表
                    database.execSQL("ALTER TABLE " + tableName + " RENAME TO " + oldTableName);
                    // 首先删除老的索引
                    String[] oldIndexNames = liteConfig.getIndexNames(tableName);
                    for (String oldIndexName : oldIndexNames)
                    {
                        database.execSQL("DROP INDEX " + oldIndexName);
                        Log.e("RoomLite", "删除索引:" + oldIndexName);
                    }
                    // 删除旧的验证数据
                    liteConfig.delMsg(tableName);
                } else
                {
                    // 首先删除老的索引
                    String[] oldIndexNames = liteConfig.getIndexNames(oldTableName);
                    for (String oldIndexName : oldIndexNames)
                    {
                        database.execSQL("DROP INDEX " + oldIndexName);
                        Log.e("RoomLite", "删除索引:" + oldIndexName);
                    }
                    // 删除旧的验证数据
                    liteConfig.delMsg(oldTableName);
                }
                
                // 在新建新表
                String sql = helper.getCreateSQL();
                Log.e("RoomLite", "升级建表:" + sql);
                database.execSQL(sql);
                String[] indexSqls = helper.getCreateIndexSQL();
                for (String indexSql : indexSqls)
                {
                    Log.e("RoomLite", "升级添加索引:" + indexSql);
                    database.execSQL(indexSql);
                }
                // 保存对应的配置
                liteConfig.saveOrUpdateEntityMsg(helper);
                // 进行数据迁移
                try (Cursor cursor = database.query("SELECT * FROM " + oldTableName))
                {
                    Transaction writer = new Transaction(database);
                    while (cursor.moveToNext())
                    {
                        try
                        {
                            helper.insert(writer, ConflictAlgorithm.NONE, helper.fromCursor(cursor));
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                // 删除老表
                database.execSQL("DROP TABLE " + oldTableName);
            }
        }
    }

    public UpgradeOptions[] upgradeOptions()
    {
        return new UpgradeOptions[]{};
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
        return true;
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
        return RoomLite.getDao(daoClass, this, this.database);
    }

    /**
     * 根据Entity获取对应的帮助类
     */
    public EntityHelper getEntityHelper(Class<?> klass)
    {
        return this.helperCache.get(klass);
    }

}
