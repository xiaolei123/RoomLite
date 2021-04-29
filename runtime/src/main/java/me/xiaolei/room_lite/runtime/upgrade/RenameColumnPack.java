
package me.xiaolei.room_lite.runtime.upgrade;

/**
 * 重命名字段的包装
 */
public class RenameColumnPack
{
    /**
     * 对应的表
     */
    public Class<?> entity;
    /**
     * 旧名称
     */
    public String oldColumnName;
    /**
     * 新名称
     */
    public String newColumnName;

    public RenameColumnPack(Class<?> entity, String oldColumnName, String newColumnName)
    {
        this.entity = entity;
        this.oldColumnName = oldColumnName;
        this.newColumnName = newColumnName;
    }
}