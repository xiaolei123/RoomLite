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
    public final List<Class<?>> dropTable = new LinkedList<>();
    // 重命名表名
    public final Map<String, Class<?>> renameTable = new ConcurrentHashMap<>();
    // 添加字段
    public final Map<Class<?>, String> addColumn = new ConcurrentHashMap<>();
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
    public UpgradeOptions dropTable(Class<?>... entities)
    {
        this.dropTable.addAll(Arrays.asList(entities));
        return this;
    }

    /**
     * 重命名表
     *
     * @param oldTableName 旧表名
     * @param newTable     新表
     */
    public UpgradeOptions renameTable(String oldTableName, Class<?> newTable)
    {
        this.renameTable.put(oldTableName, newTable);
        return this;
    }

    /**
     * 添加某个表的字段
     */
    public UpgradeOptions addColumn(Class<?> entity, String columnName)
    {
        this.addColumn.put(entity, columnName);
        return this;
    }
}
