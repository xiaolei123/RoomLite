package me.xiaolei.room_lite.library.util;

import android.content.ContentValues;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import me.xiaolei.room_lite.annotations.Column;
import me.xiaolei.room_lite.annotations.Entity;
import me.xiaolei.room_lite.annotations.Ignore;
import me.xiaolei.room_lite.annotations.PrimaryKey;
import me.xiaolei.room_lite.library.coverts.Convert;
import me.xiaolei.room_lite.library.coverts.Converts;
import me.xiaolei.room_lite.library.coverts.base.ToBooleanConvert;
import me.xiaolei.room_lite.library.coverts.base.ToByteArrayConvert;
import me.xiaolei.room_lite.library.coverts.base.ToByteConvert;
import me.xiaolei.room_lite.library.coverts.base.ToDoubleConvert;
import me.xiaolei.room_lite.library.coverts.base.ToFloatConvert;
import me.xiaolei.room_lite.library.coverts.base.ToIntegerConvert;
import me.xiaolei.room_lite.library.coverts.base.ToLongConvert;
import me.xiaolei.room_lite.library.coverts.base.ToShortConvert;
import me.xiaolei.room_lite.library.coverts.base.ToStringConvert;

public class RoomLiteUtil
{
    /**
     * 建表工具
     */
    public static final CreateSqlUtil Create = new CreateSqlUtil();
    // 缓存表名
    private final static Map<Class<?>, String> tableNameMapper = new ConcurrentHashMap<>();

    /**
     * 获取 @Entity 的表名称<br/>
     * 会优先去缓存里取名字，如果缓存里没有，则临时反射获取。<br/>
     * <br/>
     *
     * @param klass
     * @return
     */
    public static String getTableName(Class<?> klass)
    {
        String tableName = tableNameMapper.get(klass);
        if (tableName == null)
        {
            Entity entity = klass.getAnnotation(Entity.class);
            if (entity == null)
            {
                return null;
            }
            tableName = entity.name();
            tableName = (tableName.isEmpty()) ? klass.getSimpleName() : tableName;
            tableNameMapper.put(klass, tableName);
        }
        return tableName;
    }

    // 缓存字段名称
    private final static Map<Field, String> columnNameMapper = new ConcurrentHashMap<>();

    /**
     * 解析字段的，对应数据库的字段名称<br/>
     * <br/>
     * 会优先去缓存里获取，获取不到，则通过反射进行获取<br/>
     * <br/>
     */
    public static String getColumnName(Field field)
    {
        String columnName = columnNameMapper.get(field);
        if (columnName == null)
        {
            Column column = field.getAnnotation(Column.class);
            if (column == null || column.name().isEmpty())
            {
                columnName = field.getName();
            } else
            {
                columnName = column.name();
            }
            columnNameMapper.put(field, columnName);
        }
        return columnName;
    }

    private final static Map<Class<?>, List<Field>> fieldCacheMapper = new ConcurrentHashMap<>();

    /**
     * 获取一个entity class的所有的字段,并且自动忽略标记忽略的，或者不可访问的<br/>
     * <br/>
     * 会优先去缓存里获取，如果获取不到，则进行反射获取<br/>
     * <br/>
     *
     * @param klass
     * @return
     */
    public static List<Field> getFields(Class<?> klass)
    {
        List<Field> fields = fieldCacheMapper.get(klass);
        if (fields == null)
        {
            fields = new CopyOnWriteArrayList<>();
            Field[] dfields = klass.getDeclaredFields();
            for (Field field : dfields)
            {
                if (field.isAnnotationPresent(Ignore.class))
                    continue;
                if (!Modifier.isPublic(field.getModifiers()))
                    continue;
                fields.add(field);
            }
            fieldCacheMapper.put(klass, fields);
        }
        return fields;
    }

    // 主键缓存
    private final static Map<Class<?>, List<Field>> primaryKeyMapper = new ConcurrentHashMap<>();

    /**
     * 获取一个表中的所有主键的字段<br/>
     * <br/>
     * 会优先去缓存里获取，如果获取不到，则进行反射获取<br/>
     * <br/>
     * @param klass
     */
    public static List<Field> getPrimaryKeyField(Class<?> klass)
    {
        List<Field> primaryField = primaryKeyMapper.get(klass);
        if (primaryField == null)
        {
            primaryField = new ArrayList<>();
            List<Field> allField = getFields(klass);
            for (Field field : allField)
            {
                if (RoomLiteUtil.isPrimaryKey(field))
                {
                    primaryField.add(field);
                }
            }
            primaryKeyMapper.put(klass, primaryField);
        }
        return primaryField;
    }

    /**
     * 获取对应的SQL语句的字段类型
     */
    public static Column.SQLType getColumnSQLType(Field field)
    {
        Column column = field.getAnnotation(Column.class);
        if (column == null || Column.SQLType.UNDEFINED.equals(column.type()))
        {
            return Converts.convertSqlType(field.getType());
        } else
        {
            return column.type();
        }
    }

    /**
     * 判断字段是否是主键，并且自动递增
     */
    public static boolean isPrimaryKeyAndAutoGenerate(Field field)
    {
        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        return (isPrimaryKey(field) && primaryKey.autoGenerate());
    }

    /**
     * 判断字段是否是主键
     *
     * @param field
     */
    public static boolean isPrimaryKey(Field field)
    {
        return field.isAnnotationPresent(PrimaryKey.class);
    }

    /**
     * 通过反射，获取一个某个对象的某个字段的值
     *
     * @param obj
     * @param field
     * @return
     */
    public static Object getFieldValue(Object obj, Field field)
    {
        try
        {
            return field.get(obj);
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据类型，将一个对象，转换成原生API插入数据时，需要用到的 ContentValues
     * <p>
     * 自动忽略自增长的字段
     *
     * @param klass
     * @param obj
     */
    public static ContentValues convertContentValue(Class<?> klass, Object obj)
    {
        ContentValues values = new ContentValues();
        // 获取所有的字段
        List<Field> fields = RoomLiteUtil.getFields(klass);
        for (Field field : fields)
        {
            // 获取对应字段值
            Object value = RoomLiteUtil.getFieldValue(obj, field);
            // 获取对应字段名称
            String columnName = RoomLiteUtil.getColumnName(field);
            // 获取对应字段的类型
            Class<?> javaType = field.getType();
            // 根据字段类型，获取转换器
            Convert convert = Converts.getConvert(javaType);
            if (convert == null)
                throw new RuntimeException(field + "所对应的数据库类型转换器未定义。");
            // 判断字段的类型是不是数字
            if (convert.getSqlType() == Column.SQLType.INTEGER)
            {
                // 如果是数字，并且标记为主键，并且是自增长的，那么就不添加此字段
                if (RoomLiteUtil.isPrimaryKeyAndAutoGenerate(field))
                    continue;
            }
            Object result = convert.convertToDataBaseObject(value);
            // 用转换器的类型，去跟基本支持类型去比对
            if (convert instanceof ToByteConvert)
            {
                values.put(columnName, result == null ? null : (byte) result);
            } else if (convert instanceof ToLongConvert)
            {
                values.put(columnName, result == null ? null : (long) result);
            } else if (convert instanceof ToFloatConvert)
            {
                values.put(columnName, result == null ? null : (float) result);
            } else if (convert instanceof ToDoubleConvert)
            {
                values.put(columnName, result == null ? null : (double) result);
            } else if (convert instanceof ToShortConvert)
            {
                values.put(columnName, result == null ? null : (short) result);
            } else if (convert instanceof ToByteArrayConvert)
            {
                values.put(columnName, result == null ? null : (byte[]) result);
            } else if (convert instanceof ToStringConvert)
            {
                values.put(columnName, result == null ? null : (String) result);
            } else if (convert instanceof ToBooleanConvert)
            {
                values.put(columnName, result == null ? null : (boolean) result);
            } else if (convert instanceof ToIntegerConvert)
            {
                values.put(columnName, result == null ? null : (int) result);
            }
        }
        return values;
    }

    /**
     * 根据类型，获取默认值
     *
     * @param klass
     */
    public static Object defaultValue(Class<?> klass)
    {
        if (klass == int.class)
        {
            return 0;
        } else if (klass == boolean.class)
        {
            return false;
        } else if (klass == byte.class)
        {
            return (byte) 0;
        } else if (klass == char.class)
        {
            return (char) 0;
        } else if (klass == float.class)
        {
            return 0f;
        } else if (klass == double.class)
        {
            return 0d;
        } else if (klass == long.class)
        {
            return 0L;
        } else if (klass == short.class)
        {
            return (short) 0;
        } else
        {
            return null;
        }
    }
}
