package me.xiaolei.room_lite.runtime.upgrade;

import java.util.HashMap;
import java.util.Map;

/**
 * 版本更新选项
 */
public class UpgradeOptions
{
    // 使用SQL语句的需求
    public final Map<String, Object[]> sqls = new HashMap<>();
    // 老版本
    public final int oldVersion;
    // 新版本
    public final int newVersion;


    private UpgradeOptions(int oldVersion, int newVersion)
    {
        this.newVersion = newVersion;
        this.oldVersion = oldVersion;
    }

    public static UpgradeOptions upgrade(int oldVersion, int newVersion)
    {
        return new UpgradeOptions(oldVersion, newVersion);
    }
    
    /**
     * 执行某些SQL语句
     */
    public UpgradeOptions execSQL(String sql, Object[] args)
    {
        this.sqls.put(sql, args);
        return this;
    }
}
