package me.xiaolei.room_lite.runtime.upgrade;

import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.Map;
import me.xiaolei.room_lite.runtime.config.RoomLiteConfig;
import me.xiaolei.room_lite.runtime.sqlite.RoomLiteDatabase;

/**
 * 真正执行数据库升级的类
 */
public class UpdateRunner implements Runnable
{
    private final UpgradeOptions option;
    private final RoomLiteDatabase database;
    private final SupportSQLiteDatabase sqlite;
    private final RoomLiteConfig liteConfig;

    public UpdateRunner(UpgradeOptions option,
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
