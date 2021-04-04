package me.xiaolei.myroom.library.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.xiaolei.myroom.library.anno.Column;
import me.xiaolei.myroom.library.anno.PrimaryKey;

/**
 * 创建表要用到的
 */
public class CreateSqlUtil
{
    /**
     * 将实体类，转换成数据库表 SQL 语句
     *
     * @param klass
     */
    public String convertCreateSql(Class<?> klass)
    {
        String tableName = RoomLiteUtil.getTableName(klass);
        if (tableName == null)
            throw new RuntimeException(klass.getCanonicalName() + "必须使用 @Entity 注解使用");
        if (!hasPrimaryKey(klass))
            throw new RuntimeException(klass.getCanonicalName() + "必须含有至少一个主键 @PrimaryKey 使用");
        StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(" ").append(tableName);
        Map<String, String> columns = getColumns(klass);
        sqlBuilder.append("(");
        for (Map.Entry<String, String> entry : columns.entrySet())
        {
            sqlBuilder.append(entry.getKey()).append(" ").append(entry.getValue()).append(",");
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(");");
        return sqlBuilder.toString();
    }


    /**
     * 检查是否含有主键
     *
     * @param klass
     * @return
     */
    private boolean hasPrimaryKey(Class<?> klass)
    {
        List<Field> fields = RoomLiteUtil.getFields(klass);
        for (Field field : fields)
        {
            if (RoomLiteUtil.isPrimaryKey(field))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析字段
     */
    private Map<String, String> getColumns(Class<?> klass)
    {
        Map<String, String> columns = new LinkedHashMap<>();
        List<Field> fields = RoomLiteUtil.getFields(klass);
        for (Field field : fields)
        {
            String columnName = RoomLiteUtil.getColumnName(field);
            Column.SQLType sqlType = RoomLiteUtil.getColumnSQLType(field);
            String sqlTag = getColumnTag(field);
            columns.put(columnName, sqlType + sqlTag);
        }
        return columns;
    }


    /**
     * 获取其他修饰符
     */
    private String getColumnTag(Field field)
    {
        StringBuilder builder = new StringBuilder();
        // ---------------------@Column-----------------------------
        Column column = field.getAnnotation(Column.class);

        // ---------------------@PrimaryKey-----------------------------
        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        if (primaryKey != null)
        {
            builder.append(" PRIMARY KEY");
            if (primaryKey.autoGenerate())
            {
                Class<?> fieldType = field.getType();
                if (fieldType == int.class || fieldType == Integer.class || fieldType == long.class || fieldType == Long.class)
                {
                    builder.append(" AUTOINCREMENT");
                }
            }
        }
        // --------------------------------------------------
        return builder.toString();
    }
}
