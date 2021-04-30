
package me.xiaolei.room_lite.runtime.upgrade;

/**
 * 更新表
 */
public class TableUpdater
{
    // 旧的表名
    private final String oldTableName;
    // 现在的Entity实体类
    private final Class<?> entity;

    public TableUpdater(Class<?> entity)
    {
        this(null, entity);
    }

    public TableUpdater(String oldTableName, Class<?> entity)
    {
        this.oldTableName = oldTableName;
        this.entity = entity;
    }

    public String getOldTableName()
    {
        return oldTableName;
    }

    public Class<?> getEntity()
    {
        return entity;
    }
}