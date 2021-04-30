package me.xiaolei.room_lite.runtime.upgrade;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 版本更新选项
 */
public class UpgradeOptions
{
    // 要添加的表的集合
    public final List<Class<?>> addTable = new LinkedList<>();
    // 要删除的表的集合
    public final List<String> dropTable = new LinkedList<>();
    // 要更新的表的集合
    public final List<TableUpdater> updater = new LinkedList<>();
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
     * 添加表
     *
     * @param entities 本次升级添加的表
     */
    public UpgradeOptions addTable(Class<?>... entities)
    {
        this.addTable.addAll(Arrays.asList(entities));
        return this;
    }

    /**
     * 删除表
     */
    public UpgradeOptions dropTable(String... tableNames)
    {
        this.dropTable.addAll(Arrays.asList(tableNames));
        return this;
    }

    /**
     * 表更新
     */
    public UpgradeOptions update(TableUpdater... updaters)
    {
        this.updater.addAll(Arrays.asList(updaters));
        return this;
    }

}
