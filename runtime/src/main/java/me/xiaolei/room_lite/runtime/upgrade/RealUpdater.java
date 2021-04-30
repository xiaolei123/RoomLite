package me.xiaolei.room_lite.runtime.upgrade;

import android.database.Cursor;
import android.util.Log;

import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Map;

import me.xiaolei.room_lite.ConflictAlgorithm;
import me.xiaolei.room_lite.runtime.config.RoomLiteConfig;
import me.xiaolei.room_lite.runtime.entity.EntityHelper;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;
import me.xiaolei.room_lite.runtime.sqlite.Transaction;

/**
 * 真正执行数据库升级的类
 */
public class RealUpdater implements Runnable
{
    private final UpgradeOptions option;
    private final RoomLiteDatabase database;
    private final SupportSQLiteDatabase sqlite;
    private final RoomLiteConfig liteConfig;

    public RealUpdater(UpgradeOptions option,
                       RoomLiteDatabase database,
                       SupportSQLiteDatabase sqlite,
                       RoomLiteConfig liteConfig)
    {
        this.option = option;
        this.database = database;
        this.sqlite = sqlite;
        this.liteConfig = liteConfig;
    }

    @Override
    public void run()
    {
        // 建表
        for (Class<?> entity : option.addTable)
        {
            EntityHelper helper = this.database.getEntityHelper(entity);
            assert helper != null;
            String sql = helper.getCreateSQL();
            this.sqlite.execSQL(sql);
            String[] indexCreateSqls = helper.getCreateIndexSQL();
            Log.e("RoomLite", "升级建表:" + sql);
            for (String createSql : indexCreateSqls)
            {
                Log.e("RoomLite", "升级添加索引:" + createSql);
                this.sqlite.execSQL(createSql);
            }
            // 保存对应的配置
            this.liteConfig.saveOrUpdateEntityMsg(helper);
        }
        // 删除表
        for (String tableName : option.dropTable)
        {
            this.sqlite.execSQL("DROP TABLE " + tableName);
            Log.e("RoomLite", "删除表:" + tableName);
            this.liteConfig.delMsg(tableName);
        }
        // 更新表
        for (TableUpdater updater : option.updater)
        {
            String oldTableName = updater.getOldTableName();
            Class<?> entity = updater.getEntity();
            EntityHelper helper = this.database.getEntityHelper(entity);
            if (oldTableName == null || oldTableName.isEmpty()) // 从旧表迁移数据
            {
                // 定义临时表名
                oldTableName = "RoomLite_tmp";
                // 获取表名
                String tableName = helper.getTableName();
                // 表改名为临时表
                this.sqlite.execSQL("ALTER TABLE " + tableName + " RENAME TO " + oldTableName);
                // 首先删除老的索引
                String[] oldIndexNames = this.liteConfig.getIndexNames(tableName);
                for (String oldIndexName : oldIndexNames)
                {
                    this.sqlite.execSQL("DROP INDEX " + oldIndexName);
                    Log.e("RoomLite", "删除索引:" + oldIndexName);
                }
                // 删除旧的验证数据
                this.liteConfig.delMsg(tableName);
            } else
            {
                // 首先删除老的索引
                String[] oldIndexNames = this.liteConfig.getIndexNames(oldTableName);
                for (String oldIndexName : oldIndexNames)
                {
                    this.sqlite.execSQL("DROP INDEX " + oldIndexName);
                    Log.e("RoomLite", "删除索引:" + oldIndexName);
                }
                // 删除旧的验证数据
                this.liteConfig.delMsg(oldTableName);
            }

            // 在新建新表
            String sql = helper.getCreateSQL();
            Log.e("RoomLite", "升级建表:" + sql);
            this.sqlite.execSQL(sql);
            String[] indexSqls = helper.getCreateIndexSQL();
            for (String indexSql : indexSqls)
            {
                Log.e("RoomLite", "升级添加索引:" + indexSql);
                this.sqlite.execSQL(indexSql);
            }
            // 保存对应的配置
            this.liteConfig.saveOrUpdateEntityMsg(helper);
            // 进行数据迁移
            try (Cursor cursor = this.sqlite.query("SELECT * FROM " + oldTableName))
            {
                Transaction writer = new Transaction(this.sqlite);
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
            this.sqlite.execSQL("DROP TABLE " + oldTableName);
        }
        // 单独执行SQL语句
        for (Map.Entry<String, Object[]> entry : option.sqls.entrySet())
        {
            String sql = entry.getKey();
            Object[] args = entry.getValue();
            this.sqlite.execSQL(sql, args);
        }
    }

    public void start()
    {
        this.run();
    }
}
